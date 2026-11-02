package com.vizeno.loomie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.model.FilterCatalog
import com.vizeno.loomie.model.FilterCategory
import com.vizeno.loomie.model.FilterDefinition
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.UserFilter
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.ui.theme.Brass
import com.vizeno.loomie.ui.theme.Olive
import com.vizeno.loomie.ui.theme.OliveLight
import com.vizeno.loomie.ui.theme.Paper
import com.vizeno.loomie.ui.theme.SoftInk

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterLibrarySection(
    preferences: WearabilityPreferences,
    onPreferences: (WearabilityPreferences) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(setOf<FilterCategory>()) }
    val activeIds = preferences.userFilters.filter { it.enabled }.map { it.filterId }.toSet()
    val visible = remember(query) { FilterCatalog.search(query) }

    fun upsert(definition: FilterDefinition, mode: FilterMode = FilterMode.MUST_AVOID) {
        val without = preferences.userFilters.filterNot { it.filterId == definition.id }
        val nextFilters = without + UserFilter(definition.id, mode = mode, enabled = true)
        onPreferences(FilterCatalog.syncClassicPreferences(preferences.copy(userFilters = nextFilters)))
    }

    fun remove(id: String) {
        val nextFilters = preferences.userFilters.filterNot { it.filterId == id }
        onPreferences(preferences.copy(userFilters = nextFilters))
    }

    fun cycle(definition: FilterDefinition) {
        val existing = preferences.userFilters.firstOrNull { it.filterId == definition.id && it.enabled }
        when {
            existing == null -> upsert(definition, FilterMode.MUST_AVOID)
            existing.mode == FilterMode.MUST_AVOID -> upsert(definition, FilterMode.PREFER)
            existing.mode == FilterMode.PREFER -> upsert(definition, FilterMode.CONDITIONAL)
            else -> remove(definition.id)
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Text("Filter library", style = MaterialTheme.typography.titleLarge)
        Text(
            "Must / Prefer / Conditional. Evaluable filters can match verified fields. Needs evidence never matches or rank-boosts.",
            color = SoftInk,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(10.dp))
        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search ${FilterCatalog.all.size} filters") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) }
        )
        Spacer(Modifier.height(12.dp))
        FilterCategory.entries.forEach { category ->
            val items = visible.filter { it.category == category }
            if (items.isEmpty()) return@forEach
            val open = category in expanded || query.isNotBlank()
            Row(
                Modifier.fillMaxWidth().clickable {
                    expanded = if (category in expanded) expanded - category else expanded + category
                }.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(category.label, style = MaterialTheme.typography.titleMedium)
                    Text("${items.size} filters · ${items.count { it.id in activeIds }} active", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                }
                Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null, tint = Olive)
            }
            if (open) {
                items.forEach { definition ->
                    val selected = definition.id in activeIds
                    val mode = preferences.userFilters.firstOrNull { it.filterId == definition.id }?.mode
                    Row(
                        Modifier.fillMaxWidth().clickable { cycle(definition) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(definition.label, style = MaterialTheme.typography.bodyLarge)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                EvidenceBadge(evaluable = definition.evaluable)
                                if (selected) Text(modeLabel(mode), color = SoftInk, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(if (selected) "On" else "Off", color = Olive, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("${activeIds.size} active catalog filters", color = SoftInk, style = MaterialTheme.typography.labelMedium)
    }
}

private fun modeLabel(mode: FilterMode?): String = when (mode) {
    FilterMode.MUST_AVOID -> "Must"
    FilterMode.PREFER -> "Prefer"
    FilterMode.CONDITIONAL -> "Conditional"
    null -> ""
}

@Composable
private fun EvidenceBadge(evaluable: Boolean) {
    Text(
        if (evaluable) "Evaluable" else "Needs evidence",
        color = if (evaluable) Olive else Brass,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .background(if (evaluable) OliveLight else Paper, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
