package com.vizeno.loomie.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * YouCam / Fit try-on composite cache.
 *
 * Lookup order: memory → disk (filesDir/youcam-cache) → pre-seeded assets → (caller does live generate).
 * Keys: personBodyHash (or stable alias like "person1") + ordered garment / accessory stack ids.
 */
object TryOnCompositeCache {
    private const val DISK_DIR = "youcam-cache"
    private const val ASSET_DIR = "youcam-cache"
    /** Prefs flag: body was seeded from bundled Person 1 (allows asset alias lookup). */
    const val BODY_SOURCE_PERSON1 = "person1"

    /** Built-in sample aliases person1…person6 (assets + offline seed filenames). */
    fun isBuiltInPersonAlias(token: String?): Boolean {
        val t = token?.trim().orEmpty()
        return t.matches(Regex("person[1-6]"))
    }

    /** Soft cap ~24 MB of decoded composites (sizeOf returns KiB). */
    private val memory = object : LruCache<String, Bitmap>(24 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun cacheKey(personKey: String, garmentIds: List<String>): String {
        val person = personKey.trim().ifBlank { "unknown" }
        val stack = garmentIds.map { it.trim() }.filter { it.isNotBlank() }.joinToString("+")
        return "${person}__$stack"
    }

    fun safeFileName(cacheKey: String): String =
        cacheKey.replace(Regex("[^a-zA-Z0-9_+.-]"), "_").take(120) + ".jpg"

    /** SHA-256 hex (16 chars) of file bytes; empty if unreadable. */
    fun hashFile(file: File): String? = runCatching {
        if (!file.exists() || file.length() <= 0) return null
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(16 * 1024)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                digest.update(buf, 0, n)
            }
        }
        digest.digest().joinToString("") { "%02x".format(it) }.take(16)
    }.getOrNull()

    fun hashUri(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        val uri = Uri.parse(uriString)
        val path = uri.path
        if (!path.isNullOrBlank()) {
            val file = File(path)
            if (file.exists()) return hashFile(file)
        }
        return runCatching {
            val digest = MessageDigest.getInstance("SHA-256")
            context.contentResolver.openInputStream(uri)?.use { input ->
                val buf = ByteArray(16 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    digest.update(buf, 0, n)
                }
            } ?: return null
            digest.digest().joinToString("") { "%02x".format(it) }.take(16)
        }.getOrNull()
    }

    /** Primary person token for keys; prefers content hash, falls back to sanitized URI. */
    fun personKey(context: Context, bodyUri: String?): String {
        hashUri(context, bodyUri)?.let { return it }
        val raw = bodyUri?.trim().orEmpty()
        if (raw.isBlank()) return "unknown"
        return raw.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(48)
    }

    fun candidatePersonKeys(context: Context, bodyUri: String?): List<String> {
        val primary = personKey(context, bodyUri)
        val keys = linkedSetOf(primary)
        val source = LocalProfileStore.bodySource(context)
        if (isBuiltInPersonAlias(source)) {
            keys += source!!
        }
        return keys.toList()
    }

    fun diskDir(context: Context): File =
        File(context.filesDir, DISK_DIR).also { it.mkdirs() }

    fun diskFile(context: Context, cacheKey: String): File =
        File(diskDir(context), safeFileName(cacheKey))

    /**
     * Resolve a cached composite. Tries each person key × memory → disk → assets.
     * On asset hit, copies into disk for next open.
     */
    fun load(context: Context, personKeys: List<String>, garmentIds: List<String>): CacheHit? {
        if (garmentIds.isEmpty()) return null
        for (person in personKeys) {
            val key = cacheKey(person, garmentIds)
            memory.get(key)?.takeIf { !it.isRecycled }?.let {
                return CacheHit(it, key, CacheTier.MEMORY)
            }
            val disk = diskFile(context, key)
            if (disk.exists() && disk.length() > 0) {
                val bmp = BitmapFactory.decodeFile(disk.absolutePath) ?: continue
                memory.put(key, bmp)
                return CacheHit(bmp, key, CacheTier.DISK, Uri.fromFile(disk).toString())
            }
            val assetPath = "$ASSET_DIR/${safeFileName(key)}"
            val fromAsset = runCatching {
                if (!assetPath.endsWith(".jpg", ignoreCase = true) &&
                    !assetPath.endsWith(".jpeg", ignoreCase = true) &&
                    !assetPath.endsWith(".png", ignoreCase = true)
                ) {
                    return@runCatching null
                }
                context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) }
            }.getOrNull()
            if (fromAsset != null) {
                memory.put(key, fromAsset)
                val savedUri = saveToDisk(context, key, fromAsset)
                return CacheHit(fromAsset, key, CacheTier.ASSETS, savedUri)
            }
        }
        return null
    }

    fun load(context: Context, bodyUri: String?, garmentIds: List<String>): CacheHit? =
        load(context, candidatePersonKeys(context, bodyUri), garmentIds)

    fun put(context: Context, bodyUri: String?, garmentIds: List<String>, bitmap: Bitmap): String? {
        val key = cacheKey(personKey(context, bodyUri), garmentIds)
        return put(context, key, bitmap)
    }

    fun put(context: Context, cacheKey: String, bitmap: Bitmap): String? {
        memory.put(cacheKey, bitmap)
        return saveToDisk(context, cacheKey, bitmap)
    }

    private fun saveToDisk(context: Context, cacheKey: String, bitmap: Bitmap): String? = runCatching {
        val dest = diskFile(context, cacheKey)
        FileOutputStream(dest).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output))
        }
        Uri.fromFile(dest).toString()
    }.getOrNull()

    /** Drop memory + disk entries for a person token (hash or person1). */
    fun invalidatePerson(context: Context, personToken: String?) {
        val token = personToken?.trim().orEmpty()
        if (token.isBlank()) return
        val prefix = "${token}__"
        val memKeys = memory.snapshot().keys.filter { it.startsWith(prefix) || it.startsWith("${token}_") }
        memKeys.forEach { memory.remove(it) }
        diskDir(context).listFiles()?.forEach { file ->
            if (file.name.startsWith(token) || file.name.contains(token)) {
                runCatching { file.delete() }
            }
        }
    }

    fun invalidateAll(context: Context) {
        memory.evictAll()
        diskDir(context).listFiles()?.forEach { runCatching { it.delete() } }
        // Legacy tryon-*.jpg files from earlier cache layout
        context.filesDir.listFiles()?.filter { it.name.startsWith("tryon-") }?.forEach {
            runCatching { it.delete() }
        }
    }

    data class CacheHit(
        val bitmap: Bitmap,
        val cacheKey: String,
        val tier: CacheTier,
        val fileUri: String? = null
    )

    enum class CacheTier { MEMORY, DISK, ASSETS }
}
