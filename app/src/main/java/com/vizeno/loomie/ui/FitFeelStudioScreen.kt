package com.vizeno.loomie.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt
import com.vizeno.loomie.data.LocalProfileStore
import com.vizeno.loomie.data.LookWeaveRepository
import com.vizeno.loomie.data.SamplePersonProfiles
import com.vizeno.loomie.data.TryOnCompositeCache
import com.vizeno.loomie.domain.ConstraintSolver
import com.vizeno.loomie.integration.LookWeaveGateway
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.model.GarmentCategory
import com.vizeno.loomie.model.OutfitPlan
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.model.WearabilityRule
import com.vizeno.loomie.model.WearabilityRuleKey
import com.vizeno.loomie.model.WearabilityRuleKind
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableStateListOf
import com.vizeno.loomie.ui.theme.Hairline
import com.vizeno.loomie.ui.theme.Ink
import com.vizeno.loomie.ui.theme.Olive
import com.vizeno.loomie.ui.theme.OliveLight
import com.vizeno.loomie.ui.theme.Paper
import com.vizeno.loomie.ui.theme.Parchment
import com.vizeno.loomie.ui.theme.SoftInk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private enum class FitStudioTab(val label: String) {
    COMFORT("Comfort Map"),
    PROFILE_SIZES("Profile sizes"),
    ANALYSIS("Fit Analysis"),
    FIX("Fix / Save"),
    INSIGHTS("Insights")
}

/** Stage image mode for comfort / analysis — independent of studio panel tabs. */
private enum class FitImageMode(val label: String) {
    ORIGINAL("Original"),
    FIT_PREVIEW("Fit preview"),
    FACELESS("Faceless")
}

