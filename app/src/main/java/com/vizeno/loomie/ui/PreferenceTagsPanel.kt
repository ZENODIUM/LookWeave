package com.vizeno.loomie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.model.FilterCatalog
import com.vizeno.loomie.model.FilterCategory
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.UserFilter
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.ui.theme.Hairline
import com.vizeno.loomie.ui.theme.Olive
import com.vizeno.loomie.ui.theme.OliveLight
import com.vizeno.loomie.ui.theme.Paper
import com.vizeno.loomie.ui.theme.SoftInk

data class ClassicPreferenceTag(
    val id: String,
    val label: String,
    val category: String,
    val getter: (WearabilityPreferences) -> Boolean,
    val setter: (WearabilityPreferences, Boolean) -> WearabilityPreferences
)

val ClassicPreferenceTags: List<ClassicPreferenceTag> = listOf(
    ClassicPreferenceTag("oneHand", "Easy one-hand closure", "How it works", { it.oneHandClosure }) { p, v -> p.copy(oneHandClosure = v) },
    ClassicPreferenceTag("seated", "Comfort while seated", "How it works", { it.seatedComfort }) { p, v -> p.copy(seatedComfort = v) },
    ClassicPreferenceTag("sensory", "Low sensory load", "How it works", { it.lowSensoryLoad }) { p, v -> p.copy(lowSensoryLoad = v) },
    ClassicPreferenceTag("device", "Assistive-device clearance", "How it works", { it.deviceClearance }) { p, v -> p.copy(deviceClearance = v) },
    ClassicPreferenceTag("footwear", "Easy footwear", "How it works", { it.easyFootwear }) { p, v -> p.copy(easyFootwear = v) },
    ClassicPreferenceTag("overhead", "Limited overhead reach", "Movement", { it.limitedOverheadReach }) { p, v -> p.copy(limitedOverheadReach = v) },
    ClassicPreferenceTag("grip", "Grip-friendly handling", "Movement", { it.gripStrength }) { p, v -> p.copy(gripStrength = v) },
    ClassicPreferenceTag("pressure", "Low seam / pressure load", "Movement", { it.seamPressureSensitivity }) { p, v -> p.copy(seamPressureSensitivity = v) },
    ClassicPreferenceTag("removal", "Easy removal", "Movement", { it.easyRemoval }) { p, v -> p.copy(easyRemoval = v) },
    ClassicPreferenceTag("mobility", "Mobility-aid compatibility", "Movement", { it.mobilityAidCompatibility }) { p, v -> p.copy(mobilityAidCompatibility = v) },
    ClassicPreferenceTag("wool", "Avoid wool", "Materials", { it.avoidWool }) { p, v -> p.copy(avoidWool = v) },
    ClassicPreferenceTag("animal", "Animal-free materials", "Materials", { it.animalFree }) { p, v -> p.copy(animalFree = v) },
    ClassicPreferenceTag("stepFree", "Step-free shopping", "Outing", { it.stepFreeAccess }) { p, v -> p.copy(stepFreeAccess = v) },
    ClassicPreferenceTag("energy", "Conserve energy", "Outing", { it.conserveEnergy }) { p, v -> p.copy(conserveEnergy = v) },
    ClassicPreferenceTag("heat", "Heat affects comfort", "Outing", { it.heatSensitive }) { p, v -> p.copy(heatSensitive = v) }
)

