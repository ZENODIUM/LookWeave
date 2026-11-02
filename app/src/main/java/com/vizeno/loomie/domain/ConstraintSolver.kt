package com.vizeno.loomie.domain

import com.vizeno.loomie.data.LookWeaveRepository
import com.vizeno.loomie.model.DailyLookSuggestion
import com.vizeno.loomie.model.FilterCatalog
import com.vizeno.loomie.model.FilterEvidence
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.model.GarmentCategory
import com.vizeno.loomie.model.OutfitPlan
import com.vizeno.loomie.model.SizeLog
import com.vizeno.loomie.model.WearabilityRuleKey
import com.vizeno.loomie.model.WearabilityPreferences

object ConstraintSolver {
    data class Feasibility(
        val allowed: Boolean,
        val reasons: List<String> = emptyList(),
        val unverifiable: List<String> = emptyList(),
        val evidence: Map<String, FilterEvidence> = emptyMap()
    )

    fun feasibility(garment: Garment, preferences: WearabilityPreferences, occasion: String? = null): Feasibility {
        val reasons = mutableListOf<String>()
        val unverifiable = mutableListOf<String>()
        val evidence = linkedMapOf<String, FilterEvidence>()
        fun require(enabled: Boolean, satisfied: Boolean?, reason: String, unknown: String? = null) {
            if (!enabled) return
            if (satisfied != true) reasons += reason
            if (satisfied == null && unknown != null) unverifiable += unknown
        }
        require(true, garment.available, "${garment.name} is unavailable")
        require(preferences.conserveEnergy, LookWeaveRepository.observations.lastOrNull { it.garmentId == garment.id }?.difficulty !in listOf("Difficult", "Couldn’t finish", "Couldn't finish"), "Your latest observation flagged effort for ${garment.name}")
        require(preferences.oneHandClosure, garment.oneHandFriendly, "Easy one-hand closure", "One-hand closure is not verified for ${garment.name}")
        require(preferences.seatedComfort, garment.seatedComfort, "Seated comfort")
        require(preferences.lowSensoryLoad, garment.lowSensoryLoad, "Low sensory load", "Sensory load is not verified for ${garment.name}")
        require(preferences.deviceClearance, garment.deviceClearance, "Assistive-device clearance", "Device clearance is not verified for ${garment.name}")
        require(preferences.easyFootwear && garment.category == GarmentCategory.SHOES, garment.category != GarmentCategory.SHOES || garment.oneHandFriendly, "Easy footwear")
        require(preferences.avoidWool, !garment.fabric.value.contains("wool", true), "Avoid wool")
        require(preferences.animalFree, listOf("wool", "leather", "silk").none { garment.fabric.value.contains(it, true) }, "Animal-free materials")
        require(preferences.limitedOverheadReach, garment.overheadReachFriendly, "Limited overhead reach", "Overhead reach is not verified for ${garment.name}")
        require(preferences.gripStrength, garment.gripFriendly, "Grip-friendly handling", "Grip demands are not verified for ${garment.name}")
        require(preferences.seamPressureSensitivity, garment.lowPressure, "Low seam and pressure load", "Pressure comfort is not verified for ${garment.name}")
        require(preferences.easyRemoval, garment.easyRemoval, "Easy removal", "Removal effort is not verified for ${garment.name}")
        require(preferences.mobilityAidCompatibility, garment.mobilityAidFriendly, "Mobility-aid compatibility", "Mobility-aid clearance is not verified for ${garment.name}")
        preferences.userFilters.filter { it.enabled }.forEach { filter ->
            val definition = FilterCatalog.byId(filter.filterId) ?: return@forEach
            val conditionalActive = filter.mode != FilterMode.CONDITIONAL ||
                filter.condition.isNullOrBlank() ||
                (occasion?.contains(filter.condition, true) == true)
            if (!conditionalActive) return@forEach
            if (!definition.evaluable || definition.evaluatorKey == null) {
                unverifiable += "Unknown until product evidence exists: ${definition.label}"
                evidence[definition.id] = FilterEvidence.UNKNOWN
                return@forEach
            }
            val satisfied = FilterCatalog.evaluateKey(definition.evaluatorKey, garment)
            when {
                satisfied == null -> {
                    // Unknown never matches, never rank-boosts, and never blocks as verified conflict.
                    unverifiable += "Unknown until product evidence exists: ${definition.label}"
                    evidence[definition.id] = FilterEvidence.UNKNOWN
                }
                satisfied -> {
                    evidence[definition.id] = FilterEvidence.VERIFIED
                    if (filter.mode == FilterMode.PREFER) Unit
                }
                else -> {
                    evidence[definition.id] = FilterEvidence.CONFLICT
                    if (filter.mode == FilterMode.MUST_AVOID) reasons += definition.label
                }
            }
        }
        preferences.customRules.filter { it.enabled && it.confirmed && (it.occasion == null || it.occasion.equals(occasion, true)) }.forEach { rule ->
            val matched = when (rule.key) {
                WearabilityRuleKey.ONE_HAND_CLOSURE -> garment.oneHandFriendly
                WearabilityRuleKey.SEATED_COMFORT -> garment.seatedComfort
                WearabilityRuleKey.LOW_SENSORY_LOAD -> garment.lowSensoryLoad
                WearabilityRuleKey.DEVICE_CLEARANCE -> garment.deviceClearance
                WearabilityRuleKey.EASY_FOOTWEAR -> garment.category != GarmentCategory.SHOES || garment.oneHandFriendly
                WearabilityRuleKey.AVOID_WOOL -> !garment.fabric.value.contains("wool", true)
                WearabilityRuleKey.ANIMAL_FREE -> listOf("wool", "leather", "silk").none { garment.fabric.value.contains(it, true) }
                WearabilityRuleKey.LIMITED_OVERHEAD_REACH -> garment.overheadReachFriendly == true
                WearabilityRuleKey.GRIP_STRENGTH -> garment.gripFriendly == true
                WearabilityRuleKey.SEAM_PRESSURE_SENSITIVITY -> garment.lowPressure == true
                WearabilityRuleKey.EASY_REMOVAL -> garment.easyRemoval == true
                WearabilityRuleKey.MOBILITY_AID_COMPATIBILITY -> garment.mobilityAidFriendly == true
                WearabilityRuleKey.STEP_FREE_ACCESS, WearabilityRuleKey.CONSERVE_ENERGY, WearabilityRuleKey.HEAT_SENSITIVE -> true
            }
            if (rule.kind != com.vizeno.loomie.model.WearabilityRuleKind.SOFT && !matched) reasons += rule.label
            if (!matched) unverifiable += "${rule.label}: ${garment.name} has no confirmed evidence"
        }
        return Feasibility(reasons.distinct().isEmpty(), reasons.distinct(), unverifiable.distinct(), evidence)
    }

