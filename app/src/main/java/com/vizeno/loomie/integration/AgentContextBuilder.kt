package com.vizeno.loomie.integration

import android.content.Context
import com.vizeno.loomie.data.LocalProfileStore
import com.vizeno.loomie.data.LookWeaveRepository
import com.vizeno.loomie.data.SamplePersonProfiles
import com.vizeno.loomie.model.FilterCatalog
import com.vizeno.loomie.model.WearabilityPreferences
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/** Builds [AgentRequest] payloads with profile gender, sizes, and day context for suggestions. */
object AgentContextBuilder {
    fun build(
        context: Context,
        message: String,
        preferences: WearabilityPreferences,
        dayContext: DayContext? = null,
        dayContextNotes: String? = null,
        likedProductIds: Set<String> = emptySet(),
        likedPlaceIds: Set<String> = emptySet(),
        style: Map<String, String> = emptyMap(),
        extraEvidence: List<GarmentEvidencePayload> = emptyList()
    ): AgentRequest {
        val month = YearMonth.now()
        val monthStart = month.atDay(1).toEpochDay()
        val monthEnd = month.plusMonths(1).atDay(1).toEpochDay()
        val budget = LocalProfileStore.loadBudget(context)
        val events = LocalProfileStore.loadEvents(context)
        val planned = LocalProfileStore.loadPlannedOutfits(context)
        val sizes = LocalProfileStore.loadSizes(context)
        val gender = LocalProfileStore.genderLabel(context)
        val priceLimit = LocalProfileStore.priceLimit(context)
        val postalCode = LocalProfileStore.postalCode(context)
        val preferredColors = LocalProfileStore.preferredColors(context)
        val preferredStyles = LocalProfileStore.preferredStyles(context)
        val interests = LocalProfileStore.interests(context)
        val avoidColors = LocalProfileStore.avoidColors(context)
        val styleChoices = LocalProfileStore.styleChoices(context)
        val persona = LocalProfileStore.personaMeta(context)
        val occupation = LocalProfileStore.occupation(context).ifBlank { persona.occupation }
        val age = LocalProfileStore.age(context) ?: persona.age
        val exampleRequest = LocalProfileStore.exampleRequest(context)
        val contextEvidence = dayContext?.let { day ->
            GarmentEvidencePayload(
                garmentId = "day-context",
                facts = mapOf(
                    "location" to day.location,
                    "signals" to day.signals.joinToString { "${it.label}: ${it.value}" },
                    "recommended_adjustments" to day.recommendations.filter { it.action == "apply" }.joinToString { it.constraint },
                    "confirmation_required" to day.recommendations.filter { it.action == "confirm" }.joinToString { it.constraint }
                ),
                sources = mapOf(
                    "signals" to "Live Open-Meteo and CAMS forecast",
                    "confirmation_required" to "Not a constraint until user confirms"
                )
            )
        }
        return AgentRequest(
            message = message,
            garmentEvidence = SamplePersonProfiles.wardrobeForActiveProfile(context).map { garment ->
                GarmentEvidencePayload(
                    garmentId = garment.id,
                    facts = mapOf(
                        "name" to garment.name,
                        "closure" to garment.closure.value,
                        "fabric" to garment.fabric.value,
                        "note" to garment.note
                    ),
                    sources = mapOf(
                        "closure" to garment.closure.source.label,
                        "fabric" to garment.fabric.source.label
                    )
                )
            } + listOfNotNull(contextEvidence) + extraEvidence,
            hardConstraintIds = buildSet {
                if (preferences.oneHandClosure) add("easy_one_hand_closure")
                if (preferences.seatedComfort) add("seated_comfort")
                if (preferences.easyFootwear) add("easy_footwear")
                if (preferences.lowSensoryLoad) add("low_sensory_load")
                if (preferences.deviceClearance) add("device_clearance")
                if (preferences.avoidWool) add("avoid_wool")
                if (preferences.animalFree) add("animal_free")
                if (preferences.limitedOverheadReach) add("limited_overhead_reach")
                if (preferences.gripStrength) add("grip_strength")
                if (preferences.seamPressureSensitivity) add("seam_pressure_sensitivity")
                if (preferences.easyRemoval) add("easy_removal")
                if (preferences.mobilityAidCompatibility) add("mobility_aid_compatibility")
                if (preferences.stepFreeAccess) add("step_free_access")
                if (preferences.conserveEnergy) add("conserve_energy")
                if (preferences.heatSensitive) add("heat_sensitive")
                preferences.userFilters.filter { it.enabled }.forEach { add("filter:${it.filterId}:${it.mode.name}") }
                preferences.customRules.filter { it.enabled && it.confirmed }.forEach { add("custom_rule:${it.id}:${it.label}") }
            } + (dayContext?.recommendations?.filter { it.action == "apply" }?.map { "day_${it.id}" } ?: emptyList()),
            dayContextNotes = dayContextNotes,
            priceLimit = priceLimit,
            budgetRemaining = budget.remaining(monthStart, monthEnd),
            events = events.takeLast(12).map {
                "${LocalDate.ofEpochDay(it.dateEpochDay)} · ${it.title} (${it.recurrence.name.lowercase()})" +
                    (it.outfitOccasion?.let { occasion -> " · outfit:$occasion" } ?: "")
            },
            preferences = mapOf(
                "oneHandClosure" to preferences.oneHandClosure,
                "seatedComfort" to preferences.seatedComfort,
                "lowSensoryLoad" to preferences.lowSensoryLoad,
                "easyFootwear" to preferences.easyFootwear,
                "easyRemoval" to preferences.easyRemoval,
                "avoidWool" to preferences.avoidWool,
                "animalFree" to preferences.animalFree,
                "deviceClearance" to preferences.deviceClearance,
                "mobilityAidCompatibility" to preferences.mobilityAidCompatibility,
                "gender" to gender,
                "postalCode" to postalCode,
                "preferredColors" to preferredColors.joinToString(", "),
                "avoidColors" to avoidColors.joinToString(", "),
                "preferredStyles" to preferredStyles.joinToString(", "),
                "interests" to interests.joinToString(", "),
                "age" to age,
                "occupation" to occupation,
                "personaSummary" to LocalProfileStore.personaSummary(context),
                "exampleRequest" to exampleRequest
            ),
            style = style + styleChoices + buildMap {
                if (preferredColors.isNotEmpty()) put("preferredColors", preferredColors.joinToString(", "))
                if (avoidColors.isNotEmpty()) put("avoidColors", avoidColors.joinToString(", "))
                if (preferredStyles.isNotEmpty()) put("preferredStyles", preferredStyles.joinToString(", "))
                if (interests.isNotEmpty()) put("interests", interests.joinToString(", "))
                if (postalCode.isNotBlank()) put("postalCode", postalCode)
                age?.let { put("age", it.toString()) }
                if (occupation.isNotBlank()) put("occupation", occupation)
                if (exampleRequest.isNotBlank()) put("exampleRequest", exampleRequest)
            },
            sizes = sizes.asMap(),
            gender = gender,
            postalCode = postalCode,
            wardrobe = SamplePersonProfiles.wardrobeForActiveProfile(context).map { "${it.id}:${it.name}:${it.category.name}:${it.audience.name}" },
            accessoriesPreferences = (style + styleChoices).filterKeys {
                it.startsWith("colour:") || it == "faceShape" || it == "skinTone" || it == "colourDirection" || it == "metal"
            },
            discoveryLikes = likedProductIds.map { "product:$it" } + likedPlaceIds.map { "place:$it" } +
                LocalProfileStore.likedProductIds(context).map { "product:$it" },
            fitObservations = LookWeaveRepository.observations.takeLast(12).map {
                "${it.garmentId} · ${it.task} · ${it.difficulty}" + (it.region?.let { region -> " · $region" } ?: "")
            },
            activeFilters = preferences.userFilters.filter { it.enabled }.map { filter ->
                val def = FilterCatalog.byId(filter.filterId)
                "${def?.label ?: filter.filterId}:${filter.mode.name}:${if (def?.evaluable == true) "evaluable" else "needs_evidence"}"
            },
            customRules = preferences.customRules.filter { it.enabled && it.confirmed }.map { it.label },
            purchases = budget.purchases.filter { it.dateEpochDay in monthStart until monthEnd }.takeLast(12).map {
                "${it.name}: $${String.format(Locale.US, "%.2f", it.amount)}"
            },
            plannedOutfits = planned.takeLast(12).map {
                "${LocalDate.ofEpochDay(it.dateEpochDay)} · ${it.occasion}"
            }
        )
    }
}
