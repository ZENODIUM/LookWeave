package com.vizeno.loomie.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.vizeno.loomie.model.CalendarEvent
import com.vizeno.loomie.model.ClothingBudget
import com.vizeno.loomie.model.ClothingPurchase
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.GenderOptions
import com.vizeno.loomie.model.PlannedOutfit
import com.vizeno.loomie.model.RecurrenceRule
import com.vizeno.loomie.model.SizeLog
import com.vizeno.loomie.model.UserFilter
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.model.WearabilityRule
import com.vizeno.loomie.model.WearabilityRuleKey
import com.vizeno.loomie.model.WearabilityRuleKind
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object LocalProfileStore {
    private const val PREFS = "lookweave"
    private const val KEY_DISPLAY_NAME = "displayName"
    private const val KEY_PORTRAIT = "portraitUri"
    /** Full-body / zoomed-out photo for Fit studio (outfit, comfort map, try-on). */
    private const val KEY_BODY = "bodyUri"
    /** Face / style analysis portrait — never used as Fit body stage by default. */
    private const val KEY_FACE = "faceUri"
    private const val KEY_BUDGET = "clothingBudget"
    private const val KEY_EVENTS = "calendarEvents"
    private const val KEY_PLANS = "plannedOutfits"
    private const val KEY_PRICE_LIMIT = "priceLimit"
    private const val KEY_SIZES = "sizeLog"
    private const val KEY_GENDER = "gender"
    private const val KEY_GENDER_CUSTOM = "genderCustom"
    /** "person1".."person6" when body came from a bundled sample; cleared on user upload. */
    private const val KEY_BODY_SOURCE = "bodySource"
    private const val KEY_ACTIVE_PROFILE = "activeProfileId"
    private const val KEY_CUSTOM_PROFILES = "customProfiles"
    private const val KEY_POSTAL = "postalCode"
    private const val KEY_COLORS = "preferredColors"
    private const val KEY_AVOID_COLORS = "avoidColors"
    private const val KEY_STYLES = "preferredStyles"
    private const val KEY_INTERESTS = "interests"
    private const val KEY_STYLE_CHOICES = "styleChoices"
    private const val KEY_WEARABILITY_SEED = "wearabilitySeed"
    private const val KEY_SAVED_OCCASIONS = "savedOccasions"
    private const val KEY_PERSONA_META = "personaMeta"
    private const val KEY_APPLIED_LOOKS = "appliedLooks"
    private const val KEY_LIKED_PRODUCTS = "likedProductIdsLocal"
    private const val KEY_PASSED_PRODUCTS = "passedProductIdsLocal"
    private const val KEY_PRODUCT_META = "productDecisionMeta"
    private const val KEY_AGE = "personaAge"
    private const val KEY_OCCUPATION = "personaOccupation"
    private const val KEY_PERSONA_SUMMARY = "personaSummary"
    private const val KEY_EXAMPLE_REQUEST = "personaExampleRequest"
    private const val KEY_BUILT_IN_NAME_REPAIR = "builtInNameRepairV1"

    fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(PREFS, 0)

    fun activeProfileId(context: Context): String? =
        prefs(context).getString(KEY_ACTIVE_PROFILE, null)?.takeIf { it.isNotBlank() }

    fun saveActiveProfileId(context: Context, id: String?) {
        prefs(context).edit().apply {
            if (id.isNullOrBlank()) remove(KEY_ACTIVE_PROFILE) else putString(KEY_ACTIVE_PROFILE, id)
        }.apply()
    }

    fun bodySource(context: Context): String? =
        prefs(context).getString(KEY_BODY_SOURCE, null)?.takeIf { it.isNotBlank() }

    fun saveBodySource(context: Context, source: String?) {
        prefs(context).edit().apply {
            if (source.isNullOrBlank()) remove(KEY_BODY_SOURCE) else putString(KEY_BODY_SOURCE, source)
        }.apply()
    }

    fun displayName(context: Context): String {
        val active = activeProfileId(context)
        if (!active.isNullOrBlank()) {
            val custom = prefs(context).getString("displayName_$active", null)
            if (!custom.isNullOrBlank()) return custom
        }
        return prefs(context).getString(KEY_DISPLAY_NAME, "") ?: ""
    }

    fun saveDisplayName(context: Context, name: String) {
        val trimmed = name.trim()
        val active = activeProfileId(context)
        prefs(context).edit().apply {
            putString(KEY_DISPLAY_NAME, trimmed)
            if (!active.isNullOrBlank()) {
                putString("displayName_$active", trimmed)
            }
        }.apply()
    }

    fun displayNameForProfile(context: Context, profileId: String): String? =
        prefs(context).getString("displayName_$profileId", null)?.takeIf { it.isNotBlank() }

    fun saveDisplayNameForProfile(context: Context, profileId: String, name: String) {
        prefs(context).edit().putString("displayName_$profileId", name.trim()).apply()
    }

    /**
     * Older builds wrote an incoming built-in persona name against the outgoing
     * profile during a switch. These six names are demo identities, so clear only
     * their leaked overrides once; custom-profile names are never touched.
     */
    fun repairLegacyBuiltInNameLeak(context: Context): Boolean {
        val store = prefs(context)
        if (store.getBoolean(KEY_BUILT_IN_NAME_REPAIR, false)) return false
        val editor = store.edit()
        (1..6).forEach { editor.remove("displayName_person$it") }
        editor.putBoolean(KEY_BUILT_IN_NAME_REPAIR, true).apply()
        return true
    }

    /** @deprecated Prefer [faceUri] for Face tab; kept for profile avatar / migration. */
    fun portraitUri(context: Context): String? =
        prefs(context).getString(KEY_FACE, null)
            ?: prefs(context).getString(KEY_PORTRAIT, null)

    fun savePortraitUri(context: Context, uri: String?) {
        prefs(context).edit()
            .putString(KEY_PORTRAIT, uri)
            .putString(KEY_FACE, uri)
            .apply()
    }

    fun faceUri(context: Context): String? =
        prefs(context).getString(KEY_FACE, null)
            ?: prefs(context).getString(KEY_PORTRAIT, null)

    fun saveFaceUri(context: Context, uri: String?) {
        prefs(context).edit()
            .putString(KEY_FACE, uri)
            .putString(KEY_PORTRAIT, uri)
            .apply()
    }

    fun bodyUri(context: Context): String? = prefs(context).getString(KEY_BODY, null)

    fun saveBodyUri(context: Context, uri: String?) {
        prefs(context).edit().putString(KEY_BODY, uri).apply()
    }

    /** Persist body and mark as user-uploaded (invalidates built-in alias + prior person try-on cache). */
    fun saveUserBodyUri(context: Context, uri: String?, previousBodyUri: String? = bodyUri(context)) {
        val priorKey = TryOnCompositeCache.personKey(context, previousBodyUri)
        val priorSource = bodySource(context)
        saveBodyUri(context, uri)
        saveBodySource(context, null)
        TryOnCompositeCache.invalidatePerson(context, priorKey)
        if (TryOnCompositeCache.isBuiltInPersonAlias(priorSource)) {
            TryOnCompositeCache.invalidatePerson(context, priorSource)
        }
    }

    fun genderPreset(context: Context): String =
        prefs(context).getString(KEY_GENDER, "")?.takeIf { it.isNotBlank() }.orEmpty()

    fun genderCustom(context: Context): String =
        prefs(context).getString(KEY_GENDER_CUSTOM, "")?.takeIf { it.isNotBlank() }.orEmpty()

    /** Resolved label for agent / discovery (custom text when preset is Custom gender). */
    fun genderLabel(context: Context): String? {
        val preset = genderPreset(context)
        if (preset.isBlank()) return null
        if (preset == GenderOptions.CUSTOM) {
            return genderCustom(context).takeIf { it.isNotBlank() } ?: GenderOptions.CUSTOM
        }
        return preset
    }

    fun saveGender(context: Context, preset: String, custom: String = genderCustom(context)) {
        prefs(context).edit()
            .putString(KEY_GENDER, preset.trim())
            .putString(KEY_GENDER_CUSTOM, custom.trim())
            .apply()
    }

    fun postalCode(context: Context): String =
        prefs(context).getString(KEY_POSTAL, "")?.takeIf { it.isNotBlank() }.orEmpty()

    fun savePostalCode(context: Context, zip: String) {
        prefs(context).edit().putString(KEY_POSTAL, zip.filter(Char::isDigit).take(5)).apply()
    }

    fun preferredColors(context: Context): List<String> = loadStringList(context, KEY_COLORS)

    fun savePreferredColors(context: Context, colors: List<String>) {
        saveStringList(context, KEY_COLORS, colors)
    }

    fun avoidColors(context: Context): List<String> = loadStringList(context, KEY_AVOID_COLORS)

    fun saveAvoidColors(context: Context, colors: List<String>) {
        saveStringList(context, KEY_AVOID_COLORS, colors)
    }

    data class PersonaMeta(
        val age: Int? = null,
        val occupation: String = "",
        val summary: String = "",
        val dayNotes: List<String> = emptyList(),
        val loomiePrompts: List<String> = emptyList()
    )

    fun personaMeta(context: Context): PersonaMeta {
        val raw = prefs(context).getString(KEY_PERSONA_META, null) ?: return PersonaMeta()
        return runCatching {
            val json = JSONObject(raw)
            PersonaMeta(
                age = json.optInt("age").takeIf { json.has("age") && it > 0 },
                occupation = json.optString("occupation"),
                summary = json.optString("summary"),
                dayNotes = json.optJSONArray("dayNotes")?.let { arr ->
                    (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
                }.orEmpty(),
                loomiePrompts = json.optJSONArray("loomiePrompts")?.let { arr ->
                    (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
                }.orEmpty()
            )
        }.getOrDefault(PersonaMeta())
    }

    fun savePersonaMeta(
        context: Context,
        age: Int?,
        occupation: String,
        summary: String,
        dayNotes: List<String>,
        loomiePrompts: List<String>
    ) {
        val json = JSONObject()
            .put("age", age ?: 0)
            .put("occupation", occupation)
            .put("summary", summary)
            .put("dayNotes", JSONArray(dayNotes))
            .put("loomiePrompts", JSONArray(loomiePrompts))
        prefs(context).edit().putString(KEY_PERSONA_META, json.toString()).apply()
    }

    fun preferredStyles(context: Context): List<String> = loadStringList(context, KEY_STYLES)

    fun savePreferredStyles(context: Context, styles: List<String>) {
        saveStringList(context, KEY_STYLES, styles)
    }

    fun interests(context: Context): List<String> = loadStringList(context, KEY_INTERESTS)

    fun saveInterests(context: Context, interests: List<String>) {
        saveStringList(context, KEY_INTERESTS, interests)
    }

    fun age(context: Context): Int? =
        prefs(context).getInt(KEY_AGE, -1).takeIf { it > 0 }
            ?: personaMeta(context).age

    fun saveAge(context: Context, age: Int?) {
        prefs(context).edit().apply {
            if (age == null || age <= 0) remove(KEY_AGE) else putInt(KEY_AGE, age)
        }.apply()
        val meta = personaMeta(context)
        savePersonaMeta(context, age, meta.occupation, meta.summary, meta.dayNotes, meta.loomiePrompts)
    }

    fun occupation(context: Context): String =
        prefs(context).getString(KEY_OCCUPATION, "")?.takeIf { it.isNotBlank() }
            ?: personaMeta(context).occupation

    fun saveOccupation(context: Context, value: String) {
        prefs(context).edit().putString(KEY_OCCUPATION, value.trim()).apply()
        val meta = personaMeta(context)
        savePersonaMeta(context, meta.age ?: age(context), value, meta.summary, meta.dayNotes, meta.loomiePrompts)
    }

    fun personaSummary(context: Context): String =
        prefs(context).getString(KEY_PERSONA_SUMMARY, "")?.takeIf { it.isNotBlank() }
            ?: personaMeta(context).summary

    fun savePersonaSummary(context: Context, value: String) {
        prefs(context).edit().putString(KEY_PERSONA_SUMMARY, value.trim()).apply()
        val meta = personaMeta(context)
        savePersonaMeta(context, meta.age ?: age(context), meta.occupation.ifBlank { occupation(context) }, value, meta.dayNotes, meta.loomiePrompts)
    }

    fun exampleRequest(context: Context): String =
        prefs(context).getString(KEY_EXAMPLE_REQUEST, "")?.takeIf { it.isNotBlank() }
            ?: personaMeta(context).loomiePrompts.firstOrNull().orEmpty()

    fun saveExampleRequest(context: Context, value: String) {
        prefs(context).edit().putString(KEY_EXAMPLE_REQUEST, value.trim()).apply()
        val meta = personaMeta(context)
        val prompts = if (value.isBlank()) meta.loomiePrompts else listOf(value.trim()) + meta.loomiePrompts.filterNot { it == value.trim() }
        savePersonaMeta(context, meta.age ?: age(context), meta.occupation.ifBlank { occupation(context) }, meta.summary.ifBlank { personaSummary(context) }, meta.dayNotes, prompts)
    }

    fun styleChoices(context: Context): Map<String, String> {
        val raw = prefs(context).getString(KEY_STYLE_CHOICES, null) ?: return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            buildMap {
                json.keys().forEach { key ->
                    val value = json.optString(key)
                    if (value.isNotBlank() && value != "null") put(key, value)
                }
            }
        }.getOrDefault(emptyMap())
    }

    fun saveStyleChoice(context: Context, key: String, value: String) {
        val next = styleChoices(context).toMutableMap()
        if (value.isBlank()) next.remove(key) else next[key] = value.trim()
        saveStyleChoices(context, next)
    }

    fun saveStyleChoices(context: Context, choices: Map<String, String>) {
        val json = JSONObject()
        choices.forEach { (k, v) -> if (v.isNotBlank()) json.put(k, v) }
        prefs(context).edit().putString(KEY_STYLE_CHOICES, json.toString()).apply()
    }

    fun wearabilitySeed(context: Context): WearabilityPreferences? {
        val raw = prefs(context).getString(scopedKey(context, KEY_WEARABILITY_SEED), null)
            ?: prefs(context).getString(KEY_WEARABILITY_SEED, null)
            ?: return null
        return runCatching {
            val json = JSONObject(raw)
            val filters = json.optJSONArray("userFilters")?.let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    val item = arr.optJSONObject(i) ?: return@mapNotNull null
                    UserFilter(
                        filterId = item.optString("filterId"),
                        mode = runCatching { FilterMode.valueOf(item.optString("mode", "MUST_AVOID")) }
                            .getOrDefault(FilterMode.MUST_AVOID),
                        condition = item.optString("condition").takeIf { it.isNotBlank() && it != "null" },
                        enabled = item.optBoolean("enabled", true)
                    ).takeIf { it.filterId.isNotBlank() }
                }
            }.orEmpty()
            val rules = json.optJSONArray("customRules")?.let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    val item = arr.optJSONObject(i) ?: return@mapNotNull null
                    WearabilityRule(
                        id = item.optString("id").ifBlank { "rule-$i" },
                        label = item.optString("label"),
                        key = runCatching {
                            WearabilityRuleKey.valueOf(item.optString("key", "LOW_SENSORY_LOAD"))
                        }.getOrDefault(WearabilityRuleKey.LOW_SENSORY_LOAD),
                        kind = runCatching {
                            WearabilityRuleKind.valueOf(item.optString("kind", "HARD"))
                        }.getOrDefault(WearabilityRuleKind.HARD),
                        enabled = item.optBoolean("enabled", true),
                        confirmed = item.optBoolean("confirmed", true),
                        source = item.optString("source", "Persona seed")
                    ).takeIf { it.label.isNotBlank() }
                }
            }.orEmpty()
            WearabilityPreferences(
                oneHandClosure = json.optBoolean("oneHandClosure", false),
                seatedComfort = json.optBoolean("seatedComfort", false),
                lowSensoryLoad = json.optBoolean("lowSensoryLoad", false),
                deviceClearance = json.optBoolean("deviceClearance", false),
                easyFootwear = json.optBoolean("easyFootwear", false),
                avoidWool = json.optBoolean("avoidWool", false),
                animalFree = json.optBoolean("animalFree", false),
                stepFreeAccess = json.optBoolean("stepFreeAccess", false),
                conserveEnergy = json.optBoolean("conserveEnergy", false),
                heatSensitive = json.optBoolean("heatSensitive", false),
                limitedOverheadReach = json.optBoolean("limitedOverheadReach", false),
                gripStrength = json.optBoolean("gripStrength", false),
                seamPressureSensitivity = json.optBoolean("seamPressureSensitivity", false),
                easyRemoval = json.optBoolean("easyRemoval", false),
                mobilityAidCompatibility = json.optBoolean("mobilityAidCompatibility", false),
                userFilters = filters,
                customRules = rules
            )
        }.getOrNull()
    }

    fun saveWearabilitySeed(context: Context, preferences: WearabilityPreferences) {
        val json = JSONObject()
            .put("oneHandClosure", preferences.oneHandClosure)
            .put("seatedComfort", preferences.seatedComfort)
            .put("lowSensoryLoad", preferences.lowSensoryLoad)
            .put("deviceClearance", preferences.deviceClearance)
            .put("easyFootwear", preferences.easyFootwear)
            .put("avoidWool", preferences.avoidWool)
            .put("animalFree", preferences.animalFree)
            .put("stepFreeAccess", preferences.stepFreeAccess)
            .put("conserveEnergy", preferences.conserveEnergy)
            .put("heatSensitive", preferences.heatSensitive)
            .put("limitedOverheadReach", preferences.limitedOverheadReach)
            .put("gripStrength", preferences.gripStrength)
            .put("seamPressureSensitivity", preferences.seamPressureSensitivity)
            .put("easyRemoval", preferences.easyRemoval)
            .put("mobilityAidCompatibility", preferences.mobilityAidCompatibility)
            .put("userFilters", JSONArray(preferences.userFilters.map { filter ->
                JSONObject()
                    .put("filterId", filter.filterId)
                    .put("mode", filter.mode.name)
                    .put("condition", filter.condition)
                    .put("enabled", filter.enabled)
            }))
            .put("customRules", JSONArray(preferences.customRules.map { rule ->
                JSONObject()
                    .put("id", rule.id)
                    .put("label", rule.label)
                    .put("key", rule.key.name)
                    .put("kind", rule.kind.name)
                    .put("enabled", rule.enabled)
                    .put("confirmed", rule.confirmed)
                    .put("source", rule.source)
            }))
        prefs(context).edit()
            .putString(scopedKey(context, KEY_WEARABILITY_SEED), json.toString())
            .putString(KEY_WEARABILITY_SEED, json.toString())
            .apply()
    }

    /** True only when this profile has its own saved wearability state (no legacy fallback). */
    fun hasScopedWearabilitySeed(context: Context): Boolean =
        prefs(context).contains(scopedKey(context, KEY_WEARABILITY_SEED))

    fun savedOccasions(context: Context): List<String> =
        loadStringList(context, scopedKey(context, KEY_SAVED_OCCASIONS))
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinctBy(String::lowercase)

    fun saveOccasion(context: Context, occasion: String) {
        val clean = occasion.trim().take(80)
        if (clean.isBlank()) return
        val next = (savedOccasions(context) + clean)
            .distinctBy(String::lowercase)
            .takeLast(12)
        saveStringList(context, scopedKey(context, KEY_SAVED_OCCASIONS), next)
    }

    /** Personalized discover query fragments from gender + ZIP + colors + interests. */
    fun discoverQueryHints(context: Context): String {
        val gender = genderLabel(context)
        val colors = preferredColors(context).take(3)
        val avoid = avoidColors(context).take(2)
        val styles = preferredStyles(context).take(2)
        val interest = interests(context).take(3)
        val zip = postalCode(context)
        val meta = personaMeta(context)
        return buildList {
            gender?.takeIf { it.isNotBlank() && it != GenderOptions.PREFER_NOT }?.let { add(it) }
            meta.occupation.takeIf { it.isNotBlank() }?.let { add(it) }
            colors.forEach { add(it) }
            styles.forEach { add(it) }
            interest.forEach { add(it) }
            avoid.forEach { add("avoid $it") }
            if (zip.length == 5) add("near $zip")
            priceLimit(context)?.let { add("under $$it") }
        }.joinToString(" ")
    }

    data class AppliedLook(
        val id: String,
        val title: String,
        val garmentIds: List<String>,
        val occasion: String,
        val savedAtEpochDay: Long
    )

    fun loadAppliedLooks(context: Context): List<AppliedLook> {
        val raw = prefs(context).getString(scopedKey(context, KEY_APPLIED_LOOKS), null) ?: return emptyList()
        return runCatching {
            val values = JSONArray(raw)
            (0 until values.length()).mapNotNull { index ->
                val item = values.optJSONObject(index) ?: return@mapNotNull null
                val garmentIds = item.optJSONArray("garmentIds") ?: JSONArray()
                AppliedLook(
                    id = item.optString("id"),
                    title = item.optString("title"),
                    garmentIds = (0 until garmentIds.length()).map { garmentIds.getString(it) },
                    occasion = item.optString("occasion"),
                    savedAtEpochDay = item.optLong("savedAtEpochDay")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveAppliedLook(context: Context, look: AppliedLook) {
        val next = (listOf(look) + loadAppliedLooks(context).filterNot { it.id == look.id }).take(12)
        replaceAppliedLooks(context, next)
    }

    fun replaceAppliedLooks(context: Context, looks: List<AppliedLook>) {
        val values = JSONArray(looks.take(12).map { item ->
            JSONObject()
                .put("id", item.id)
                .put("title", item.title)
                .put("garmentIds", JSONArray(item.garmentIds))
                .put("occasion", item.occasion)
                .put("savedAtEpochDay", item.savedAtEpochDay)
        })
        prefs(context).edit().putString(scopedKey(context, KEY_APPLIED_LOOKS), values.toString()).apply()
    }

    fun likedProductIds(context: Context): Set<String> =
        loadStringList(context, scopedKey(context, KEY_LIKED_PRODUCTS)).toSet()

    fun passedProductIds(context: Context): Set<String> =
        loadStringList(context, scopedKey(context, KEY_PASSED_PRODUCTS)).toSet()

    fun saveProductDecision(context: Context, productId: String, liked: Boolean, meta: JSONObject? = null) {
        val likedIds = likedProductIds(context).toMutableSet()
        val passedIds = passedProductIds(context).toMutableSet()
        if (liked) {
            likedIds += productId
            passedIds -= productId
        } else {
            passedIds += productId
            likedIds -= productId
        }
        saveStringList(context, scopedKey(context, KEY_LIKED_PRODUCTS), likedIds.toList())
        saveStringList(context, scopedKey(context, KEY_PASSED_PRODUCTS), passedIds.toList())
        if (meta != null) {
            val store = runCatching {
                JSONObject(prefs(context).getString(scopedKey(context, KEY_PRODUCT_META), "{}") ?: "{}")
            }.getOrDefault(JSONObject())
            store.put(productId, meta)
            prefs(context).edit().putString(scopedKey(context, KEY_PRODUCT_META), store.toString()).apply()
        }
    }

    fun replaceProductDecisions(context: Context, liked: Set<String>, passed: Set<String>) {
        saveStringList(context, scopedKey(context, KEY_LIKED_PRODUCTS), liked.toList())
        saveStringList(context, scopedKey(context, KEY_PASSED_PRODUCTS), passed.toList())
    }

    fun clearProductDecisions(context: Context) {
        prefs(context).edit()
            .remove(scopedKey(context, KEY_LIKED_PRODUCTS))
            .remove(scopedKey(context, KEY_PASSED_PRODUCTS))
            .remove(scopedKey(context, KEY_PRODUCT_META))
            .apply()
    }

    fun productMeta(context: Context): JSONObject = runCatching {
        JSONObject(prefs(context).getString(scopedKey(context, KEY_PRODUCT_META), "{}") ?: "{}")
    }.getOrDefault(JSONObject())

    private fun scopedKey(context: Context, base: String): String {
        val profile = activeProfileId(context)?.takeIf { it.isNotBlank() } ?: "default"
        return "$base::$profile"
    }

    private fun loadStringList(context: Context, key: String): List<String> {
        val raw = prefs(context).getString(key, null) ?: return emptyList()
        return runCatching {
            val values = JSONArray(raw)
            (0 until values.length()).mapNotNull { values.optString(it).takeIf { s -> s.isNotBlank() } }
        }.getOrDefault(emptyList())
    }

    private fun saveStringList(context: Context, key: String, values: List<String>) {
        prefs(context).edit().putString(key, JSONArray(values).toString()).apply()
    }

    /**
     * Decode any gallery URI (HEIC/WebP/content) and persist as JPEG so gateway/YouCam accept it.
     */
    private fun copyUriAsJpeg(context: Context, sourceUri: Uri, dest: File): String? {
        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null
        FileOutputStream(dest).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) return null
        }
        if (!bitmap.isRecycled) bitmap.recycle()
        return Uri.fromFile(dest).toString()
    }

    fun copyPortraitToAppFiles(context: Context, sourceUri: Uri): String? = runCatching {
        val dest = File(context.filesDir, "portrait.jpg")
        val value = copyUriAsJpeg(context, sourceUri, dest) ?: return null
        saveFaceUri(context, value)
        value
    }.getOrNull()

    fun copyBodyToAppFiles(context: Context, sourceUri: Uri): String? = runCatching {
        val previous = bodyUri(context)
        val dest = File(context.filesDir, "body.jpg")
        val value = copyUriAsJpeg(context, sourceUri, dest) ?: return null
        saveUserBodyUri(context, value, previous)
        value
    }.getOrNull()

    fun copyFaceToAppFiles(context: Context, sourceUri: Uri): String? = runCatching {
        val dest = File(context.filesDir, "face.jpg")
        val value = copyUriAsJpeg(context, sourceUri, dest) ?: return null
        saveFaceUri(context, value)
        value
    }.getOrNull()

    /** Persist a wardrobe garment photo into app files; returns a file:// URI. */
    fun copyGarmentImageToAppFiles(context: Context, sourceUri: Uri, garmentId: String): String? = runCatching {
        val safe = garmentId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(48)
        val dest = File(context.filesDir, "garment-$safe.jpg")
        copyUriAsJpeg(context, sourceUri, dest)
    }.getOrNull()

    /** Cache a try-on composite (delegates to [TryOnCompositeCache] memory + disk). */
    fun saveTryOnComposite(context: Context, bitmap: Bitmap, cacheKey: String): String? =
        TryOnCompositeCache.put(context, cacheKey, bitmap)

    fun loadTryOnComposite(context: Context, cacheKey: String): File? =
        TryOnCompositeCache.diskFile(context, cacheKey).takeIf { it.exists() && it.length() > 0 }

    fun priceLimit(context: Context): Double? {
        val raw = prefs(context).getString(KEY_PRICE_LIMIT, null) ?: return null
        return raw.toDoubleOrNull()?.takeIf { it >= 0 }
    }

    fun savePriceLimit(context: Context, limit: Double?) {
        prefs(context).edit().putString(KEY_PRICE_LIMIT, limit?.toString()).apply()
    }

    fun loadSizes(context: Context): SizeLog {
        val raw = prefs(context).getString(KEY_SIZES, null) ?: return SizeLog()
        return runCatching {
            val json = JSONObject(raw)
            val measures = json.optJSONObject("measurements")
            val measureMap = buildMap {
                if (measures != null) {
                    measures.keys().forEach { key ->
                        val value = measures.optString(key)
                        if (value.isNotBlank()) put(key, value)
                    }
                }
            }
            SizeLog(
                tops = json.optString("tops"),
                tShirt = json.optString("tShirt"),
                pants = json.optString("pants"),
                shoes = json.optString("shoes"),
                dress = json.optString("dress"),
                outerwear = json.optString("outerwear"),
                notes = json.optString("notes"),
                measurements = measureMap
            )
        }.getOrDefault(SizeLog())
    }

    fun saveSizes(context: Context, sizes: SizeLog) {
        val json = JSONObject()
            .put("tops", sizes.tops)
            .put("tShirt", sizes.tShirt)
            .put("pants", sizes.pants)
            .put("shoes", sizes.shoes)
            .put("dress", sizes.dress)
            .put("outerwear", sizes.outerwear)
            .put("notes", sizes.notes)
            .put("measurements", JSONObject().apply {
                sizes.measurements.forEach { (key, value) ->
                    if (value.isNotBlank()) put(key, value)
                }
            })
        prefs(context).edit().putString(KEY_SIZES, json.toString()).apply()
    }

    fun loadBudget(context: Context): ClothingBudget {
        val raw = prefs(context).getString(KEY_BUDGET, null) ?: return ClothingBudget()
        return runCatching {
            val json = JSONObject(raw)
            val purchases = json.optJSONArray("purchases") ?: JSONArray()
            ClothingBudget(
                monthlyLimit = json.optDouble("monthlyLimit", 200.0),
                currency = json.optString("currency", "USD"),
                purchases = (0 until purchases.length()).mapNotNull { index ->
                    val item = purchases.optJSONObject(index) ?: return@mapNotNull null
                    ClothingPurchase(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        amount = item.optDouble("amount"),
                        dateEpochDay = item.optLong("dateEpochDay"),
                        garmentId = item.optString("garmentId").takeIf { it.isNotBlank() && it != "null" }
                    )
                }
            )
        }.getOrDefault(ClothingBudget())
    }

    fun saveBudget(context: Context, budget: ClothingBudget) {
        val json = JSONObject()
            .put("monthlyLimit", budget.monthlyLimit)
            .put("currency", budget.currency)
            .put("purchases", JSONArray(budget.purchases.map { purchase ->
                JSONObject()
                    .put("id", purchase.id)
                    .put("name", purchase.name)
                    .put("amount", purchase.amount)
                    .put("dateEpochDay", purchase.dateEpochDay)
                    .put("garmentId", purchase.garmentId)
            }))
        prefs(context).edit().putString(KEY_BUDGET, json.toString()).apply()
    }

    fun loadEvents(context: Context): List<CalendarEvent> {
        val raw = prefs(context).getString(KEY_EVENTS, null) ?: return emptyList()
        return runCatching {
            val values = JSONArray(raw)
            (0 until values.length()).mapNotNull { index ->
                val item = values.optJSONObject(index) ?: return@mapNotNull null
                val garmentIds = item.optJSONArray("garmentIds") ?: JSONArray()
                CalendarEvent(
                    id = item.optString("id"),
                    title = item.optString("title"),
                    dateEpochDay = item.optLong("dateEpochDay"),
                    recurrence = runCatching { RecurrenceRule.valueOf(item.optString("recurrence", "NONE")) }.getOrDefault(RecurrenceRule.NONE),
                    notes = item.optString("notes"),
                    garmentIds = (0 until garmentIds.length()).map { garmentIds.getString(it) },
                    outfitOccasion = item.optString("outfitOccasion").takeIf { it.isNotBlank() && it != "null" }
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveEvents(context: Context, events: List<CalendarEvent>) {
        val values = JSONArray(events.map { event ->
            JSONObject()
                .put("id", event.id)
                .put("title", event.title)
                .put("dateEpochDay", event.dateEpochDay)
                .put("recurrence", event.recurrence.name)
                .put("notes", event.notes)
                .put("garmentIds", JSONArray(event.garmentIds))
                .put("outfitOccasion", event.outfitOccasion)
        })
        prefs(context).edit().putString(KEY_EVENTS, values.toString()).apply()
    }

    fun loadPlannedOutfits(context: Context): List<PlannedOutfit> {
        val raw = prefs(context).getString(KEY_PLANS, null) ?: return emptyList()
        return runCatching {
            val values = JSONArray(raw)
            (0 until values.length()).mapNotNull { index ->
                val item = values.optJSONObject(index) ?: return@mapNotNull null
                val garmentIds = item.optJSONArray("garmentIds") ?: JSONArray()
                PlannedOutfit(
                    eventId = item.optString("eventId").takeIf { it.isNotBlank() && it != "null" },
                    dateEpochDay = item.optLong("dateEpochDay"),
                    occasion = item.optString("occasion"),
                    garmentIds = (0 until garmentIds.length()).map { garmentIds.getString(it) },
                    note = item.optString("note")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun savePlannedOutfits(context: Context, plans: List<PlannedOutfit>) {
        val values = JSONArray(plans.map { plan ->
            JSONObject()
                .put("eventId", plan.eventId)
                .put("dateEpochDay", plan.dateEpochDay)
                .put("occasion", plan.occasion)
                .put("garmentIds", JSONArray(plan.garmentIds))
                .put("note", plan.note)
        })
        prefs(context).edit().putString(KEY_PLANS, values.toString()).apply()
    }

    /**
     * Demo seed: activate the active (or default Person 1) built-in sample when stores are empty
     * (or [force] is true). Delegates to [SamplePersonProfiles] so all 6 Input Images people work.
     */
    fun seedSamplePerson1IfNeeded(context: Context, force: Boolean = false): Boolean =
        SamplePersonProfiles.seedDefaultIfNeeded(context, force = force)

    /** Prefer raw byte copy for already-JPEG assets (stable SHA with offline seed script). */
    private fun copyAssetFile(context: Context, assetPath: String, dest: File): String? = runCatching {
        context.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        if (!dest.exists() || dest.length() <= 0) return null
        Uri.fromFile(dest).toString()
    }.getOrNull()

    private fun copyAssetImageToJpeg(context: Context, assetPath: String, dest: File): String? = runCatching {
        val bitmap = context.assets.open(assetPath).use { BitmapFactory.decodeStream(it) } ?: return null
        FileOutputStream(dest).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) return null
        }
        if (!bitmap.isRecycled) bitmap.recycle()
        Uri.fromFile(dest).toString()
    }.getOrNull()

    fun clearLocalIdentity(context: Context) {
        prefs(context).edit()
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_PORTRAIT)
            .remove(KEY_FACE)
            .remove(KEY_BODY)
            .remove(KEY_BODY_SOURCE)
            .remove(KEY_ACTIVE_PROFILE)
            .remove(KEY_BUDGET)
            .remove(KEY_EVENTS)
            .remove(KEY_PLANS)
            .remove(KEY_PRICE_LIMIT)
            .remove(KEY_SIZES)
            .remove(KEY_GENDER)
            .remove(KEY_GENDER_CUSTOM)
            .remove(KEY_POSTAL)
            .remove(KEY_COLORS)
            .remove(KEY_AVOID_COLORS)
            .remove(KEY_STYLES)
            .remove(KEY_INTERESTS)
            .remove(KEY_STYLE_CHOICES)
            .remove(KEY_WEARABILITY_SEED)
            .remove(KEY_SAVED_OCCASIONS)
            .remove(KEY_PERSONA_META)
            .apply()
        runCatching { File(context.filesDir, "portrait.jpg").delete() }
        runCatching { File(context.filesDir, "face.jpg").delete() }
        runCatching { File(context.filesDir, "body.jpg").delete() }
        runCatching { File(context.filesDir, "fit-person.jpg").delete() }
        TryOnCompositeCache.invalidateAll(context)
    }
}