    fun isFeasible(garment: Garment, preferences: WearabilityPreferences): Boolean = feasibility(garment, preferences).allowed

    fun wearabilityBreakdown(garments: List<Garment>, preferences: WearabilityPreferences, occasion: String): Map<String, Pair<Int, Int>> {
        val dims = listOf(
            "Dressing ease" to listOf("one_hand_closure", "limited_overhead", "easy_removal", "grip_friendly"),
            "Seated comfort" to listOf("seated_comfort"),
            "Sensory" to listOf("low_sensory", "low_pressure"),
            "Mobility" to listOf("mobility_aid", "device_clearance", "easy_footwear")
        )
        return dims.associate { (label, keys) ->
            var known = 0
            var ok = 0
            garments.forEach { garment ->
                keys.forEach { key ->
                    when (FilterCatalog.evaluateKey(key, garment)) {
                        true -> { known++; ok++ }
                        false -> known++
                        null -> Unit
                    }
                }
            }
            val observations = LookWeaveRepository.observations
            if (label == "Dressing ease") {
                observations.filter { it.difficulty in listOf("Difficult", "Couldn’t finish", "Couldn't finish") }.forEach { _ -> known++; }
                observations.filter { it.difficulty == "Easy" }.forEach { _ -> known++; ok++ }
            }
            label to (ok to known)
        }
    }