@Composable
private fun OliveFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = enabled,
        label = label,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Olive,
            selectedLabelColor = Paper,
            containerColor = Paper,
            labelColor = Ink,
            disabledContainerColor = OliveLight,
            disabledLabelColor = SoftInk
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = Hairline,
            selectedBorderColor = Olive
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FitFeelStudioScreen(
    preferences: WearabilityPreferences,
    plan: OutfitPlan,
    onPreferences: (WearabilityPreferences) -> Unit,
    onPlan: (OutfitPlan) -> Unit,
    onSaved: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileWardrobe = remember(LocalProfileStore.activeProfileId(context), LookWeaveRepository.importedGarments.size) {
        SamplePersonProfiles.wardrobeForActiveProfile(context)
    }
    var outfit by remember {
        mutableStateOf(plan.resolved.ifEmpty { profileWardrobe.take(5) })
    }
    var selectedRegion by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGarmentId by rememberSaveable { mutableStateOf(outfit.firstOrNull()?.id) }
    var noteChip by rememberSaveable { mutableStateOf("Uncomfortable while sitting") }
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var personBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var facelessBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var previewBusy by remember { mutableStateOf(false) }
    var previewError by remember { mutableStateOf<String?>(null) }
    var previewCacheKey by rememberSaveable { mutableStateOf<String?>(null) }
    /** Person URI used as the current composite base (stacks accessories). */
    var compositePersonUri by rememberSaveable { mutableStateOf<String?>(null) }
    val accessoryLayers = remember { mutableStateListOf<String>() }
    var analysisDetail by rememberSaveable { mutableStateOf<String?>(null) }
    var fixSuggestion by remember { mutableStateOf<OutfitPlan?>(null) }
    var insightDraft by remember { mutableStateOf<WearabilityRule?>(null) }
    var tab by rememberSaveable { mutableStateOf(FitStudioTab.COMFORT.name) }
    var imageMode by rememberSaveable { mutableStateOf(FitImageMode.ORIGINAL.name) }
    var hotspots by remember { mutableStateOf<List<RegionHotspot>>(emptyList()) }
    var poseUseOverlay by remember { mutableStateOf(false) }
    var poseBusy by remember { mutableStateOf(false) }
    var facelessBusy by remember { mutableStateOf(false) }
    // Fit studio body image only — never reuse Face portrait as the body stage by default.
    var bodyUri by rememberSaveable {
        mutableStateOf(
            run {
                val face = LocalProfileStore.faceUri(context)
                fun usableBody(candidate: String?): String? =
                    candidate?.takeIf { it.isNotBlank() && it != face }
                usableBody(LocalProfileStore.bodyUri(context))
                    ?: File(context.filesDir, "body.jpg").takeIf { it.exists() }?.let { file ->
                        usableBody(Uri.fromFile(file).toString())?.also {
                            LocalProfileStore.saveBodyUri(context, it)
                        }
                    }
                    ?: File(context.filesDir, "fit-person.jpg").takeIf { it.exists() }?.let { file ->
                        usableBody(Uri.fromFile(file).toString())?.also {
                            LocalProfileStore.saveBodyUri(context, it)
                        }
                    }
            }
        )
    }

    val facelessFile = remember { File(context.cacheDir, "lookweave-mannequin.png") }
    // Prefer saved body photo; never fall back to faceUri. Mannequin only if user already made one.
    val personUri = when {
        !bodyUri.isNullOrBlank() && bodyUri != LocalProfileStore.faceUri(context) -> bodyUri
        else -> null
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val saved = withContext(Dispatchers.IO) {
                LocalProfileStore.copyBodyToAppFiles(context, uri)
            }
            bodyUri = saved ?: uri.toString().also {
                LocalProfileStore.saveUserBodyUri(context, it)
            }
            // New person image: drop in-memory preview; disk invalidated in copyBodyToAppFiles / saveUserBodyUri.
            previewBitmap = null
            previewCacheKey = null
            compositePersonUri = null
            accessoryLayers.clear()
            personBitmap = null
            facelessBitmap = null
            hotspots = emptyList()
            poseUseOverlay = false
            imageMode = FitImageMode.ORIGINAL.name
        }
    }

    fun garmentStackIds(stack: Boolean, garment: Garment): List<String> =
        if (stack) accessoryLayers.toList() + garment.id else listOf(garment.id)

    fun applyCacheHit(hit: TryOnCompositeCache.CacheHit, stack: Boolean, garment: Garment, isAccessory: Boolean) {
        previewBitmap = hit.bitmap
        previewCacheKey = hit.cacheKey
        compositePersonUri = hit.fileUri
            ?: TryOnCompositeCache.diskFile(context, hit.cacheKey).takeIf { it.exists() }?.let { Uri.fromFile(it).toString() }
        if (stack && garment.id !in accessoryLayers) accessoryLayers += garment.id
        if (!stack) {
            accessoryLayers.clear()
            if (isAccessory) accessoryLayers += garment.id
        }
        imageMode = FitImageMode.FIT_PREVIEW.name
    }

    fun runTryOn(garment: Garment, stackAccessory: Boolean) {
        val basePerson = personUri ?: return
        val garmentUri = garmentTryOnUri(context.packageName, garment) ?: run {
            previewError = "This piece has no image for try-on."
            return
        }
        val isAccessory = garment.category == GarmentCategory.ACCESSORY || garment.category == GarmentCategory.SHOES
        val stack = stackAccessory && isAccessory
        val stackIds = garmentStackIds(stack, garment)
        // memory → disk → pre-seeded assets
        val cached = TryOnCompositeCache.load(context, basePerson, stackIds)
        if (cached != null) {
            applyCacheHit(cached, stack, garment, isAccessory)
            return
        }
        val layerKey = TryOnCompositeCache.cacheKey(
            TryOnCompositeCache.personKey(context, basePerson),
            stackIds
        )
        if (layerKey == previewCacheKey && previewBitmap != null) {
            imageMode = FitImageMode.FIT_PREVIEW.name
            return
        }
        val sourceUri = if (stack && !compositePersonUri.isNullOrBlank()) {
            Uri.parse(compositePersonUri)
        } else {
            Uri.parse(basePerson)
        }
        previewBusy = true
        previewError = null
        scope.launch {
            runCatching {
                val task = LookWeaveGateway.createPreview(
                    context.contentResolver,
                    sourceUri,
                    garmentUri,
                    when (garment.category) {
                        GarmentCategory.SHOES -> "shoes"
                        GarmentCategory.ACCESSORY -> "auto"
                        GarmentCategory.OUTERWEAR -> "outer"
                        GarmentCategory.BOTTOM -> "lower_body"
                        GarmentCategory.TOP -> "upper_body"
                        else -> garment.category.name.lowercase()
                    }
                )
                val bytes = LookWeaveGateway.download(task.imageUrl)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: error("Preview decode failed")
            }.onSuccess { bitmap ->
                previewBitmap = bitmap
                previewCacheKey = layerKey
                val saved = withContext(Dispatchers.IO) {
                    val uri = TryOnCompositeCache.put(context, basePerson, stackIds, bitmap)
                    // Also store under built-in alias (person1…6) so offline seeds match.
                    val source = LocalProfileStore.bodySource(context)
                    if (TryOnCompositeCache.isBuiltInPersonAlias(source)) {
                        TryOnCompositeCache.put(
                            context,
                            TryOnCompositeCache.cacheKey(source!!, stackIds),
                            bitmap
                        )
                    }
                    uri
                }
                compositePersonUri = saved
                if (stack) {
                    if (garment.id !in accessoryLayers) accessoryLayers += garment.id
                } else {
                    accessoryLayers.clear()
                    if (isAccessory) accessoryLayers += garment.id
                }
                imageMode = FitImageMode.FIT_PREVIEW.name
            }.onFailure { previewError = it.message ?: "Preview unavailable" }
            previewBusy = false
        }
    }

    LaunchedEffect(plan.resolved.map { it.id }.joinToString()) {
        if (plan.resolved.isNotEmpty()) outfit = plan.resolved
        if (fixSuggestion == null && plan.replacement != null) fixSuggestion = plan
    }

    // Keep the comfort form actionable whenever the active plan/profile
    // changes. A previously selected garment may no longer be in the new
    // candidate list, so fall back to the first available item.
    LaunchedEffect(outfit.map { it.id }.joinToString()) {
        if (outfit.none { it.id == selectedGarmentId }) {
            selectedGarmentId = outfit.firstOrNull()?.id
        }
    }

    LaunchedEffect(personUri) {
        val sourceUri = personUri
        if (sourceUri.isNullOrBlank()) {
            personBitmap = null
            facelessBitmap = null
            hotspots = emptyList()
            poseUseOverlay = false
            return@LaunchedEffect
        }
        poseBusy = true
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use {
                    BitmapFactory.decodeStream(it, null, bounds)
                }
                val sample = maxOf(1, maxOf(bounds.outWidth, bounds.outHeight) / 900)
                val options = BitmapFactory.Options().apply { inSampleSize = sample }
                context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }
            }.getOrNull()
        }
        personBitmap = loaded
        facelessBitmap = null
        // Always map comfort regions on the original body photo (not try-on overlay).
        if (loaded != null) {
            val result = withContext(Dispatchers.Default) { detectPoseRegions(loaded) }
            hotspots = result.hotspots
            poseUseOverlay = result.useOverlay
        } else {
            hotspots = emptyList()
            poseUseOverlay = false
        }
        poseBusy = false
    }

    val activeImageMode = FitImageMode.entries.firstOrNull { it.name == imageMode } ?: FitImageMode.ORIGINAL

    LaunchedEffect(activeImageMode, personBitmap, previewBitmap) {
        if (activeImageMode != FitImageMode.FACELESS) return@LaunchedEffect
        if (facelessBitmap != null || personBitmap == null) return@LaunchedEffect
        facelessBusy = true
        facelessBitmap = withContext(Dispatchers.Default) {
            createLocalMannequin(personBitmap!!)
                ?: personBitmap // graceful fallback if face not found
        }
        // Persist so Style studio / empty stage can reuse.
        runCatching {
            facelessBitmap?.let { bmp ->
                withContext(Dispatchers.IO) {
                    java.io.FileOutputStream(facelessFile).use { out ->
                        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
            }
        }
        facelessBusy = false
    }

    // When a try-on lands, surface Fit preview mode automatically.
    LaunchedEffect(previewBitmap) {
        if (previewBitmap != null && activeImageMode == FitImageMode.ORIGINAL) {
            imageMode = FitImageMode.FIT_PREVIEW.name
        }
    }

    val breakdown = remember(outfit, preferences) {
        ConstraintSolver.wearabilityBreakdown(outfit, preferences, plan.occasion)
    }
    val observations = LookWeaveRepository.observations
    val confirmedInsights = remember(observations, preferences.customRules) {
        observations.groupBy { "${it.region ?: it.task}|${it.difficulty}" }
            .entries
            .sortedByDescending { it.value.size }
            .take(6)
            .map { (key, values) ->
                val (task, difficulty) = key.split("|", limit = 2)
                val label = "$task felt ${difficulty.lowercase()}"
                val existing = preferences.customRules.firstOrNull {
                    it.confirmed && it.label.contains(task.substringBefore(" ·"), true)
                }
                Triple(label, values.size, existing)
            }
    }
    val candidates = remember(outfit, fixSuggestion, profileWardrobe) {
        (profileWardrobe + outfit + listOfNotNull(fixSuggestion?.replacement))
            .distinctBy { it.id }
    }
    val activeTab = FitStudioTab.entries.firstOrNull { it.name == tab } ?: FitStudioTab.COMFORT

    Column(Modifier.fillMaxSize().background(Parchment)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(36.dp).background(OliveLight, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Science, null, tint = Olive, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("FIT & FEEL", style = MaterialTheme.typography.labelSmall, color = SoftInk)
                Text("Studio", style = MaterialTheme.typography.titleLarge, color = Ink)
            }
            if (fixSuggestion?.replacement != null) {
                Text("Fix ready", color = Olive, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.background(OliveLight, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable { tab = FitStudioTab.FIX.name })
            }
        }

        // Image mode: Original body | Fit try-on | Faceless redaction
        if (personBitmap != null || previewBitmap != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FitImageMode.entries.forEach { mode ->
                    OliveFilterChip(
                        selected = activeImageMode == mode,
                        onClick = { imageMode = mode.name },
                        enabled = when (mode) {
                            FitImageMode.ORIGINAL -> personBitmap != null
                            FitImageMode.FIT_PREVIEW -> previewBitmap != null || personBitmap != null
                            FitImageMode.FACELESS -> personBitmap != null
                        },
                        label = { Text(mode.label) }
                    )
                }
            }
        }

        // Center stage — person / outfit image (no page scroll)
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Paper)
                .border(1.dp, Hairline, RoundedCornerShape(18.dp))
        ) {
            val stageBitmap = when (activeImageMode) {
                FitImageMode.ORIGINAL -> personBitmap
                FitImageMode.FIT_PREVIEW -> previewBitmap ?: personBitmap
                FitImageMode.FACELESS -> facelessBitmap ?: personBitmap
            }
            // Pose rings always map against the original body photo dimensions/landmarks.
            val poseImage = personBitmap
            when {
                stageBitmap != null -> {
                    // Full body / zoomed out — letterbox OK; never Crop to face.
                    Image(
                        stageBitmap.asImageBitmap(),
                        contentDescription = when (activeImageMode) {
                            FitImageMode.ORIGINAL -> "Original body photo"
                            FitImageMode.FIT_PREVIEW -> "Fit try-on preview"
                            FitImageMode.FACELESS -> "Faceless body photo"
                        },
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                    val showComfortOverlay =
                        (activeTab == FitStudioTab.COMFORT || activeTab == FitStudioTab.ANALYSIS) &&
                            activeImageMode != FitImageMode.FIT_PREVIEW
                    if (showComfortOverlay && poseUseOverlay && hotspots.isNotEmpty() && poseImage != null) {
                        PoseHotspotOverlay(
                            hotspots = hotspots,
                            imageWidth = poseImage.width,
                            imageHeight = poseImage.height,
                            selected = selectedRegion,
                            onSelect = { region ->
                                selectedRegion = region.label
                                val preferred = outfit.firstOrNull {
                                    it.category.name == regionSuggestedGarmentCategory(region)
                                } ?: outfit.firstOrNull()
                                preferred?.let { selectedGarmentId = it.id }
                            }
                        )
                    }
                    if (showComfortOverlay && (!poseUseOverlay || hotspots.isEmpty()) && !poseBusy) {
                        RegionChipFallback(
                            selected = selectedRegion,
                            onSelect = { region ->
                                selectedRegion = region.label
                                val preferred = outfit.firstOrNull {
                                    it.category.name == regionSuggestedGarmentCategory(region)
                                } ?: outfit.firstOrNull()
                                preferred?.let { selectedGarmentId = it.id }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp)
                        )
                    }
                }
                previewBusy || facelessBusy -> LinearProgressIndicator(
                    Modifier.fillMaxWidth().align(Alignment.Center).padding(24.dp),
                    color = Olive,
                    trackColor = OliveLight
                )
                else -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Checkroom, null, tint = Olive, modifier = Modifier.size(40.dp))
                    Text("Upload a full-body photo", color = SoftInk, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Fit studio needs a zoomed-out body shot. Face portraits stay on the Face tab.",
                        color = SoftInk,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { picker.launch("image/*") }) { Text("Upload body photo") }
                        OutlinedButton(onClick = {
                            if (LocalProfileStore.seedSamplePerson1IfNeeded(context, force = true)) {
                                bodyUri = LocalProfileStore.bodyUri(context)
                                personBitmap = null
                            }
                        }) { Text("Use sample Noah") }
                        if (facelessFile.exists()) {
                            OutlinedButton(onClick = {
                                val uri = Uri.fromFile(facelessFile).toString()
                                bodyUri = uri
                                LocalProfileStore.saveBodyUri(context, uri)
                            }) { Text("Use mannequin") }
                        }
                    }
                }
            }
            if (previewError != null) {
                Text(
                    previewError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(10.dp)
                        .background(Paper.copy(alpha = .92f), RoundedCornerShape(8.dp)).padding(8.dp)
                )
            }
            if ((poseBusy || facelessBusy) && stageBitmap != null) {
                Text(
                    if (facelessBusy) "Redacting face…" else "Mapping body regions…",
                    color = SoftInk,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                        .background(Paper.copy(alpha = .9f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Bottom candidate strip
        LazyRow(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(candidates, key = { it.id }) { garment ->
                CandidateThumb(
                    garment = garment,
                    selected = selectedGarmentId == garment.id,
                    layered = garment.id in accessoryLayers,
                    onClick = {
                        selectedGarmentId = garment.id
                        if (personUri == null) {
                            previewError = "Add a person photo first."
                            return@CandidateThumb
                        }
                        val isAccessory = garment.category == GarmentCategory.ACCESSORY ||
                            garment.category == GarmentCategory.SHOES
                        if (isAccessory && garment.id in accessoryLayers) {
                            // Remove this accessory layer and fall back to base photo / prior cache.
                            accessoryLayers.remove(garment.id)
                            if (accessoryLayers.isEmpty()) {
                                compositePersonUri = null
                                previewBitmap = null
                                previewCacheKey = null
                                imageMode = FitImageMode.ORIGINAL.name
                            } else {
                                val cached = TryOnCompositeCache.load(
                                    context,
                                    personUri,
                                    accessoryLayers.toList()
                                )
                                if (cached != null) {
                                    previewBitmap = cached.bitmap
                                    previewCacheKey = cached.cacheKey
                                    compositePersonUri = cached.fileUri
                                        ?: TryOnCompositeCache.diskFile(context, cached.cacheKey)
                                            .takeIf { it.exists() }
                                            ?.let { Uri.fromFile(it).toString() }
                                    imageMode = FitImageMode.FIT_PREVIEW.name
                                } else {
                                    compositePersonUri = personUri
                                    previewBitmap = null
                                    previewCacheKey = null
                                    imageMode = FitImageMode.ORIGINAL.name
                                    onSaved("Layer removed — re-apply remaining accessories to rebuild the look")
                                }
                            }
                            return@CandidateThumb
                        }
                        runTryOn(garment, stackAccessory = isAccessory && (previewBitmap != null || !compositePersonUri.isNullOrBlank()))
                        if (fixSuggestion?.replacement?.id == garment.id) tab = FitStudioTab.FIX.name
                    }
                )
            }
        }

        // Tabs — panel switch, same stage
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FitStudioTab.entries.forEach { entry ->
                OliveFilterChip(
                    selected = activeTab == entry,
                    onClick = { tab = entry.name },
                    label = { Text(entry.label) }
                )
            }
        }

        // Panel content (local scroll only inside panel, not whole page)
        Box(
            Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(Paper, RoundedCornerShape(14.dp))
                .border(1.dp, Hairline, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            when (activeTab) {
                FitStudioTab.COMFORT -> ComfortPanel(
                    selectedRegion = selectedRegion,
                    selectedGarmentId = selectedGarmentId,
                    outfit = outfit,
                    noteChip = noteChip,
                    onRegion = { selectedRegion = it },
                    onGarment = { selectedGarmentId = it },
                    onNote = { noteChip = it },
                    onSave = {
                        val garmentId = selectedGarmentId ?: return@ComfortPanel
                        val region = selectedRegion ?: "General comfort"
                        scope.launch {
                            runCatching {
                                LookWeaveGateway.recordObservation(garmentId, "$region · $noteChip", 0, "Some effort")
                            }.onSuccess {
                                LookWeaveRepository.observations // refresh if mutable
                                onSaved("Comfort note saved for $region")
                                val next = ConstraintSolver.plan(
                                    plan.occasion,
                                    preferences,
                                    SamplePersonProfiles.wardrobeForActiveProfile(context),
                                    LocalProfileStore.genderLabel(context)
                                )
                                if (next.replacement != null) {
                                    fixSuggestion = next
                                    tab = FitStudioTab.FIX.name
                                }
                            }.onFailure { onSaved(it.message ?: "Could not save note") }
                        }
                    }
                )
                FitStudioTab.PROFILE_SIZES -> {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Gender, sizes & preference tags for discovery and Loomie", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                        PreferenceHubSection(
                            context = context,
                            preferences = preferences,
                            onPreferences = onPreferences
                        )
                    }
                }
                FitStudioTab.ANALYSIS -> AnalysisPanel(breakdown, analysisDetail) { analysisDetail = it }
                FitStudioTab.FIX -> FixPanel(
                    suggestion = fixSuggestion,
                    onCheck = {
                        fixSuggestion = ConstraintSolver.plan(
                            plan.occasion,
                            preferences,
                            SamplePersonProfiles.wardrobeForActiveProfile(context),
                            LocalProfileStore.genderLabel(context)
                        )
                    },
                    onTry = { suggestion ->
                        onPlan(suggestion)
                        outfit = suggestion.resolved
                        selectedGarmentId = suggestion.replacement?.id ?: selectedGarmentId
                        fixSuggestion = null
                        onSaved("Outfit updated — open YouCam preview from a candidate below")
                        suggestion.replacement?.let { runTryOn(it, stackAccessory = false) }
                    },
                    onKeep = { fixSuggestion = null },
                    onExplain = { analysisDetail = it; tab = FitStudioTab.ANALYSIS.name }
                )
                FitStudioTab.INSIGHTS -> InsightsPanel(
                    insights = confirmedInsights,
                    draft = insightDraft,
                    onDraft = { insightDraft = it },
                    onConfirm = { draft ->
                        scope.launch {
                            runCatching { LookWeaveGateway.createRule(draft.copy(confirmed = true)) }
                                .onSuccess { saved ->
                                    onPreferences(preferences.copy(customRules = preferences.customRules + saved))
                                    insightDraft = null
                                    onSaved("Personal rule saved")
                                }
                                .onFailure {
                                    val local = draft.copy(id = UUID.randomUUID().toString(), confirmed = true, enabled = true)
                                    onPreferences(preferences.copy(customRules = preferences.customRules + local))
                                    insightDraft = null
                                    onSaved("Personal rule saved on device")
                                }
                        }
                    },
                    onCancel = { insightDraft = null }
                )
            }
        }
    }
}

