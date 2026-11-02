package com.vizeno.loomie.model

enum class FilterCategory(val label: String) {
    CLOSURES("Closures & fastening"),
    SEATED("Seated comfort"),
    SENSORY("Sensory comfort"),
    FOOTWEAR("Footwear"),
    REACH("Reach & dressing"),
    GRIP("Grip & hardware"),
    PRESSURE("Pressure & seams"),
    MATERIALS("Materials"),
    MOBILITY("Mobility & devices"),
    CLIMATE("Climate & layers"),
    OCCASION("Occasion & formality"),
    SIZING("Sizing & shopping")
}

enum class FilterMode { MUST_AVOID, PREFER, CONDITIONAL }

enum class FilterEvidence { VERIFIED, CONFLICT, UNKNOWN }

data class FilterDefinition(
    val id: String,
    val label: String,
    val category: FilterCategory,
    val evaluable: Boolean = false,
    val evaluatorKey: String? = null,
    val quick: Boolean = false
)

data class UserFilter(
    val filterId: String,
    val mode: FilterMode = FilterMode.MUST_AVOID,
    val condition: String? = null,
    val enabled: Boolean = true
)

object FilterCatalog {
    private fun d(
        id: String,
        label: String,
        category: FilterCategory,
        key: String? = null,
        quick: Boolean = false
    ) = FilterDefinition(id, label, category, evaluable = key != null, evaluatorKey = key, quick = quick)

