package com.vizeno.loomie.integration

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.vizeno.loomie.BuildConfig
import com.vizeno.loomie.model.FilterCatalog
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.UserFilter
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.model.WearabilityRule
import com.vizeno.loomie.model.WearabilityRuleKey
import com.vizeno.loomie.model.WearabilityRuleKind
import com.vizeno.loomie.integration.AgentAction
import com.vizeno.loomie.model.Evidence
import com.vizeno.loomie.model.EvidenceSource
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.model.GarmentCategory
import com.vizeno.loomie.model.NearbyPlace
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.Locale

class IntegrationException(message: String, val httpStatus: Int? = null) : Exception(message)

data class VoiceCredentials(
    val token: String,
    val conversationId: String?,
    val expiresInSeconds: Int
)

data class HourlySignal(
    val time: String,
    val temperatureF: Double?,
    val precipitationPercent: Double?,
    val windGustMph: Double?,
    val uvIndex: Double?,
    val usAqi: Double?
)

data class PreviewTask(val taskId: String, val imageUrl: String)

data class SharedLookWeaveState(
    val revision: Int,
    val preferences: WearabilityPreferences,
    val selectedOccasion: String,
    val updatedAt: String?,
    val likedPlaceIds: Set<String> = emptySet(),
    val passedPlaceIds: Set<String> = emptySet(),
    val importedGarments: List<Garment> = emptyList(),
    val likedProductIds: Set<String> = emptySet(),
    val passedProductIds: Set<String> = emptySet()
)

data class RuleDraft(val rule: WearabilityRule)

data class DaySignal(
    val label: String,
    val value: String
)

data class ContextRecommendation(
    val id: String,
    val title: String,
    val reason: String,
    val action: String,
    val constraint: String
)

data class DayContext(
    val location: String,
    val observedAt: String,
    val signals: List<DaySignal>,
    val recommendations: List<ContextRecommendation>,
    val hourly: List<HourlySignal> = emptyList(),
    val attribution: String,
    val departure: String = "00:00",
    val returnTime: String = "23:00"
)

data class NearbySearchResult(
    val postalCode: String,
    val area: String,
    val latitude: Double,
    val longitude: Double,
    val places: List<NearbyPlace>,
    val attribution: String,
    val privacy: String
)

data class ProductCard(
    val id: String,
    val name: String,
    val retailer: String,
    val productUrl: String,
    val imageUrl: String?,
    val match: Int,
    val reason: String,
    val evidence: String,
    val unknowns: List<String>,
    val source: String,
    val price: String? = null,
    val compatibility: String = "Needs review",
    val kind: String = "product"
)

data class AccessoryDiscovery(
    val subcategory: String,
    val products: List<ProductCard>,
    val eyewear: List<ProductCard> = emptyList(),
    val jewellery: List<ProductCard> = emptyList(),
    val subcategories: List<String> = listOf("glasses", "watches", "jewellery", "scarf", "hat", "shoes")
)

object LookWeaveGateway : FashionAgentService, VirtualTryOnService, VoiceAgentService {
    private val baseUrl = BuildConfig.LOOKWEAVE_BACKEND_URL.trimEnd('/')

