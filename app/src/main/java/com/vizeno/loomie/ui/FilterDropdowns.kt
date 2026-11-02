package com.vizeno.loomie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.model.ClothingTypeOptions
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.ui.theme.SoftInk

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SimpleDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
internal fun ClothingTypeDropdown(
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val label = ClothingTypeOptions.all.firstOrNull { it.first == selectedKey }?.second ?: "All types"
    SimpleDropdown(
        label = "Clothing type",
        options = ClothingTypeOptions.all.map { it.second },
        selected = label,
        onSelect = { chosen ->
            onSelect(ClothingTypeOptions.all.firstOrNull { it.second == chosen }?.first)
        },
        modifier = modifier
    )
}

@Composable
internal fun ForYouDropdown(
    suggestions: List<Garment>,
    selectedId: String?,
    onSelect: (Garment?) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("For you · browse all") + suggestions.map { it.name }
    val selected = suggestions.firstOrNull { it.id == selectedId }?.name ?: "For you · browse all"
    SimpleDropdown(
        label = "For you",
        options = options,
        selected = selected,
        onSelect = { chosen ->
            onSelect(suggestions.firstOrNull { it.name == chosen })
        },
        modifier = modifier
    )
}

@Composable
internal fun ClothingAndForYouFilters(
    clothingType: String?,
    onClothingType: (String?) -> Unit,
    suggestions: List<Garment>,
    forYouId: String?,
    onForYou: (Garment?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClothingTypeDropdown(clothingType, onClothingType, Modifier.weight(1f))
            ForYouDropdown(suggestions, forYouId, onForYou, Modifier.weight(1f))
        }
        Text(
            "Type filters the list · For you jumps to a suggested piece",
            color = SoftInk,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

internal fun garmentMatchesClothingType(garment: Garment, typeKey: String?): Boolean {
    if (typeKey.isNullOrBlank()) return true
    val corpus = "${garment.name} ${garment.subtitle} ${garment.category.label} ${garment.category.name}".lowercase()
    return when (typeKey.lowercase()) {
        "shirt", "top" -> garment.category.name == "TOP" || corpus.contains("shirt") || corpus.contains("tee") || corpus.contains("blouse")
        "pant", "trousers", "bottom" -> garment.category.name == "BOTTOM" || corpus.contains("pant") || corpus.contains("trouser") || corpus.contains("jean")
        "dress" -> corpus.contains("dress")
        "skirt" -> corpus.contains("skirt")
        "jacket", "outerwear" -> garment.category.name == "OUTERWEAR" || corpus.contains("jacket") || corpus.contains("coat") || corpus.contains("layer")
        "shoes" -> garment.category.name == "SHOES" || corpus.contains("shoe") || corpus.contains("sneaker")
        "accessory" -> garment.category.name == "ACCESSORY"
        else -> corpus.contains(typeKey.lowercase())
    }
}