    val all: List<FilterDefinition> = listOf(
        // Closures & fastening (12)
        d("cl_one_hand", "Easy one-hand closure", FilterCategory.CLOSURES, "one_hand_closure", quick = true),
        d("cl_no_small_buttons", "Avoid small buttons", FilterCategory.CLOSURES, "one_hand_closure"),
        d("cl_no_back_zip", "Avoid back zippers", FilterCategory.CLOSURES, "one_hand_closure"),
        d("cl_magnetic", "Prefer magnetic closures", FilterCategory.CLOSURES, "one_hand_closure", quick = true),
        d("cl_large_pulls", "Prefer large zipper pulls", FilterCategory.CLOSURES, "grip_friendly"),
        d("cl_no_hooks", "Avoid hook-and-eye fasteners", FilterCategory.CLOSURES),
        d("cl_no_snaps_tight", "Avoid tight snap rows", FilterCategory.CLOSURES),
        d("cl_front_access", "Prefer front-access closures", FilterCategory.CLOSURES, "limited_overhead"),
        d("cl_velcro_ok", "Hook-and-loop acceptable", FilterCategory.CLOSURES),
        d("cl_no_ties", "Avoid ties and drawstrings that knot", FilterCategory.CLOSURES),
        d("cl_easy_toggle", "Prefer toggles over tiny clasps", FilterCategory.CLOSURES, "grip_friendly"),
        d("cl_no_corset", "Avoid lace-up structured closures", FilterCategory.CLOSURES),

        // Seated comfort (11)
        d("se_seated", "Comfort while seated", FilterCategory.SEATED, "seated_comfort", quick = true),
        d("se_soft_waist", "Prefer soft or elastic waistbands", FilterCategory.SEATED, "seated_comfort"),
        d("se_no_rigid_rise", "Avoid rigid high rise", FilterCategory.SEATED, "seated_comfort"),
        d("se_smooth_back", "Prefer smooth back details", FilterCategory.SEATED, "seated_comfort"),
        d("se_no_pocket_bulk", "Avoid bulky rear pocket hardware", FilterCategory.SEATED),
        d("se_stretch_seat", "Prefer stretch through the seat", FilterCategory.SEATED, "seated_comfort"),
        d("se_no_tight_thigh", "Avoid tight thigh cut when seated", FilterCategory.SEATED),
        d("se_longer_rise", "Prefer longer rise for seated wear", FilterCategory.SEATED),
        d("se_no_belt_loops_hard", "Avoid stiff belt-loop pressure", FilterCategory.SEATED, "low_pressure"),
        d("se_wheelchair_friendly_cut", "Prefer seated-wear cut", FilterCategory.SEATED, "seated_comfort", quick = true),
        d("se_no_back_seam_ridge", "Avoid raised center-back seams", FilterCategory.SEATED, "low_pressure"),

        // Sensory comfort (11)
        d("sn_low_load", "Low sensory load", FilterCategory.SENSORY, "low_sensory", quick = true),
        d("sn_no_scratchy_label", "Avoid scratchy neck labels", FilterCategory.SENSORY, "low_sensory"),
        d("sn_tagless", "Prefer tagless or printed labels", FilterCategory.SENSORY, "low_sensory"),
        d("sn_soft_handfeel", "Prefer soft handfeel fabrics", FilterCategory.SENSORY, "low_sensory"),
        d("sn_no_stiff_collar", "Avoid stiff collars", FilterCategory.SENSORY),
        d("sn_no_heavy_hardware", "Avoid heavy dangling hardware", FilterCategory.SENSORY, "low_sensory"),
        d("sn_quiet_fabric", "Prefer quieter fabrics (less rustle)", FilterCategory.SENSORY),
        d("sn_no_rough_lining", "Avoid rough linings", FilterCategory.SENSORY, "low_sensory"),
        d("sn_seamless_where_possible", "Prefer fewer high-friction seams", FilterCategory.SENSORY, "low_pressure"),
        d("sn_no_itchy_blend", "Avoid itchy blends", FilterCategory.SENSORY),
        d("sn_matte_not_shiny", "Prefer matte over high-shine surfaces", FilterCategory.SENSORY),

        // Footwear (11)
        d("ft_easy", "Easy footwear", FilterCategory.FOOTWEAR, "easy_footwear", quick = true),
        d("ft_step_in", "Prefer step-in styles", FilterCategory.FOOTWEAR, "easy_footwear", quick = true),
        d("ft_no_laces", "Avoid traditional lace-ups", FilterCategory.FOOTWEAR, "easy_footwear"),
        d("ft_wide_opening", "Prefer wide opening uppers", FilterCategory.FOOTWEAR, "easy_footwear"),
        d("ft_pull_loops", "Prefer heel or tongue pull loops", FilterCategory.FOOTWEAR, "grip_friendly"),
        d("ft_removable_insole", "Prefer removable insoles", FilterCategory.FOOTWEAR),
        d("ft_stable_heel", "Prefer stable heel counter", FilterCategory.FOOTWEAR),
        d("ft_low_force_zip", "Prefer low-force shoe zippers", FilterCategory.FOOTWEAR, "easy_footwear"),
        d("ft_no_backless_unstable", "Avoid unstable backless styles for walking days", FilterCategory.FOOTWEAR),
        d("ft_extra_width", "Prefer wider width options", FilterCategory.FOOTWEAR),
        d("ft_easy_off", "Easy to remove footwear", FilterCategory.FOOTWEAR, "easy_removal"),

        // Reach & dressing (11)
        d("rc_limited_overhead", "Limited overhead reach", FilterCategory.REACH, "limited_overhead", quick = true),
        d("rc_no_pullover_tight", "Avoid tight pullover necks", FilterCategory.REACH, "limited_overhead"),
        d("rc_front_entry", "Prefer front-entry tops", FilterCategory.REACH, "limited_overhead"),
        d("rc_easy_removal", "Easy removal", FilterCategory.REACH, "easy_removal", quick = true),
        d("rc_no_overhead_fasten", "Avoid overhead fastening", FilterCategory.REACH, "limited_overhead"),
        d("rc_low_force_layers", "Prefer low-force layers", FilterCategory.REACH, "limited_overhead"),
        d("rc_sleeve_ease", "Prefer generous sleeve openings", FilterCategory.REACH),
        d("rc_no_back_dressing", "Avoid garments that dress from the back", FilterCategory.REACH, "one_hand_closure"),
        d("rc_one_step_on", "Prefer fewer dressing steps", FilterCategory.REACH),
        d("rc_assisted_ok", "Works with assisted dressing", FilterCategory.REACH),
        d("rc_no_skinny_cuffs", "Avoid skinny cuffs that catch", FilterCategory.REACH),

        // Grip & hardware (10)
        d("gp_grip", "Grip-friendly handling", FilterCategory.GRIP, "grip_friendly", quick = true),
        d("gp_large_pulls", "Prefer oversized pulls and tabs", FilterCategory.GRIP, "grip_friendly"),
        d("gp_no_tiny_zips", "Avoid tiny zippers", FilterCategory.GRIP, "grip_friendly"),
        d("gp_magnetic_prefer", "Prefer magnets over fine clasps", FilterCategory.GRIP, "one_hand_closure"),
        d("gp_loop_pulls", "Prefer loop pulls", FilterCategory.GRIP, "grip_friendly"),
        d("gp_no_slippery_tabs", "Avoid slick low-friction tabs", FilterCategory.GRIP),
        d("gp_chunky_buttons", "Prefer larger buttons if buttons are used", FilterCategory.GRIP, "grip_friendly"),
        d("gp_one_hand_hardware", "Hardware usable one-handed", FilterCategory.GRIP, "one_hand_closure"),
        d("gp_no_spring_clips", "Avoid stiff spring clips", FilterCategory.GRIP),
        d("gp_easy_jewellery_clasps", "Prefer easy jewellery clasps", FilterCategory.GRIP),

        // Pressure & seams (10)
        d("pr_low_pressure", "Low seam and pressure load", FilterCategory.PRESSURE, "low_pressure", quick = true),
        d("pr_no_tight_bands", "Avoid tight elastic bands", FilterCategory.PRESSURE, "low_pressure"),
        d("pr_flat_seams", "Prefer flat or covered seams", FilterCategory.PRESSURE, "low_pressure"),
        d("pr_no_underwire_like", "Avoid rigid understructure", FilterCategory.PRESSURE),
        d("pr_soft_waist", "Prefer soft waist contact", FilterCategory.PRESSURE, "low_pressure"),
        d("pr_no_shoulder_bite", "Avoid biting shoulder seams", FilterCategory.PRESSURE, "low_pressure"),
        d("pr_no_ankle_bind", "Avoid binding ankle cuffs", FilterCategory.PRESSURE),
        d("pr_padded_ok", "Soft padding acceptable", FilterCategory.PRESSURE),
        d("pr_no_point_hardware", "Avoid hardware that presses into skin", FilterCategory.PRESSURE, "low_pressure"),
        d("pr_even_distribution", "Prefer even pressure distribution", FilterCategory.PRESSURE),

        // Materials (11)
        d("mt_avoid_wool", "Avoid wool", FilterCategory.MATERIALS, "avoid_wool", quick = true),
        d("mt_animal_free", "Animal-free materials", FilterCategory.MATERIALS, "animal_free", quick = true),
        d("mt_no_leather", "Avoid leather", FilterCategory.MATERIALS, "animal_free"),
        d("mt_no_silk", "Avoid silk", FilterCategory.MATERIALS, "animal_free"),
        d("mt_breathable", "Prefer breathable fabrics", FilterCategory.MATERIALS),
        d("mt_stretch", "Prefer stretch content", FilterCategory.MATERIALS),
        d("mt_no_stiff_denim", "Avoid stiff raw denim", FilterCategory.MATERIALS),
        d("mt_moisture_manage", "Prefer moisture-managing knits", FilterCategory.MATERIALS),
        d("mt_no_latex", "Avoid latex contact", FilterCategory.MATERIALS),
        d("mt_soft_knit", "Prefer soft knits", FilterCategory.MATERIALS, "low_sensory"),
        d("mt_easy_care", "Prefer easy-care fabrics", FilterCategory.MATERIALS),

        // Mobility & devices (11)
        d("mb_device", "Assistive-device clearance", FilterCategory.MOBILITY, "device_clearance", quick = true),
        d("mb_mobility_aid", "Mobility-aid compatibility", FilterCategory.MOBILITY, "mobility_aid", quick = true),
        d("mb_side_clear", "Keep side seams clear", FilterCategory.MOBILITY, "device_clearance"),
        d("mb_no_waist_bulk", "Avoid waist bulk near devices", FilterCategory.MOBILITY, "device_clearance"),
        d("mb_pocket_access", "Prefer reachable pocket placement", FilterCategory.MOBILITY),
        d("mb_hearing_space", "Space around ears for devices", FilterCategory.MOBILITY),
        d("mb_no_tight_chest", "Avoid tight chest constriction", FilterCategory.MOBILITY, "device_clearance"),
        d("mb_transfer_friendly", "Transfer-friendly bottoms", FilterCategory.MOBILITY, "seated_comfort"),
        d("mb_cable_routes", "Allow discreet cable routes", FilterCategory.MOBILITY),
        d("mb_no_shoulder_bag_only", "Avoid looks that require shoulder bags only", FilterCategory.MOBILITY),
        d("mb_step_free_shopping", "Step-free shopping preference", FilterCategory.MOBILITY),

        // Climate & layers (10)
        d("clm_heat", "Heat affects comfort", FilterCategory.CLIMATE),
        d("clm_light_layers", "Prefer light layers", FilterCategory.CLIMATE),
        d("clm_easy_off_layer", "Layers easy to remove", FilterCategory.CLIMATE, "easy_removal"),
        d("clm_no_heavy_coat", "Avoid heavy coats when heat is high", FilterCategory.CLIMATE),
        d("clm_breathable_outer", "Prefer breathable outer layers", FilterCategory.CLIMATE),
        d("clm_rain_ready", "Prefer water-resistant outer when raining", FilterCategory.CLIMATE),
        d("clm_wind_block", "Prefer wind-blocking shell on windy days", FilterCategory.CLIMATE),
        d("clm_no_trapped_heat", "Avoid heat-trapping thick linings", FilterCategory.CLIMATE),
        d("clm_uv_cover", "Prefer light coverage for strong sun", FilterCategory.CLIMATE),
        d("clm_conserve_energy", "Conserve energy on outing days", FilterCategory.CLIMATE),

        // Occasion & formality (11)
        d("oc_gallery", "Gallery / opening appropriate", FilterCategory.OCCASION),
        d("oc_work", "Work presentation appropriate", FilterCategory.OCCASION),
        d("oc_dinner", "Dinner appropriate", FilterCategory.OCCASION),
        d("oc_travel", "Travel-day comfort first", FilterCategory.OCCASION),
        d("oc_casual", "Keep casual silhouette", FilterCategory.OCCASION),
        d("oc_smart", "Smart-casual ceiling", FilterCategory.OCCASION),
        d("oc_no_formal_shoes", "Avoid formal dress shoes", FilterCategory.OCCASION),
        d("oc_quiet_palette", "Prefer quieter palette", FilterCategory.OCCASION),
        d("oc_photo_ready", "Photo-ready layers", FilterCategory.OCCASION),
        d("oc_outdoor", "Outdoor event readiness", FilterCategory.OCCASION),
        d("oc_whole_day", "Whole-day versatility", FilterCategory.OCCASION),

        // Sizing & shopping (11)
        d("sz_extended", "Prefer extended sizing availability", FilterCategory.SIZING),
        d("sz_try_before", "Prefer try-before or easy returns", FilterCategory.SIZING),
        d("sz_true_measure", "Prefer garments with clear measurements", FilterCategory.SIZING),
        d("sz_price_aware", "Respect price limit when shopping", FilterCategory.SIZING),
        d("sz_online_detail", "Prefer retailers with detailed product evidence", FilterCategory.SIZING),
        d("sz_in_store_option", "Prefer in-person try-on options", FilterCategory.SIZING),
        d("sz_petite_tall", "Consider length variants", FilterCategory.SIZING),
        d("sz_adaptive_line", "Prefer adaptive product lines", FilterCategory.SIZING),
        d("sz_kids_adult", "Include kids or adult adaptive where relevant", FilterCategory.SIZING),
        d("sz_no_final_sale", "Avoid final-sale only when evidence is thin", FilterCategory.SIZING),
        d("sz_alteration_friendly", "Prefer alteration-friendly construction", FilterCategory.SIZING)
    )