    suspend fun styleProfile(): JSONObject = withContext(Dispatchers.IO) { getJson("/v1/style/profile", 15_000) }
    suspend fun saveStyle(choices: JSONObject): JSONObject = withContext(Dispatchers.IO) { postJson("/v1/style/profile", choices) }
    suspend fun draftRule(text: String): WearabilityRule = withContext(Dispatchers.IO) { parseRule(postJson("/v1/rules/draft", JSONObject().put("text", text)).getJSONObject("rule")) }
    suspend fun rules(): List<WearabilityRule> = withContext(Dispatchers.IO) { parseRules(getJson("/v1/rules", 8_000).optJSONArray("rules")) }
    suspend fun createRule(rule: WearabilityRule): WearabilityRule = withContext(Dispatchers.IO) {
        parseRule(postJson("/v1/rules", JSONObject().put("confirmed", true).put("rule", ruleJson(rule.copy(confirmed = true)))).getJSONObject("rule"))
    }
    suspend fun updateRule(rule: WearabilityRule): WearabilityRule = withContext(Dispatchers.IO) {
        parseRule(requestJson("/v1/rules/${URLEncoder.encode(rule.id, "UTF-8")}", "PUT", JSONObject().put("rule", ruleJson(rule.copy(confirmed = true))), 8_000).getJSONObject("rule"))
    }
    suspend fun deleteRule(id: String): List<WearabilityRule> = withContext(Dispatchers.IO) { parseRules(requestJson("/v1/rules/${URLEncoder.encode(id, "UTF-8")}", "DELETE", JSONObject(), 8_000).optJSONArray("rules")) }
    suspend fun deleteStyle(): JSONObject = withContext(Dispatchers.IO) { postJson("/v1/style/delete", JSONObject()) }
    suspend fun ingestProductUrl(url: String): JSONObject = withContext(Dispatchers.IO) { postJson("/v1/products/ingest", JSONObject().put("url", url)) }
    fun importedGarment(product: JSONObject): Garment {
        val category = when (product.optString("category").lowercase()) {
            "pant", "trouser", "jean", "skirt" -> GarmentCategory.BOTTOM
            "jacket", "blazer", "coat" -> GarmentCategory.OUTERWEAR
            "shoe", "sneaker", "loafer" -> GarmentCategory.SHOES
            "scarf", "necklace" -> GarmentCategory.ACCESSORY
            else -> GarmentCategory.TOP
        }
        val name = product.optString("name").ifBlank { "Imported garment" }.take(80)
        val material = product.optString("material").takeIf { it.isNotBlank() && it != "null" } ?: "Unknown"
        val closure = product.optString("closure").takeIf { it.isNotBlank() && it != "null" } ?: "Unknown"
        return Garment("import-${name.hashCode().toUInt().toString(16)}", name, product.optString("sourceUrl"), category, "Imported", Color(0xFF7C897E), Evidence(closure, EvidenceSource.PRODUCT, if (closure == "Unknown") 0 else 80), Evidence(material, EvidenceSource.PRODUCT, if (material == "Unknown") 0 else 80), 50, false, false, false, false, note = "Imported from a reviewed source. Price, stock, fit and accessibility remain unknown unless explicitly verified.", imageUrl = product.optJSONArray("images")?.optString(0)?.takeIf { it.isNotBlank() })
    }
    suspend fun styleJob(id: String): JSONObject = withContext(Dispatchers.IO) { getJson("/v1/style/jobs/${URLEncoder.encode(id, "UTF-8")}", 15_000) }
    suspend fun analyseStyle(resolver: ContentResolver, uri: Uri, mode: String): JSONObject = withContext(Dispatchers.IO) {
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth < 320 || bounds.outHeight < 320 || maxOf(bounds.outWidth, bounds.outHeight) > 4096) {
            throw IntegrationException("Choose a portrait with each side at least 320 pixels and no side above 4096 pixels.")
        }
        postJson("/v1/style/analyse", JSONObject().put("photo", encodeImage(resolver, uri, "Portrait")).put("mode", mode).put("consent", true))
    }

    override suspend fun respond(request: AgentRequest): AgentResponse = withContext(Dispatchers.IO) {
        val evidence = JSONArray().apply {
            request.garmentEvidence.forEach { item ->
                put(JSONObject().apply {
                    put("garmentId", item.garmentId)
                    put("facts", JSONObject(item.facts))
                    put("sources", JSONObject(item.sources))
                })
            }
        }
        val body = postJson("/v1/agent/respond", JSONObject().apply {
            put("message", request.message)
            put("garmentEvidence", evidence)
            put("hardConstraintIds", JSONArray(request.hardConstraintIds.toList()))
            request.dayContextNotes?.takeIf { it.isNotBlank() }?.let { put("dayContextNotes", it) }
            request.priceLimit?.let { put("priceLimit", it) }
            request.budgetRemaining?.let { put("budgetRemaining", it) }
            if (request.events.isNotEmpty()) put("events", JSONArray(request.events))
            if (request.preferences.isNotEmpty()) put("preferences", JSONObject(request.preferences.mapValues { (_, value) -> value ?: JSONObject.NULL }))
            if (request.style.isNotEmpty()) put("style", JSONObject(request.style))
            if (request.sizes.isNotEmpty()) put("sizes", JSONObject(request.sizes))
            request.gender?.takeIf { it.isNotBlank() }?.let { put("gender", it) }
            request.postalCode?.takeIf { it.isNotBlank() }?.let { put("postalCode", it) }
            if (request.wardrobe.isNotEmpty()) put("wardrobe", JSONArray(request.wardrobe))
            if (request.accessoriesPreferences.isNotEmpty()) put("accessoriesPreferences", JSONObject(request.accessoriesPreferences))
            if (request.discoveryLikes.isNotEmpty()) put("discoveryLikes", JSONArray(request.discoveryLikes))
            if (request.fitObservations.isNotEmpty()) put("fitObservations", JSONArray(request.fitObservations))
            if (request.activeFilters.isNotEmpty()) put("activeFilters", JSONArray(request.activeFilters))
            if (request.customRules.isNotEmpty()) put("customRules", JSONArray(request.customRules))
            if (request.purchases.isNotEmpty()) put("purchases", JSONArray(request.purchases))
            if (request.plannedOutfits.isNotEmpty()) put("plannedOutfits", JSONArray(request.plannedOutfits))
        }, readTimeoutMs = 90_000)
        val tools = body.optJSONArray("proposedToolCalls") ?: JSONArray()
        AgentResponse(
            message = cleanAgentMessage(body.optString("message")),
            proposedToolCalls = buildList {
                repeat(tools.length()) { index ->
                    val tool = tools.getJSONObject(index)
                    val arguments = tool.optJSONObject("arguments") ?: JSONObject()
                    add(ToolCall(tool.getString("name"), arguments.keys().asSequence().associateWith { arguments.optString(it) }))
                }
            },
            executedActions = buildList {
                val actions = body.optJSONArray("executedActions") ?: JSONArray()
                repeat(actions.length()) { index ->
                    val action = actions.getJSONObject(index)
                    add(AgentAction(action.optString("tool"), action.optString("status"), action.optString("title"), action.optString("detail")))
                }
            }
        )
    }

    private fun cleanAgentMessage(value: String): String {
        var clean = value.trim()
            .replace(Regex("^```(?:json|markdown|text)?\\s*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*```$", RegexOption.IGNORE_CASE), "")
            .trim()
        if (clean.startsWith("{") && clean.endsWith("}")) {
            clean = runCatching { JSONObject(clean).optString("message").ifBlank { clean } }.getOrDefault(clean)
        }
        return clean
            .replace(Regex("```(?:json|markdown|text)?", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^\\s*(?:message|answer)\\s*:\\s*", RegexOption.IGNORE_CASE), "")
            .trim()
            .take(1200)
            .ifBlank { "I prepared the useful next step below." }
    }

    suspend fun sharedState(): SharedLookWeaveState = withContext(Dispatchers.IO) {
        parseSharedState(getJson("/v1/sync/state", 8_000))
    }

    suspend fun nearbyPlaces(postalCode: String): NearbySearchResult = withContext(Dispatchers.IO) {
        val body = getJson("/v1/nearby?postalCode=${URLEncoder.encode(postalCode, Charsets.UTF_8.name())}", 80_000)
        val placesJson = body.optJSONArray("places") ?: JSONArray()
        val places = (0 until placesJson.length()).map { index ->
            val item = placesJson.getJSONObject(index)
            val features = item.optJSONArray("features") ?: JSONArray()
            val suggested = item.optJSONArray("suggestedPieces") ?: JSONArray()
            NearbyPlace(
                id = item.getString("id"),
                retailerId = item.optString("retailerId"),
                name = item.getString("name"),
                neighborhood = item.optString("neighborhood"),
                address = item.optString("address"),
                distanceMiles = item.optDouble("distanceMiles"),
                latitude = item.getDouble("latitude"),
                longitude = item.getDouble("longitude"),
                match = item.optInt("match"),
                reason = item.optString("reason"),
                features = (0 until features.length()).map { features.getString(it) },
                directionsUrl = item.optString("directionsUrl"),
                color = runCatching { Color(android.graphics.Color.parseColor(item.optString("color", "#60786F"))) }.getOrDefault(Color(0xFF60786F)),
                accessibility = item.optString("accessibility", "unknown").ifBlank { "unknown" },
                suggestedPieces = (0 until suggested.length()).map { suggested.getString(it) }
            )
        }
        NearbySearchResult(
            postalCode = body.optString("postalCode"), area = body.optString("area"),
            latitude = body.getDouble("latitude"), longitude = body.getDouble("longitude"), places = places,
            attribution = body.optString("attribution"), privacy = body.optString("privacy")
        )
    }

    suspend fun discoverProducts(
        preferences: WearabilityPreferences,
        query: String = "",
        priceLimit: Double? = null,
        gender: String? = null,
        sizes: Map<String, String> = emptyMap(),
        colors: List<String> = emptyList(),
        styles: List<String> = emptyList(),
        interests: List<String> = emptyList(),
        postalCode: String? = null
    ): List<ProductCard> = withContext(Dispatchers.IO) {
        val body = postJson("/v1/products/discover", JSONObject().apply {
            put("query", query)
            put("oneHandClosure", preferences.oneHandClosure)
            put("lowSensoryLoad", preferences.lowSensoryLoad)
            put("seatedComfort", preferences.seatedComfort)
            put("easyRemoval", preferences.easyRemoval)
            put("mobilityAidCompatibility", preferences.mobilityAidCompatibility)
            put("deviceClearance", preferences.deviceClearance)
            put("easyFootwear", preferences.easyFootwear)
            put("avoidWool", preferences.avoidWool)
            put("animalFree", preferences.animalFree)
            put("limitedOverheadReach", preferences.limitedOverheadReach)
            put("gripStrength", preferences.gripStrength)
            put("seamPressureSensitivity", preferences.seamPressureSensitivity)
            put("heatSensitive", preferences.heatSensitive)
            put("conserveEnergy", preferences.conserveEnergy)
            put("stepFreeAccess", preferences.stepFreeAccess)
            gender?.takeIf { it.isNotBlank() }?.let { put("gender", it) }
            if (sizes.isNotEmpty()) put("sizes", JSONObject(sizes))
            if (colors.isNotEmpty()) put("colors", JSONArray(colors))
            if (styles.isNotEmpty()) put("styles", JSONArray(styles))
            if (interests.isNotEmpty()) put("interests", JSONArray(interests))
            postalCode?.takeIf { it.length == 5 }?.let { put("postalCode", it) }
            put("customRules", JSONArray(preferences.customRules.filter { it.enabled && it.confirmed }.map { rule ->
                JSONObject().put("label", rule.label).put("enabled", true).put("confirmed", true)
            }))
            put("filters", JSONArray(preferences.userFilters.filter { it.enabled }.mapNotNull { filter ->
                val definition = FilterCatalog.byId(filter.filterId) ?: return@mapNotNull null
                JSONObject()
                    .put("id", filter.filterId)
                    .put("mode", filter.mode.name)
                    .put("label", definition.label)
                    .put("evaluable", definition.evaluable)
                    .put("condition", filter.condition)
            }))
            priceLimit?.let { put("priceLimit", it) }
        }, readTimeoutMs = 90_000)
        val values = body.optJSONArray("products") ?: JSONArray()
        (0 until values.length()).map { index ->
            val item = values.getJSONObject(index); val unknowns = item.optJSONArray("unknowns") ?: JSONArray()
            parseProductCard(item)
        }
    }

    /** Live Tavily-backed accessory discovery. Prefer [products]; eyewear/jewellery kept for older callers. */
    suspend fun discoverAccessories(
        choices: JSONObject,
        subcategory: String? = null
    ): AccessoryDiscovery = withContext(Dispatchers.IO) {
        val payload = JSONObject(choices.toString()).apply {
            if (!subcategory.isNullOrBlank()) put("subcategory", subcategory)
        }
        val body = postJson("/v1/style/accessories", payload, readTimeoutMs = 90_000)
        fun list(key: String) = (body.optJSONArray(key) ?: JSONArray()).let { values ->
            (0 until values.length()).map { parseProductCard(values.getJSONObject(it)) }
        }
        val products = list("products").ifEmpty { list("eyewear") + list("jewellery") + list("glasses") }
        AccessoryDiscovery(
            subcategory = body.optString("subcategory", subcategory ?: "mixed"),
            products = products,
            eyewear = list("eyewear").ifEmpty { list("glasses") },
            jewellery = list("jewellery"),
            subcategories = (body.optJSONArray("subcategories") ?: JSONArray()).let { values ->
                (0 until values.length()).map { values.getString(it) }
            }.ifEmpty {
                listOf("glasses", "watches", "jewellery", "scarf", "hat", "shoes")
            }
        )
    }

    private fun parseProductCard(item: JSONObject): ProductCard {
        val unknowns = item.optJSONArray("unknowns") ?: JSONArray()
        return ProductCard(
            id = item.optString("id"),
            name = item.optString("name"),
            retailer = item.optString("retailer"),
            productUrl = item.optString("productUrl"),
            imageUrl = item.optString("imageUrl").takeIf { it.isNotBlank() && it != "null" },
            match = item.optInt("match"),
            reason = item.optString("reason"),
            evidence = item.optString("evidence"),
            unknowns = (0 until unknowns.length()).map { unknowns.getString(it) },
            source = item.optString("source"),
            price = item.optString("price").takeIf { it.isNotBlank() && it != "null" },
            compatibility = item.optString("compatibility", "Needs review").ifBlank { "Needs review" },
            kind = item.optString("kind", "product").ifBlank { "product" }
        )
    }

    suspend fun saveSharedState(preferences: WearabilityPreferences, selectedOccasion: String): SharedLookWeaveState = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("preferences", JSONObject().apply {
                put("oneHandClosure", preferences.oneHandClosure)
                put("seatedComfort", preferences.seatedComfort)
                put("lowSensoryLoad", preferences.lowSensoryLoad)
                put("deviceClearance", preferences.deviceClearance)
                put("easyFootwear", preferences.easyFootwear)
                put("avoidWool", preferences.avoidWool)
                put("animalFree", preferences.animalFree)
                put("stepFreeAccess", preferences.stepFreeAccess)
                put("conserveEnergy", preferences.conserveEnergy)
                put("heatSensitive", preferences.heatSensitive)
                    put("limitedOverheadReach", preferences.limitedOverheadReach)
                    put("gripStrength", preferences.gripStrength)
                    put("seamPressureSensitivity", preferences.seamPressureSensitivity)
                    put("easyRemoval", preferences.easyRemoval)
                    put("mobilityAidCompatibility", preferences.mobilityAidCompatibility)
                    put("customRules", JSONArray(preferences.customRules.map { ruleJson(it) }))
                    put("userFilters", JSONArray(preferences.userFilters.map { filter ->
                        JSONObject()
                            .put("filterId", filter.filterId)
                            .put("mode", filter.mode.name)
                            .put("condition", filter.condition)
                            .put("enabled", filter.enabled)
                    }))
            })
            put("selectedOccasion", selectedOccasion)
            put("garments", JSONArray(com.vizeno.loomie.data.LookWeaveRepository.importedGarments.map { garmentJson(it) }))
        }
        parseSharedState(requestJson("/v1/sync/state", "PUT", body, 8_000))
    }

    suspend fun saveDiscoveryState(likedPlaceIds: Set<String>, passedPlaceIds: Set<String>, likedProductIds: Set<String> = emptySet(), passedProductIds: Set<String> = emptySet()): SharedLookWeaveState = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("likedPlaceIds", JSONArray(likedPlaceIds.toList()))
            put("passedPlaceIds", JSONArray(passedPlaceIds.toList()))
            put("likedProductIds", JSONArray(likedProductIds.toList()))
            put("passedProductIds", JSONArray(passedProductIds.toList()))
        }
        parseSharedState(requestJson("/v1/sync/state", "PUT", body, 8_000))
    }

    private fun parseSharedState(body: JSONObject): SharedLookWeaveState {
        val records = body.optJSONArray("observations") ?: JSONArray()
        val imported = parseGarments(body.optJSONArray("garments"))
        com.vizeno.loomie.data.LookWeaveRepository.importedGarments.clear()
        com.vizeno.loomie.data.LookWeaveRepository.importedGarments.addAll(imported)
        com.vizeno.loomie.data.LookWeaveRepository.observations = (0 until records.length()).map { index ->
            val item = records.getJSONObject(index)
            val task = item.optString("task")
            val region = item.optString("region").takeIf { it.isNotBlank() && it != "null" }
                ?: task.substringBefore(" · ", missingDelimiterValue = "").takeIf { it.isNotBlank() && it != task }
            com.vizeno.loomie.model.InteractionObservation(
                garmentId = item.optString("garmentId"),
                task = task,
                seconds = item.optInt("seconds"),
                difficulty = item.optString("difficulty"),
                note = "Confirmed fit note",
                region = region
            )
        }
        val preferences = body.optJSONObject("preferences") ?: JSONObject()
        return SharedLookWeaveState(
            revision = body.optInt("revision"),
            preferences = WearabilityPreferences(
                oneHandClosure = preferences.optBoolean("oneHandClosure", true),
                seatedComfort = preferences.optBoolean("seatedComfort", true),
                lowSensoryLoad = preferences.optBoolean("lowSensoryLoad", false),
                deviceClearance = preferences.optBoolean("deviceClearance", false),
                easyFootwear = preferences.optBoolean("easyFootwear", true),
                avoidWool = preferences.optBoolean("avoidWool", false),
                animalFree = preferences.optBoolean("animalFree", false),
                stepFreeAccess = preferences.optBoolean("stepFreeAccess", false),
                conserveEnergy = preferences.optBoolean("conserveEnergy", false),
                heatSensitive = preferences.optBoolean("heatSensitive", false),
                limitedOverheadReach = preferences.optBoolean("limitedOverheadReach", false),
                gripStrength = preferences.optBoolean("gripStrength", false),
                seamPressureSensitivity = preferences.optBoolean("seamPressureSensitivity", false),
                easyRemoval = preferences.optBoolean("easyRemoval", false),
                mobilityAidCompatibility = preferences.optBoolean("mobilityAidCompatibility", false),
                customRules = parseRules(preferences.optJSONArray("customRules")),
                userFilters = parseUserFilters(preferences.optJSONArray("userFilters"))
            ),
            selectedOccasion = body.optString("selectedOccasion", "Gallery opening"),
            updatedAt = body.optString("updatedAt").takeIf { it.isNotBlank() && it != "null" },
            likedPlaceIds = body.optJSONArray("likedPlaceIds").toStringSet(),
            passedPlaceIds = body.optJSONArray("passedPlaceIds").toStringSet(),
            likedProductIds = body.optJSONArray("likedProductIds").toStringSet(),
            passedProductIds = body.optJSONArray("passedProductIds").toStringSet()
            ,importedGarments = imported
        )
    }

    private fun parseUserFilters(values: JSONArray?): List<UserFilter> = if (values == null) emptyList() else (0 until values.length()).mapNotNull { index ->
        runCatching {
            val item = values.getJSONObject(index)
            UserFilter(
                filterId = item.optString("filterId"),
                mode = runCatching { FilterMode.valueOf(item.optString("mode", "MUST_AVOID")) }.getOrDefault(FilterMode.MUST_AVOID),
                condition = item.optString("condition").takeIf { it.isNotBlank() && it != "null" },
                enabled = item.optBoolean("enabled", true)
            ).takeIf { it.filterId.isNotBlank() }
        }.getOrNull()
    }

    private fun parseRules(values: JSONArray?): List<WearabilityRule> = if (values == null) emptyList() else (0 until values.length()).mapNotNull { index -> runCatching { parseRule(values.getJSONObject(index)) }.getOrNull() }

    private fun parseRule(value: JSONObject): WearabilityRule = WearabilityRule(
        id = value.optString("id"), label = value.optString("label"),
        key = runCatching { WearabilityRuleKey.valueOf(value.optString("key")) }.getOrDefault(WearabilityRuleKey.ONE_HAND_CLOSURE),
        kind = runCatching { WearabilityRuleKind.valueOf(value.optString("kind")) }.getOrDefault(WearabilityRuleKind.HARD),
        enabled = value.optBoolean("enabled", true), confirmed = value.optBoolean("confirmed", false),
        occasion = value.optString("occasion").takeIf { it.isNotBlank() && it != "null" }, source = value.optString("source")
    )

    private fun ruleJson(rule: WearabilityRule) = JSONObject().apply {
        put("id", rule.id); put("label", rule.label); put("key", rule.key.name); put("kind", rule.kind.name)
        put("enabled", rule.enabled); put("confirmed", rule.confirmed); put("occasion", rule.occasion); put("source", rule.source)
    }

    private fun garmentJson(garment: Garment) = JSONObject().apply {
        put("id", garment.id); put("name", garment.name); put("subtitle", garment.subtitle); put("category", garment.category.name)
        put("colorName", garment.colorName); put("closure", JSONObject().put("value", garment.closure.value).put("source", garment.closure.source.name).put("confidence", garment.closure.confidence))
        put("fabric", JSONObject().put("value", garment.fabric.value).put("source", garment.fabric.source.name).put("confidence", garment.fabric.confidence))
        put("wearability", garment.wearability); put("oneHandFriendly", garment.oneHandFriendly); put("seatedComfort", garment.seatedComfort)
        put("lowSensoryLoad", garment.lowSensoryLoad); put("deviceClearance", garment.deviceClearance); put("available", garment.available); put("note", garment.note)
        put("overheadReachFriendly", garment.overheadReachFriendly); put("gripFriendly", garment.gripFriendly); put("lowPressure", garment.lowPressure); put("easyRemoval", garment.easyRemoval); put("mobilityAidFriendly", garment.mobilityAidFriendly); put("imageUrl", garment.imageUrl)
    }

    private fun parseGarments(values: JSONArray?): List<Garment> = if (values == null) emptyList() else (0 until values.length()).mapNotNull { index -> runCatching {
        val item = values.getJSONObject(index)
        fun evidence(key: String) = item.optJSONObject(key)?.let { value -> Evidence(value.optString("value", "Unknown"), runCatching { EvidenceSource.valueOf(value.optString("source")) }.getOrDefault(EvidenceSource.ESTIMATED), value.optInt("confidence", 0)) } ?: Evidence("Unknown", EvidenceSource.ESTIMATED, 0)
        Garment(item.optString("id"), item.optString("name"), item.optString("subtitle"), runCatching { GarmentCategory.valueOf(item.optString("category")) }.getOrDefault(GarmentCategory.TOP), item.optString("colorName", "Imported"), Color(0xFF7C897E), evidence("closure"), evidence("fabric"), item.optInt("wearability", 50), item.optBoolean("oneHandFriendly"), item.optBoolean("seatedComfort"), item.optBoolean("lowSensoryLoad"), item.optBoolean("deviceClearance"), item.optBoolean("available", true), item.optString("note"), item.optNullableBoolean("overheadReachFriendly"), item.optNullableBoolean("gripFriendly"), item.optNullableBoolean("lowPressure"), item.optNullableBoolean("easyRemoval"), item.optNullableBoolean("mobilityAidFriendly"), item.optString("imageUrl").takeIf { it.isNotBlank() && it != "null" })
    }.getOrNull() }

    private fun JSONObject.optNullableBoolean(key: String): Boolean? = if (isNull(key)) null else optBoolean(key)

    private fun JSONArray?.toStringSet(): Set<String> = if (this == null) emptySet() else (0 until length()).mapNotNull {
        optString(it).takeIf(String::isNotBlank)
    }.toSet()

    suspend fun createVoiceCredentials(): VoiceCredentials = withContext(Dispatchers.IO) {
        val body = postJson("/v1/voice/session", JSONObject())
        VoiceCredentials(
            token = body.getString("token"),
            conversationId = body.optString("conversationId").takeIf { it.isNotBlank() && it != "null" },
            expiresInSeconds = body.optInt("expiresInSeconds", 600)
        )
    }

    suspend fun outingPlan(postalCode: String): JSONObject = withContext(Dispatchers.IO) {
        postJson("/v1/agent/outing", JSONObject().put("postalCode", postalCode), readTimeoutMs = 180_000)
    }

    suspend fun recordObservation(garmentId: String, task: String, seconds: Int, difficulty: String) = withContext(Dispatchers.IO) {
        parseSharedState(postJson("/v1/observations", JSONObject().put("garmentId", garmentId).put("task", task).put("seconds", seconds).put("difficulty", difficulty)))
        Unit
    }

    suspend fun observations(): List<JSONObject> = withContext(Dispatchers.IO) {
        val values = getJson("/v1/sync/state", 8_000).optJSONArray("observations") ?: JSONArray()
        (0 until values.length()).map { values.getJSONObject(it) }
    }

    suspend fun resetProfile() = withContext(Dispatchers.IO) {
        parseSharedState(postJson("/v1/profile/reset", JSONObject()))
    }

    override suspend fun beginSession(context: VoiceContext): VoiceSession {
        val credentials = createVoiceCredentials()
        return VoiceSession(credentials.conversationId.orEmpty(), credentials.token)
    }

    override suspend fun endSession(sessionId: String) = Unit

    override suspend fun createPreview(request: TryOnRequest): TryOnResult =
        TryOnResult.Failed(false, "Use createPreview with the Android content resolver")

    suspend fun createPreview(
        resolver: ContentResolver,
        personUri: Uri,
        garmentUri: Uri,
        category: String = "auto"
    ): PreviewTask = withContext(Dispatchers.IO) {
        val person = encodeImage(resolver, personUri, "Person image")
        val garment = encodeImage(resolver, garmentUri, "Garment image")
        val body = postJson("/v1/try-on", JSONObject().apply {
            put("person", person)
            put("garment", garment)
            put("category", category)
        }, readTimeoutMs = 220_000)
        PreviewTask(body.getString("taskId"), body.getString("imageUrl"))
    }

    /** Dedicated YouCam 2D VTO for a close wrist/hand/selfie and accessory product image. */
    suspend fun createAccessoryPreview(
        resolver: ContentResolver,
        targetUri: Uri,
        productUri: Uri,
        kind: String
    ): PreviewTask = withContext(Dispatchers.IO) {
        val target = encodeImage(resolver, targetUri, "Close-up image")
        val product = encodeImage(resolver, productUri, "Accessory image")
        val body = postJson("/v1/accessory-try-on", JSONObject().apply {
            put("target", target)
            put("product", product)
            put("kind", kind)
        }, readTimeoutMs = 220_000)
        PreviewTask(body.getString("taskId"), body.getString("imageUrl"))
    }

    suspend fun download(url: String): ByteArray = withContext(Dispatchers.IO) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 60_000
            requestMethod = "GET"
        }
        try {
            if (connection.responseCode !in 200..299) throw IntegrationException("Preview download failed", connection.responseCode)
            connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun health(): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val connection = (URL("$baseUrl/health").openConnection() as HttpURLConnection).apply {
            connectTimeout = 2_500
            readTimeout = 2_500
            requestMethod = "GET"
        }
        try {
            if (connection.responseCode !in 200..299) return@withContext emptyMap()
            val services = JSONObject(connection.inputStream.bufferedReader().use { it.readText() }).getJSONObject("services")
            services.keys().asSequence().associateWith { services.optBoolean(it) }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun dayContext(place: String, departure: String = "00:00", returnTime: String = "23:00"): DayContext = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(place.trim(), Charsets.UTF_8.name())
        val body = getJson("/v1/context?place=$encoded&departure=${URLEncoder.encode(departure, Charsets.UTF_8.name())}&return=${URLEncoder.encode(returnTime, Charsets.UTF_8.name())}", 20_000)
        val rawSignals = body.getJSONObject("signals")
        val signals = buildList {
            rawSignals.optDouble("apparentTemperatureF", Double.NaN).takeUnless { it.isNaN() }?.let { add(DaySignal("Feels like", "${it.toInt()}°")) }
            rawSignals.optDouble("precipitationProbabilityPercent", Double.NaN).takeUnless { it.isNaN() }?.let { add(DaySignal("Rain", "${it.toInt()}%")) }
            rawSignals.optDouble("windGustMph", Double.NaN).takeUnless { it.isNaN() }?.let { add(DaySignal("Gusts", "${it.toInt()} mph")) }
            rawSignals.optDouble("uvIndex", Double.NaN).takeUnless { it.isNaN() }?.let { add(DaySignal("UV", String.format(Locale.US, "%.1f", it))) }
            rawSignals.optDouble("usAqi", Double.NaN).takeUnless { it.isNaN() }?.let { add(DaySignal("Air", "AQI ${it.toInt()}")) }
            rawSignals.optDouble("maxPollen", Double.NaN).takeUnless { it.isNaN() }?.let { add(DaySignal("Pollen", String.format(Locale.US, "%.1f", it))) }
        }
        val rawRecommendations = body.getJSONArray("recommendations")
        DayContext(
            location = body.getString("location"),
            observedAt = body.optString("observedAt"),
            signals = signals,
            recommendations = buildList {
                repeat(rawRecommendations.length()) { index ->
                    val item = rawRecommendations.getJSONObject(index)
                    add(ContextRecommendation(item.getString("id"), item.getString("title"), item.getString("reason"), item.getString("action"), item.optString("constraint")))
                }
            },
            hourly = (body.optJSONArray("hourly") ?: JSONArray()).let { values ->
                (0 until values.length()).map { index ->
                    val item = values.getJSONObject(index)
                    HourlySignal(
                        item.optString("time"),
                        item.optNullableDouble("temperatureF"),
                        item.optNullableDouble("precipitationPercent"),
                        item.optNullableDouble("windGustMph"),
                        item.optNullableDouble("uvIndex"),
                        item.optNullableDouble("usAqi")
                    )
                }
            },
            attribution = body.optString("attribution")
            ,departure = body.optJSONObject("interval")?.optString("departure", departure) ?: departure
            ,returnTime = body.optJSONObject("interval")?.optString("returnTime", returnTime) ?: returnTime
        )
    }

    private fun JSONObject.optNullableDouble(key: String): Double? =
        if (isNull(key)) null else optDouble(key).takeUnless { it.isNaN() }

    /**
     * Always decode + re-encode as JPEG so HEIC/content-URI/WebP uploads satisfy YouCam JPEG/PNG rules.
     * Resource drawables keep PNG when already PNG-friendly; otherwise JPEG.
     */
    private fun encodeImage(resolver: ContentResolver, uri: Uri, label: String): JSONObject {
        val declared = when {
            uri.scheme == "android.resource" -> "image/png"
            else -> resolver.getType(uri)?.lowercase()
        }
        val output = ByteArrayOutputStream()
        val mime: String
        val passThrough = declared == "image/jpeg" || declared == "image/jpg" || declared == "image/png"
        if (passThrough) {
            resolver.openInputStream(uri)?.use { input ->
                val buffer = ByteArray(16 * 1024)
                var total = 0
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > 10 * 1024 * 1024) throw IntegrationException("$label must be under 10 MB")
                    output.write(buffer, 0, count)
                }
            } ?: throw IntegrationException("$label could not be read")
            if (output.size() == 0) throw IntegrationException("$label is empty")
            mime = if (declared == "image/png") "image/png" else "image/jpeg"
        } else {
            val bitmap = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw IntegrationException("$label could not be decoded — use a JPEG or PNG photo")
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                throw IntegrationException("$label could not be converted to JPEG")
            }
            if (!bitmap.isRecycled) bitmap.recycle()
            if (output.size() == 0) throw IntegrationException("$label is empty")
            if (output.size() > 10 * 1024 * 1024) throw IntegrationException("$label must be under 10 MB")
            mime = "image/jpeg"
        }
        return JSONObject().put("mimeType", mime).put("base64", Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP))
    }

    private fun postJson(path: String, payload: JSONObject, readTimeoutMs: Int = 45_000): JSONObject {
        return requestJson(path, "POST", payload, readTimeoutMs)
    }

    private fun requestJson(path: String, method: String, payload: JSONObject, readTimeoutMs: Int): JSONObject {
        var lastError: Exception? = null
        for (attempt in 0..1) {
            val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
                connectTimeout = 7_500
                readTimeout = readTimeoutMs
                requestMethod = method
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Connection", "close")
            }
            try {
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val body = try { JSONObject(text.ifBlank { "{}" }) } catch (_: Exception) { JSONObject() }
                if (status !in 200..299) throw IntegrationException(body.optString("error", "Integration request failed"), status)
                return body
            } catch (error: IntegrationException) {
                throw error
            } catch (error: Exception) {
                lastError = error
                if (attempt == 0) continue
            } finally {
                connection.disconnect()
            }
        }
        throw IntegrationException("LookWeave services are unavailable. Check that the gateway is running.")
    }

    private fun getJson(path: String, readTimeoutMs: Int): JSONObject {
        var lastError: Exception? = null
        for (attempt in 0..1) {
            val connection = (URL("$baseUrl$path").openConnection() as HttpURLConnection).apply {
                connectTimeout = 7_500
                this.readTimeout = readTimeoutMs
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Connection", "close")
            }
            try {
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val body = try { JSONObject(text.ifBlank { "{}" }) } catch (_: Exception) { JSONObject() }
                if (status !in 200..299) throw IntegrationException(body.optString("error", "Context request failed"), status)
                return body
            } catch (error: IntegrationException) {
                throw error
            } catch (error: Exception) {
                lastError = error
                if (attempt == 0) continue
            } finally {
                connection.disconnect()
            }
        }
        throw IntegrationException(lastError?.message?.takeIf { it.isNotBlank() }
            ?: "Live day signals are unavailable. Check that the gateway is running.")
    }
}