/**
 * Category tags for wearability priorities. Selected tags render neatly below
 * as "My preferences" (Plan-style summary).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WearabilityPreferenceTags(
    preferences: WearabilityPreferences,
    onPreferences: (WearabilityPreferences) -> Unit,
    compact: Boolean = false,
    showCatalogCategories: Boolean = true
) {
    val grouped = remember { ClassicPreferenceTags.groupBy { it.category } }
    val selectedClassic = ClassicPreferenceTags.filter { it.getter(preferences) }
    val selectedFilters = preferences.userFilters.filter { it.enabled }
    val selectedLabels = selectedClassic.map { it.label } + selectedFilters.mapNotNull { filter ->
        FilterCatalog.byId(filter.filterId)?.label?.let { label ->
            when (filter.mode) {
                FilterMode.MUST_AVOID -> label
                FilterMode.PREFER -> "$label · Prefer"
                FilterMode.CONDITIONAL -> "$label · If needed"
            }
        }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        grouped.forEach { (category, tags) ->
            Text(category, style = MaterialTheme.typography.titleMedium, color = Olive)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    val on = tag.getter(preferences)
                    PreferenceTagChip(label = tag.label, selected = on) {
                        onPreferences(tag.setter(preferences, !on))
                    }
                }
            }
        }

        if (showCatalogCategories) {
            val highlightCategories = if (compact) {
                listOf(FilterCategory.SENSORY, FilterCategory.REACH, FilterCategory.MATERIALS, FilterCategory.MOBILITY, FilterCategory.OCCASION)
            } else {
                FilterCategory.entries
            }
            highlightCategories.forEach { category ->
                val items = FilterCatalog.byCategory(category).take(if (compact) 6 else 10)
                if (items.isEmpty()) return@forEach
                Text(category.label, style = MaterialTheme.typography.titleMedium, color = Olive)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { definition ->
                        val existing = preferences.userFilters.firstOrNull { it.filterId == definition.id && it.enabled }
                        PreferenceTagChip(
                            label = definition.label,
                            selected = existing != null,
                            detail = existing?.mode?.let {
                                when (it) {
                                    FilterMode.MUST_AVOID -> "Must"
                                    FilterMode.PREFER -> "Prefer"
                                    FilterMode.CONDITIONAL -> "If"
                                }
                            }
                        ) {
                            val next = when {
                                existing == null -> preferences.userFilters.filterNot { it.filterId == definition.id } +
                                    UserFilter(definition.id, mode = FilterMode.MUST_AVOID, enabled = true)
                                existing.mode == FilterMode.MUST_AVOID -> preferences.userFilters.filterNot { it.filterId == definition.id } +
                                    UserFilter(definition.id, mode = FilterMode.PREFER, enabled = true)
                                existing.mode == FilterMode.PREFER -> preferences.userFilters.filterNot { it.filterId == definition.id } +
                                    UserFilter(definition.id, mode = FilterMode.CONDITIONAL, enabled = true)
                                else -> preferences.userFilters.filterNot { it.filterId == definition.id }
                            }
                            onPreferences(FilterCatalog.syncClassicPreferences(preferences.copy(userFilters = next)))
                        }
                    }
                }
            }
        }

        MyPreferencesStrip(selectedLabels)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyPreferencesStrip(selectedLabels: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(OliveLight, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text("My preferences", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        if (selectedLabels.isEmpty()) {
            Text("Tap tags above to build today’s priorities.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedLabels.forEach { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Olive,
                        modifier = Modifier
                            .background(Paper, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StylePreferenceTags(
    title: String,
    options: List<String>,
    selected: List<String>,
    onChange: (List<String>) -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val on = option in selected
                PreferenceTagChip(label = option, selected = on) {
                    onChange(if (on) selected - option else selected + option)
                }
            }
        }
        if (selected.isNotEmpty()) {
            Text("Selected", color = SoftInk, style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.forEach { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Olive,
                        modifier = Modifier
                            .background(OliveLight, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PreferenceTagChip(
    label: String,
    selected: Boolean,
    detail: String? = null,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(if (detail.isNullOrBlank()) label else "$label · $detail")
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Olive,
            selectedLabelColor = Paper,
            containerColor = Paper,
            labelColor = SoftInk
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Hairline,
            selectedBorderColor = Olive
        )
    )
}

/** Union of demo persona color / style / interest tags for the preference hub. */
object PersonaTagCatalog {
    val colors: List<String> = listOf(
        "Sage green", "Muted blue", "Cream", "Earthy tones", "Navy", "Forest green", "Beige", "Neutrals",
        "Mustard", "Charcoal", "Olive", "Black", "White", "Lavender", "Bright blue", "Emerald", "Burgundy", "Pastels", "Muted brown"
    )
    val avoidColors: List<String> = listOf("Neon", "High-contrast patterns", "Loud neons")
    val styles: List<String> = listOf(
        "minimalist streetwear", "relaxed fit", "tagless", "soft cotton", "smart casual", "elegant", "stretch",
        "adjustable waist", "flexible layers", "contemporary", "front closures", "business casual", "easy-care",
        "modest", "loose silhouette", "coverage", "breathable", "casual", "sporty", "sustainable", "oversized", "versatile", "secondhand"
    )
    val interests: List<String> = listOf(
        "campus walking", "concerts", "comfortable sneakers", "office meetings", "commute walking",
        "brace-friendly trousers", "client presentations", "independent dressing", "long office days",
        "dinner after work", "lectures", "family gatherings", "coordinated jewellery", "reuse wardrobe first",
        "cycling", "machine-washable"
    )
}
