package com.vizeno.loomie.model

/** Editable clothing size log shown on Profile / Fit Studio and included in agent / discovery context. */
data class SizeLog(
    val tops: String = "",
    val tShirt: String = "",
    val pants: String = "",
    val shoes: String = "",
    val dress: String = "",
    val outerwear: String = "",
    val notes: String = "",
    /** Optional body measurements keyed by region label (Neck, Shoulders, Chest, …), free-text inches/cm. */
    val measurements: Map<String, String> = emptyMap()
) {
    fun asMap(): Map<String, String> = buildMap {
        if (tops.isNotBlank()) put("tops", tops)
        if (tShirt.isNotBlank()) put("tShirt", tShirt)
        if (pants.isNotBlank()) put("pants", pants)
        if (shoes.isNotBlank()) put("shoes", shoes)
        if (dress.isNotBlank()) put("dress", dress)
        if (outerwear.isNotBlank()) put("outerwear", outerwear)
        if (notes.isNotBlank()) put("notes", notes)
        measurements.forEach { (region, value) ->
            if (value.isNotBlank()) put("measure:$region", value)
        }
    }

    fun summaryLine(): String = asMap().entries.joinToString(" · ") { "${it.key}: ${it.value}" }

    fun measurement(region: String): String = measurements[region].orEmpty()

    fun withMeasurement(region: String, value: String): SizeLog {
        val next = measurements.toMutableMap()
        if (value.isBlank()) next.remove(region) else next[region] = value.trim()
        return copy(measurements = next)
    }
}

/** Preset gender options for profile / discovery; Custom uses [LocalProfileStore] custom text. */
object GenderOptions {
    const val MALE = "Male"
    const val FEMALE = "Female"
    const val NON_BINARY = "Non-binary"
    const val PREFER_NOT = "Prefer not to say"
    const val CUSTOM = "Custom gender"

    val presets: List<String> = listOf(MALE, FEMALE, NON_BINARY, PREFER_NOT, CUSTOM)
}

/** Clothing type filters for wardrobe / plan / discovery dropdowns. */
object ClothingTypeOptions {
    val all: List<Pair<String?, String>> = listOf(
        null to "All types",
        "shirt" to "Shirt",
        "pant" to "Pants",
        "trousers" to "Trousers",
        "dress" to "Dress",
        "skirt" to "Skirt",
        "jacket" to "Jacket",
        "outerwear" to "Outerwear",
        "shoes" to "Shoes",
        "accessory" to "Accessories",
        "top" to "Tops",
        "bottom" to "Bottoms"
    )
}