    private fun softScore(garment: Garment, preferences: WearabilityPreferences, occasion: String): Int {
        val custom = preferences.customRules.filter {
            it.enabled && it.confirmed && it.kind == com.vizeno.loomie.model.WearabilityRuleKind.SOFT && (it.occasion == null || it.occasion.equals(occasion, true))
        }.sumOf { rule ->
            when (rule.key) {
                WearabilityRuleKey.ONE_HAND_CLOSURE -> if (garment.oneHandFriendly) 12 else 0
                WearabilityRuleKey.SEATED_COMFORT -> if (garment.seatedComfort) 12 else 0
                WearabilityRuleKey.LOW_SENSORY_LOAD -> if (garment.lowSensoryLoad) 12 else 0
                WearabilityRuleKey.DEVICE_CLEARANCE -> if (garment.deviceClearance) 12 else 0
                WearabilityRuleKey.LIMITED_OVERHEAD_REACH -> if (garment.overheadReachFriendly == true) 12 else 0
                WearabilityRuleKey.GRIP_STRENGTH -> if (garment.gripFriendly == true) 12 else 0
                WearabilityRuleKey.SEAM_PRESSURE_SENSITIVITY -> if (garment.lowPressure == true) 12 else 0
                WearabilityRuleKey.EASY_REMOVAL -> if (garment.easyRemoval == true) 12 else 0
                WearabilityRuleKey.MOBILITY_AID_COMPATIBILITY -> if (garment.mobilityAidFriendly == true) 12 else 0
                WearabilityRuleKey.EASY_FOOTWEAR -> if (garment.category != GarmentCategory.SHOES || garment.oneHandFriendly) 12 else 0
                WearabilityRuleKey.AVOID_WOOL -> if (!garment.fabric.value.contains("wool", true)) 12 else 0
                WearabilityRuleKey.ANIMAL_FREE -> if (listOf("wool", "leather", "silk").none { garment.fabric.value.contains(it, true) }) 12 else 0
                else -> 0
            }
        }
        val filters = preferences.userFilters.filter { it.enabled && it.mode == FilterMode.PREFER }.sumOf { filter ->
            val definition = FilterCatalog.byId(filter.filterId) ?: return@sumOf 0
            if (!definition.evaluable) return@sumOf 0
            val key = definition.evaluatorKey ?: return@sumOf 0
            // Unknown must never rank-boost.
            if (FilterCatalog.evaluateKey(key, garment) == true) 8 else 0
        }
        return custom + filters
    }