@Composable
private fun PoseHotspotOverlay(
    hotspots: List<RegionHotspot>,
    imageWidth: Int,
    imageHeight: Int,
    selected: String?,
    onSelect: (BodyRegion) -> Unit
) {
    // Match ContentScale.Fit + 8.dp padding used by the stage Image (letterbox offsets).
    BoxWithConstraints(Modifier.fillMaxSize().padding(8.dp)) {
        val boxW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val boxH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val imgW = imageWidth.toFloat().coerceAtLeast(1f)
        val imgH = imageHeight.toFloat().coerceAtLeast(1f)
        val scale = min(boxW / imgW, boxH / imgH)
        val drawnW = imgW * scale
        val drawnH = imgH * scale
        val offsetX = (boxW - drawnW) / 2f
        val offsetY = (boxH - drawnH) / 2f

        fun mapX(nx: Float) = offsetX + drawnW * nx
        fun mapY(ny: Float) = offsetY + drawnH * ny
        val density = LocalDensity.current

        Canvas(Modifier.fillMaxSize()) {
            hotspots.forEach { spot ->
                val center = Offset(mapX(spot.x), mapY(spot.y))
                val radius = min(drawnW, drawnH) * spot.radiusFraction
                val active = selected == spot.region.label
                val stroke = if (active) Olive.copy(alpha = .95f) else Olive.copy(alpha = .55f)
                // Small hollow rings at landmark averages — not filled pills.
                drawCircle(
                    color = Paper.copy(alpha = if (active) .35f else .18f),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = stroke,
                    radius = radius,
                    center = center,
                    style = Stroke(width = if (active) 2.6f.dp.toPx() else 2f.dp.toPx())
                )
            }
        }
        hotspots.forEach { spot ->
            val hitPx = min(drawnW, drawnH) * spot.radiusFraction * 2.8f
            val sizeHit = with(density) { hitPx.toDp() }
            val left = (mapX(spot.x) - hitPx / 2f).roundToInt()
            val top = (mapY(spot.y) - hitPx / 2f).roundToInt()
            Box(
                Modifier
                    .offset { IntOffset(left, top) }
                    .size(sizeHit)
                    .clip(CircleShape)
                    .clickable { onSelect(spot.region) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RegionChipFallback(
    selected: String?,
    onSelect: (BodyRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .background(Paper.copy(alpha = .92f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("Regions", color = SoftInk, style = MaterialTheme.typography.labelSmall)
        BodyRegion.entries.forEach { region ->
            OliveFilterChip(
                selected = selected == region.label,
                onClick = { onSelect(region) },
                label = { Text(region.label) }
            )
        }
    }
}

@Composable
private fun CandidateThumb(
    garment: Garment,
    selected: Boolean,
    layered: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .width(78.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                if (selected || layered) 2.dp else 1.dp,
                when {
                    layered -> Olive
                    selected -> Olive
                    else -> Hairline
                },
                RoundedCornerShape(12.dp)
            )
            .background(if (selected || layered) OliveLight else Paper)
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(8.dp)).background(Parchment),
            contentAlignment = Alignment.Center
        ) {
            GarmentProductImage(garment, Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(4.dp))
        Text(garment.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = Ink)
        Text(
            when {
                layered -> "Layered · tap off"
                selected -> "Selected"
                else -> garment.category.label
            },
            color = if (selected || layered) Olive else SoftInk,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ComfortPanel(
    selectedRegion: String?,
    selectedGarmentId: String?,
    outfit: List<Garment>,
    noteChip: String,
    onRegion: (String) -> Unit,
    onGarment: (String) -> Unit,
    onNote: (String) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    var sizes by remember { mutableStateOf(LocalProfileStore.loadSizes(context)) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Tap a highlighted region on the preview, then save a short note.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BodyRegion.entries.forEach { region ->
                OliveFilterChip(selected = selectedRegion == region.label, onClick = { onRegion(region.label) }, label = { Text(region.label) })
            }
        }
        selectedRegion?.let { region ->
            OutlinedTextField(
                value = sizes.measurement(region),
                onValueChange = { next ->
                    sizes = sizes.withMeasurement(region, next)
                    LocalProfileStore.saveSizes(context, sizes)
                },
                label = { Text("$region measurement (optional)") },
                placeholder = { Text("e.g. 16 in / 41 cm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(outfit, key = { it.id }) { garment ->
                OliveFilterChip(
                    selected = selectedGarmentId == garment.id,
                    onClick = { onGarment(garment.id) },
                    label = { Text(garment.name, maxLines = 1) }
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Uncomfortable while sitting", "Hard to fasten", "Pressure mark", "Restricted movement", "Sensory overload").forEach { note ->
                OliveFilterChip(selected = noteChip == note, onClick = { onNote(note) }, label = { Text(note) })
            }
        }
        OutlinedTextField(
            value = noteChip,
            onValueChange = { onNote(it.take(180)) },
            label = { Text("Custom comfort note") },
            placeholder = { Text("Describe what you felt and during which movement") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            onClick = onSave,
            // A body-region is useful context but should not block a custom
            // comfort note; users can save an overall observation as well.
            enabled = selectedGarmentId != null && noteChip.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save comfort note") }
    }
}

@Composable
private fun AnalysisPanel(
    breakdown: Map<String, Pair<Int, Int>>,
    detail: String?,
    onDetail: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Verified garment fields and your notes — unknown stays blank.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        breakdown.forEach { (label, counts) ->
            val (ok, known) = counts
            Column(
                Modifier.fillMaxWidth().clickable {
                    onDetail(
                        if (known == 0) "$label: no verified evidence yet."
                        else "$label: $ok of $known verified checks look workable for the current outfit."
                    )
                }
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.titleSmall)
                    Text(if (known == 0) "Unknown" else "$ok / $known", color = SoftInk, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { if (known == 0) 0f else ok.toFloat() / known.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Olive,
                    trackColor = OliveLight
                )
            }
        }
        detail?.let { Text(it, color = Ink, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun FixPanel(
    suggestion: OutfitPlan?,
    onCheck: () -> Unit,
    onTry: (OutfitPlan) -> Unit,
    onKeep: () -> Unit,
    onExplain: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (suggestion?.replacement == null) {
            Text("Save a comfort note or check for a category swap.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onCheck, modifier = Modifier.fillMaxWidth()) { Text("Check for an alternative") }
        } else {
            Text("Loomie found an alternative", style = MaterialTheme.typography.titleMedium)
            Text(
                "${suggestion.replacement!!.name} could replace ${suggestion.replaced?.name ?: "a conflicting piece"}.",
                color = SoftInk,
                style = MaterialTheme.typography.bodySmall
            )
            Text(suggestion.explanation, style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onTry(suggestion) }) { Text("YouCam preview") }
                OutlinedButton(onClick = onKeep) { Text("Keep") }
                TextButton(onClick = { onExplain(suggestion.explanation) }) { Text("Explain") }
            }
        }
    }
}

@Composable
private fun InsightsPanel(
    insights: List<Triple<String, Int, WearabilityRule?>>,
    draft: WearabilityRule?,
    onDraft: (WearabilityRule) -> Unit,
    onConfirm: (WearabilityRule) -> Unit,
    onCancel: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (insights.isEmpty()) {
            Text("Insights appear after you save comfort notes.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        } else {
            insights.forEach { (pattern, count, existing) ->
                Column(Modifier.fillMaxWidth().background(OliveLight, RoundedCornerShape(10.dp)).padding(10.dp)) {
                    Text("$pattern ($count)", style = MaterialTheme.typography.bodyMedium)
                    if (existing != null) {
                        Text("Confirmed rule: ${existing.label}", color = Olive, style = MaterialTheme.typography.labelSmall)
                    } else {
                        TextButton(onClick = {
                            onDraft(
                                WearabilityRule(
                                    id = "draft-${UUID.randomUUID()}",
                                    label = pattern.substringBefore(" (").replaceFirstChar { it.uppercase() },
                                    key = when {
                                        pattern.contains("fasten", true) -> WearabilityRuleKey.ONE_HAND_CLOSURE
                                        pattern.contains("sitting", true) -> WearabilityRuleKey.SEATED_COMFORT
                                        pattern.contains("Sensory", true) -> WearabilityRuleKey.LOW_SENSORY_LOAD
                                        pattern.contains("Pressure", true) -> WearabilityRuleKey.SEAM_PRESSURE_SENSITIVITY
                                        else -> WearabilityRuleKey.EASY_REMOVAL
                                    },
                                    kind = WearabilityRuleKind.SOFT,
                                    confirmed = false,
                                    source = "Fit insight"
                                )
                            )
                        }) { Text("Turn into personal rule") }
                    }
                }
            }
        }
        draft?.let { d ->
            Column(Modifier.fillMaxWidth().background(Parchment, RoundedCornerShape(10.dp)).padding(10.dp)) {
                Text("Draft rule", color = Olive, style = MaterialTheme.typography.labelSmall)
                Text(d.label, style = MaterialTheme.typography.titleSmall)
                Button(onClick = { onConfirm(d) }, modifier = Modifier.fillMaxWidth()) { Text("Confirm rule") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    }
}
