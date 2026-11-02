package com.vizeno.loomie.model

import androidx.compose.ui.graphics.Color

enum class GarmentCategory(val label: String) {
    TOP("Tops"), BOTTOM("Bottoms"), OUTERWEAR("Layers"), SHOES("Shoes"), ACCESSORY("Accessories")
}

enum class EvidenceSource(val label: String) {
    LABEL("Label verified"), PRODUCT("Product details"), OBSERVED("Observed with you"), ESTIMATED("Visual estimate")
}

data class Evidence(
    val value: String,
    val source: EvidenceSource,
    val confidence: Int = 100
)

/** Who a capsule piece is styled for — used to keep male/female looks from cross-dumping. */
enum class GarmentAudience {
    MEN, WOMEN, UNISEX;

    fun matchesGenderLabel(gender: String?): Boolean {
        val raw = gender?.trim()?.lowercase().orEmpty()
        if (raw.isBlank() || raw == "prefer not to say" || raw == "custom gender") return true
        val nonBinary = raw.contains("non-binary") || raw.contains("nonbinary") || raw == "nb"
        // A non-binary identity is not a garment-category restriction. Style, fit,
        // sizes and explicit preferences decide relevance across the full wardrobe.
        if (nonBinary) return true
        return when (this) {
            UNISEX -> true
            MEN -> raw in setOf("male", "men", "mens", "man's", "man")
            WOMEN -> raw in setOf("female", "women", "womens", "woman's", "woman")
        }
    }
}

data class Garment(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: GarmentCategory,
    val colorName: String,
    val color: Color,
    val closure: Evidence,
    val fabric: Evidence,
    val wearability: Int,
    val oneHandFriendly: Boolean,
    val seatedComfort: Boolean,
    val lowSensoryLoad: Boolean,
    val deviceClearance: Boolean,
    val available: Boolean = true,
    val note: String,
    val overheadReachFriendly: Boolean? = null,
    val gripFriendly: Boolean? = null,
    val lowPressure: Boolean? = null,
    val easyRemoval: Boolean? = null,
    val mobilityAidFriendly: Boolean? = null,
    val imageUrl: String? = null,
    val audience: GarmentAudience = GarmentAudience.UNISEX
)

enum class WearabilityRuleKind { HARD, SOFT, CONDITIONAL }

enum class WearabilityRuleKey {
    ONE_HAND_CLOSURE, SEATED_COMFORT, LOW_SENSORY_LOAD, DEVICE_CLEARANCE,
    EASY_FOOTWEAR, AVOID_WOOL, ANIMAL_FREE, STEP_FREE_ACCESS, CONSERVE_ENERGY,
    HEAT_SENSITIVE, LIMITED_OVERHEAD_REACH, GRIP_STRENGTH,
    SEAM_PRESSURE_SENSITIVITY, EASY_REMOVAL, MOBILITY_AID_COMPATIBILITY
}

data class WearabilityRule(
    val id: String,
    val label: String,
    val key: WearabilityRuleKey,
    val kind: WearabilityRuleKind = WearabilityRuleKind.HARD,
    val enabled: Boolean = true,
    val confirmed: Boolean = false,
    val occasion: String? = null,
    val source: String = "User-created rule"
)

data class WearabilityPreferences(
    val oneHandClosure: Boolean = true,
    val seatedComfort: Boolean = true,
    val lowSensoryLoad: Boolean = false,
    val deviceClearance: Boolean = false,
    val easyFootwear: Boolean = true,
    val avoidWool: Boolean = false,
    val animalFree: Boolean = false,
    val stepFreeAccess: Boolean = false,
    val conserveEnergy: Boolean = false,
    val heatSensitive: Boolean = false,
    val limitedOverheadReach: Boolean = false,
    val gripStrength: Boolean = false,
    val seamPressureSensitivity: Boolean = false,
    val easyRemoval: Boolean = false,
    val mobilityAidCompatibility: Boolean = false,
    val customRules: List<WearabilityRule> = emptyList(),
    val userFilters: List<UserFilter> = emptyList()
)

data class OutfitPlan(
    val occasion: String,
    val original: List<Garment>,
    val resolved: List<Garment>,
    val replaced: Garment?,
    val replacement: Garment?,
    val explanation: String,
    val preservedCount: Int,
    val score: Int,
    val unresolved: List<String> = emptyList(),
    val triggeredRules: List<String> = emptyList(),
    val unverifiable: List<String> = emptyList()
)

/** One ranked day look for Today home — pieces + short reason bullets. */
data class DailyLookSuggestion(
    val id: String,
    val title: String,
    val plan: OutfitPlan,
    val reasons: List<String>,
    val score: Int
)

data class InteractionObservation(
    val garmentId: String,
    val task: String,
    val seconds: Int,
    val difficulty: String,
    val note: String,
    val region: String? = null
)

data class AgentMessage(val fromUser: Boolean, val text: String)

data class Retailer(
    val id: String,
    val name: String,
    val focus: String,
    val region: String,
    val description: String,
    val features: Set<String>,
    val notableFinds: List<String>,
    val shopUrl: String,
    val color: Color
)

data class NearbyPlace(
    val id: String,
    val retailerId: String,
    val name: String,
    val neighborhood: String,
    val address: String,
    val distanceMiles: Double,
    val latitude: Double,
    val longitude: Double,
    val match: Int,
    val reason: String,
    val features: List<String>,
    val directionsUrl: String,
    val color: Color,
    val accessibility: String = "unknown",
    val suggestedPieces: List<String> = emptyList()
)

enum class AppDestination(val label: String) {
    TODAY("Today"), WARDROBE("Wardrobe"), PLAN("Plan"), LAB("Fit"), DAY("Day"), AGENT("Loomie"), PROFILE("Profile")
}