    fun plan(
        occasion: String,
        preferences: WearabilityPreferences,
        wardrobe: List<Garment> = LookWeaveRepository.allGarments,
        gender: String? = null,
        preferProfileCapsule: Boolean = false
    ): OutfitPlan {
        val gendered = if (gender.isNullOrBlank()) wardrobe else wardrobe.filter { it.audience.matchesGenderLabel(gender) }
            .ifEmpty { wardrobe }
        val seedIds = when {
            gender?.equals("Male", true) == true || gender?.equals("Men", true) == true ->
                listOf("g1", "g12", "g3", "g10", "g18")
            gender?.equals("Female", true) == true || gender?.equals("Women", true) == true ->
                listOf("g13", "g15", "g9", "g4", "g5")
            else -> listOf("g1", "g6", "g3", "g4", "g5")
        }
        val original = (if (preferProfileCapsule) emptyList() else seedIds.mapNotNull { id -> gendered.firstOrNull { it.id == id } })
            .ifEmpty {
                listOfNotNull(
                    gendered.firstOrNull { it.category == GarmentCategory.TOP },
                    gendered.firstOrNull { it.category == GarmentCategory.OUTERWEAR },
                    gendered.firstOrNull { it.category == GarmentCategory.BOTTOM },
                    gendered.firstOrNull { it.category == GarmentCategory.SHOES },
                    gendered.firstOrNull { it.category == GarmentCategory.ACCESSORY }
                )
            }
        if (original.isEmpty()) {
            return OutfitPlan(occasion, emptyList(), emptyList(), null, null, "No gender-appropriate wardrobe pieces available.", 0, 0, listOf("Wardrobe is empty for this profile."), emptyList(), emptyList())
        }
        // Use gendered wardrobe for swaps below.
        val wardrobe = gendered
        val evaluations = original.associateWith { feasibility(it, preferences, occasion) }
        val conflicts = original.filterNot { evaluations.getValue(it).allowed }
        val swaps = conflicts.associateWith { blocked ->
            wardrobe.filter { it.category == blocked.category && it.id != blocked.id && feasibility(it, preferences, occasion).allowed }
                .maxByOrNull { it.wearability + softScore(it, preferences, occasion) }
        }
        val unresolved = swaps.filterValues { it == null }.keys.map { "No compatible ${it.category.label.lowercase()} available for ${it.name}." }
        val resolved = original.mapNotNull { if (it in swaps) swaps[it] else it }
        val conflict = conflicts.firstOrNull()
        val replacement = swaps[conflict]
        val preserved = original.count { item -> resolved.any { it.id == item.id } }
        val triggeredRules = conflicts.flatMap { evaluations.getValue(it).reasons }.distinct()
        val unverifiable = (conflicts.flatMap { evaluations.getValue(it).unverifiable } + swaps.values.filterNotNull().flatMap { feasibility(it, preferences, occasion).unverifiable }).distinct()
        val explanation = when {
            unresolved.isNotEmpty() -> "This look needs more evidence or another garment. ${unresolved.joinToString(" ")}"
            conflicts.isNotEmpty() -> swaps.entries.joinToString(" ") { (before, after) -> "${after!!.name} replaces ${before.name} to satisfy your selected requirements." } + " $preserved pieces stay unchanged."
            else -> "Every piece satisfies your current wearability preferences. Nothing needs to change."
        }
        return OutfitPlan(occasion, original, resolved, conflict, replacement, explanation, preserved, if (unresolved.isNotEmpty()) 0 else if (conflict == null) 98 else 96, unresolved, triggeredRules, unverifiable)
    }