    private val byIdMap = all.associateBy { it.id }

    fun byId(id: String): FilterDefinition? = byIdMap[id]

    fun byCategory(category: FilterCategory): List<FilterDefinition> = all.filter { it.category == category }

    fun quickFilters(): List<FilterDefinition> = all.filter { it.quick }

    fun search(query: String): List<FilterDefinition> {
        if (query.isBlank()) return all
        return all.filter {
            it.label.contains(query, true) ||
                it.category.label.contains(query, true) ||
                it.id.contains(query, true)
        }
    }

    fun evaluateKey(key: String, garment: Garment): Boolean? = when (key) {
        "one_hand_closure" -> garment.oneHandFriendly
        "seated_comfort" -> garment.seatedComfort
        "low_sensory" -> garment.lowSensoryLoad
        "device_clearance" -> garment.deviceClearance
        "easy_footwear" -> if (garment.category == GarmentCategory.SHOES) garment.oneHandFriendly else true
        "avoid_wool" -> !garment.fabric.value.contains("wool", true)
        "animal_free" -> listOf("wool", "leather", "silk").none { garment.fabric.value.contains(it, true) }
        "limited_overhead" -> garment.overheadReachFriendly
        "grip_friendly" -> garment.gripFriendly
        "low_pressure" -> garment.lowPressure
        "easy_removal" -> garment.easyRemoval
        "mobility_aid" -> garment.mobilityAidFriendly
        else -> null
    }

