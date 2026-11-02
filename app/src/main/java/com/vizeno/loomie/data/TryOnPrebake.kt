package com.vizeno.loomie.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.vizeno.loomie.R
import com.vizeno.loomie.integration.LookWeaveGateway
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.model.GarmentCategory
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Warms try-on composites for the active person while the app loads / after profile switch.
 * Order: memory → disk → assets; optional live YouCam only on miss when [liveGenerate] is true.
 */
object TryOnPrebake {
    private const val TAG = "TryOnPrebake"

    data class Result(
        val warmed: Int,
        val misses: List<String>,
        val alias: String?
    )

    suspend fun warmActivePerson(
        context: Context,
        liveGenerate: Boolean = false
    ): Result = withContext(Dispatchers.IO) {
        val profile = SamplePersonProfiles.activeProfile(context)
        val alias = LocalProfileStore.bodySource(context) ?: profile.id.takeIf { profile.builtIn }
        val bodyUri = LocalProfileStore.bodyUri(context)
        val garmentIds = profile.preferredGarmentIds
            .filter { id ->
                LookWeaveRepository.allGarments.any {
                    it.id == id && (it.category == GarmentCategory.OUTERWEAR || it.category == GarmentCategory.TOP)
                }
            }
            .ifEmpty { listOf("g12", "g1", "g2") }
            .take(3)
        warm(
            context = context,
            bodyUri = bodyUri,
            alias = alias,
            garmentIds = garmentIds,
            liveGenerate = liveGenerate
        )
    }

    suspend fun warm(
        context: Context,
        bodyUri: String?,
        alias: String?,
        garmentIds: List<String>,
        liveGenerate: Boolean = false
    ): Result = withContext(Dispatchers.IO) {
        val personKeys = buildList {
            if (!alias.isNullOrBlank()) add(alias)
            add(TryOnCompositeCache.personKey(context, bodyUri))
        }.distinct()
        var warmed = 0
        val misses = mutableListOf<String>()
        for (garmentId in garmentIds) {
            if (TryOnCompositeCache.load(context, personKeys, listOf(garmentId)) != null) {
                warmed++
                continue
            }
            val seeded = !alias.isNullOrBlank() &&
                copyAssetAliasIfPresent(context, alias, garmentId) &&
                TryOnCompositeCache.load(context, personKeys, listOf(garmentId)) != null
            if (seeded) {
                warmed++
                continue
            }
            if (liveGenerate && bodyUri != null) {
                val ok = runCatching { liveTryOn(context, bodyUri, alias, garmentId) }.getOrDefault(false)
                if (ok) {
                    warmed++
                    continue
                }
            }
            misses += garmentId
        }
        Log.i(TAG, "warm alias=$alias warmed=$warmed misses=$misses live=$liveGenerate")
        Result(warmed = warmed, misses = misses, alias = alias)
    }

    private suspend fun liveTryOn(
        context: Context,
        bodyUri: String,
        alias: String?,
        garmentId: String
    ): Boolean {
        val garment = LookWeaveRepository.allGarments.firstOrNull { it.id == garmentId } ?: return false
        val garmentUri = resolveGarmentUri(context, garment) ?: return false
        val category = when (garment.category) {
            GarmentCategory.SHOES -> "shoes"
            GarmentCategory.ACCESSORY -> "auto"
            GarmentCategory.OUTERWEAR -> "outer"
            GarmentCategory.BOTTOM -> "lower_body"
            GarmentCategory.TOP -> "upper_body"
        }
        val task = LookWeaveGateway.createPreview(
            context.contentResolver,
            Uri.parse(bodyUri),
            garmentUri,
            category
        )
        val bytes = LookWeaveGateway.download(task.imageUrl)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return false
        TryOnCompositeCache.put(context, bodyUri, listOf(garmentId), bitmap)
        if (TryOnCompositeCache.isBuiltInPersonAlias(alias)) {
            TryOnCompositeCache.put(
                context,
                TryOnCompositeCache.cacheKey(alias!!, listOf(garmentId)),
                bitmap
            )
        }
        return true
    }

    private fun copyAssetAliasIfPresent(context: Context, alias: String, garmentId: String): Boolean {
        val key = TryOnCompositeCache.cacheKey(alias, listOf(garmentId))
        val dest = TryOnCompositeCache.diskFile(context, key)
        if (dest.exists() && dest.length() > 0) return true
        val assetName = TryOnCompositeCache.safeFileName(key)
        return runCatching {
            context.assets.open("youcam-cache/$assetName").use { input ->
                dest.parentFile?.mkdirs()
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            }
            dest.exists() && dest.length() > 0
        }.getOrDefault(false)
    }

    fun peekBitmap(context: Context, bodyUri: String?, garmentIds: List<String>) =
        TryOnCompositeCache.load(context, bodyUri, garmentIds)?.bitmap

    private fun resolveGarmentUri(context: Context, garment: Garment): Uri? {
        garment.imageUrl?.takeIf { it.isNotBlank() }?.let { return Uri.parse(it) }
        val res = when (garment.id) {
            "g1", "g13" -> R.drawable.garment_g1
            "g2", "g15" -> R.drawable.garment_g2
            "g3", "g17" -> R.drawable.garment_g3
            "g4" -> R.drawable.garment_g4
            "g5", "g18" -> R.drawable.garment_g5
            "g6" -> R.drawable.garment_g6
            "g7", "g14" -> R.drawable.garment_g7
            "g8", "g16" -> R.drawable.garment_g8
            "g9" -> R.drawable.garment_g9
            "g10" -> R.drawable.garment_g10
            "g11" -> R.drawable.garment_g11
            "g12" -> R.drawable.garment_g12
            else -> return null
        }
        return Uri.parse("android.resource://${context.packageName}/$res")
    }
}