    /**
     * Rank up to [count] distinct full looks for Today home.
     * Grounded in wardrobe + preferences + optional day/events/gender/sizes context.
     */
    fun suggestDailyLooks(
        preferences: WearabilityPreferences,
        wardrobe: List<Garment> = LookWeaveRepository.allGarments,
        occasion: String = "Today",
        dayNotes: List<String> = emptyList(),
        eventTitles: List<String> = emptyList(),
        gender: String? = null,
        sizes: SizeLog = SizeLog(),
        preferredGarmentIds: List<String> = emptyList(),
        count: Int = 5
    ): List<DailyLookSuggestion> {
        val preferred = preferredGarmentIds.toSet()
        val gendered = wardrobe.filter { it.audience.matchesGenderLabel(gender) }.ifEmpty { wardrobe }
        val available = gendered.filter { it.available }
        fun preferredBoost(garment: Garment): Int = if (garment.id in preferred) 48 else 0
        fun pool(category: GarmentCategory) = available
            .filter { it.category == category }
            .sortedByDescending { it.wearability + softScore(it, preferences, occasion) + preferredBoost(it) }

        val tops = pool(GarmentCategory.TOP)
        val bottoms = pool(GarmentCategory.BOTTOM)
        val layers = pool(GarmentCategory.OUTERWEAR)
        val shoes = pool(GarmentCategory.SHOES)
        val accessories = pool(GarmentCategory.ACCESSORY)
        if (tops.isEmpty() || bottoms.isEmpty() || shoes.isEmpty()) return emptyList()

        val activeFilterLabels = preferences.userFilters.filter { it.enabled }.mapNotNull { filter ->
            FilterCatalog.byId(filter.filterId)?.label?.let { label ->
                when (filter.mode) {
                    FilterMode.MUST_AVOID -> "Must avoid: $label"
                    FilterMode.PREFER -> "Prefer: $label"
                    FilterMode.CONDITIONAL -> "When relevant: $label"
                }
            }
        }
        val hardPrefs = buildList {
            if (preferences.oneHandClosure) add("One-hand closures")
            if (preferences.seatedComfort) add("Seated comfort")
            if (preferences.easyFootwear) add("Easy footwear")
            if (preferences.lowSensoryLoad) add("Low sensory load")
            if (preferences.animalFree) add("Animal-free materials")
            if (preferences.avoidWool) add("Avoid wool")
            if (preferences.deviceClearance) add("Device clearance")
            if (preferences.limitedOverheadReach) add("Limited overhead reach")
            if (preferences.gripStrength) add("Grip-friendly")
            if (preferences.easyRemoval) add("Easy removal")
            if (preferences.mobilityAidCompatibility) add("Mobility-aid friendly")
        }

        val titles = listOf(
            "Primary day look",
            "Low-effort alternate",
            "Layered weather plan",
            "Event-ready polish",
            "Comfort-first capsule"
        )
        val usedSignatures = linkedSetOf<String>()
        val suggestions = mutableListOf<DailyLookSuggestion>()

        val topOrder = tops.indices.toList()
        val bottomOrder = bottoms.indices.toList()
        val layerOrder = if (layers.isEmpty()) listOf(-1) else layers.indices.toList() + listOf(-1)
        val shoeOrder = shoes.indices.toList()
        val accessoryOrder = if (accessories.isEmpty()) listOf(-1) else accessories.indices.toList() + listOf(-1)

        var attempts = 0
        val maxAttempts = 48
        while (suggestions.size < count && attempts < maxAttempts) {
            val ti = topOrder[attempts % topOrder.size]
            val bi = bottomOrder[(attempts / 2 + attempts) % bottomOrder.size]
            val li = layerOrder[(attempts / 3) % layerOrder.size]
            val si = shoeOrder[(attempts * 2) % shoeOrder.size]
            val ai = accessoryOrder[(attempts + 1) % accessoryOrder.size]
            attempts++

            val pieces = buildList {
                add(tops[ti])
                add(bottoms[bi])
                if (li >= 0) add(layers[li])
                add(shoes[si])
                if (ai >= 0) add(accessories[ai])
            }
            val signature = pieces.map { it.id }.sorted().joinToString("|")
            if (!usedSignatures.add(signature)) continue

            val evaluations = pieces.associateWith { feasibility(it, preferences, occasion) }
            val blocked = pieces.filterNot { evaluations.getValue(it).allowed }
            if (blocked.isNotEmpty()) {
                // Try soft-swap blocked pieces within category before discarding the look.
                val repaired = pieces.map { piece ->
                    if (evaluations.getValue(piece).allowed) piece
                    else available
                        .filter { it.category == piece.category && it.id != piece.id && feasibility(it, preferences, occasion).allowed }
                        .maxByOrNull { it.wearability + softScore(it, preferences, occasion) + preferredBoost(it) }
                        ?: return@map null
                }
                if (repaired.any { it == null }) continue
                val fixed = repaired.filterNotNull()
                val fixedSig = fixed.map { it.id }.sorted().joinToString("|")
                if (!usedSignatures.add(fixedSig) && fixedSig != signature) continue
                usedSignatures.add(fixedSig)
                suggestions += buildDailySuggestion(
                    index = suggestions.size,
                    title = titles.getOrElse(suggestions.size) { "Look ${suggestions.size + 1}" },
                    occasion = occasion,
                    pieces = fixed,
                    preferences = preferences,
                    activeFilterLabels = activeFilterLabels,
                    hardPrefs = hardPrefs,
                    dayNotes = dayNotes,
                    eventTitles = eventTitles,
                    gender = gender,
                    sizes = sizes,
                    preferredGarmentIds = preferred
                )
                continue
            }

            suggestions += buildDailySuggestion(
                index = suggestions.size,
                title = titles.getOrElse(suggestions.size) { "Look ${suggestions.size + 1}" },
                occasion = occasion,
                pieces = pieces,
                preferences = preferences,
                activeFilterLabels = activeFilterLabels,
                hardPrefs = hardPrefs,
                dayNotes = dayNotes,
                eventTitles = eventTitles,
                gender = gender,
                sizes = sizes,
                preferredGarmentIds = preferred
            )
        }

        return suggestions.sortedByDescending { it.score }.take(count)
    }