    fun syncClassicPreferences(preferences: WearabilityPreferences): WearabilityPreferences {
        fun active(id: String) = preferences.userFilters.any { it.filterId == id && it.enabled && it.mode != FilterMode.PREFER }
        fun prefer(id: String) = preferences.userFilters.any { it.filterId == id && it.enabled }
        if (preferences.userFilters.isEmpty()) return preferences
        return preferences.copy(
            oneHandClosure = preferences.oneHandClosure || active("cl_one_hand") || prefer("cl_magnetic"),
            seatedComfort = preferences.seatedComfort || active("se_seated") || prefer("se_wheelchair_friendly_cut"),
            lowSensoryLoad = preferences.lowSensoryLoad || active("sn_low_load"),
            deviceClearance = preferences.deviceClearance || active("mb_device"),
            easyFootwear = preferences.easyFootwear || active("ft_easy") || prefer("ft_step_in"),
            avoidWool = preferences.avoidWool || active("mt_avoid_wool"),
            animalFree = preferences.animalFree || active("mt_animal_free") || active("mt_no_leather"),
            limitedOverheadReach = preferences.limitedOverheadReach || active("rc_limited_overhead"),
            gripStrength = preferences.gripStrength || active("gp_grip"),
            seamPressureSensitivity = preferences.seamPressureSensitivity || active("pr_low_pressure"),
            easyRemoval = preferences.easyRemoval || active("rc_easy_removal"),
            mobilityAidCompatibility = preferences.mobilityAidCompatibility || active("mb_mobility_aid"),
            heatSensitive = preferences.heatSensitive || prefer("clm_heat"),
            conserveEnergy = preferences.conserveEnergy || prefer("clm_conserve_energy"),
            stepFreeAccess = preferences.stepFreeAccess || prefer("mb_step_free_shopping")
        )
    }
}