    private fun buildDailySuggestion(
        index: Int,
        title: String,
        occasion: String,
        pieces: List<Garment>,
        preferences: WearabilityPreferences,
        activeFilterLabels: List<String>,
        hardPrefs: List<String>,
        dayNotes: List<String>,
        eventTitles: List<String>,
        gender: String?,
        sizes: SizeLog,
        preferredGarmentIds: Set<String> = emptySet()
    ): DailyLookSuggestion {
        val evaluations = pieces.map { it to feasibility(it, preferences, occasion) }
        val verifiedFilters = evaluations.flatMap { (_, ev) ->
            ev.evidence.filterValues { it == FilterEvidence.VERIFIED }.keys.mapNotNull { id ->
                FilterCatalog.byId(id)?.label?.let { "Verified: $it" }
            }
        }.distinct()
        val unknown = evaluations.flatMap { (_, ev) -> ev.unverifiable }.distinct()
        val soft = pieces.sumOf { softScore(it, preferences, occasion) }
        val preferredHits = pieces.count { it.id in preferredGarmentIds }
        val preferredBoost = preferredHits * 14
        val base = pieces.map { it.wearability }.average().toInt()
        val score = (base + (soft / pieces.size.coerceAtLeast(1)).coerceAtMost(12) + preferredBoost).coerceIn(1, 99)

        val reasons = buildList {
            add("Pieces: " + pieces.joinToString(" · ") { it.name })
            if (preferredHits > 0) add("Capsule match: $preferredHits preferred piece${if (preferredHits == 1) "" else "s"}")
            if (hardPrefs.isNotEmpty()) add("Filters on: " + hardPrefs.take(4).joinToString(", "))
            activeFilterLabels.take(3).forEach { add(it) }
            verifiedFilters.take(3).forEach { add(it) }
            if (eventTitles.isNotEmpty()) add("Day events: " + eventTitles.take(2).joinToString(", "))
            dayNotes.take(2).forEach { note -> add(note) }
            gender?.takeIf { it.isNotBlank() && it != "Prefer not to say" }?.let { add("Profile gender: $it") }
            sizes.summaryLine().takeIf { it.isNotBlank() }?.let { add("Sizes: ${it.take(72)}") }
            pieces.firstOrNull()?.let { g ->
                add("Evidence: ${g.closure.value} (${g.closure.source.label}) · ${g.fabric.value}")
            }
            unknown.take(1).forEach { add(it) }
            add("Wearability score $score")
        }.distinct().take(7)

        val plan = OutfitPlan(
            occasion = occasion,
            original = pieces,
            resolved = pieces,
            replaced = null,
            replacement = null,
            explanation = reasons.firstOrNull().orEmpty(),
            preservedCount = pieces.size,
            score = score,
            unresolved = emptyList(),
            triggeredRules = hardPrefs.take(3),
            unverifiable = unknown
        )
        return DailyLookSuggestion(
            id = "day-look-$index-${pieces.joinToString("-") { it.id }}",
            title = title,
            plan = plan,
            reasons = reasons,
            score = score
        )
    }
}
