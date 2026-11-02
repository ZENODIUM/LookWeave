package com.vizeno.loomie.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.data.LocalProfileStore
import com.vizeno.loomie.integration.LookWeaveGateway
import com.vizeno.loomie.integration.ProductCard
import com.vizeno.loomie.ui.theme.Clay
import com.vizeno.loomie.ui.theme.Hairline
import com.vizeno.loomie.ui.theme.Ink
import com.vizeno.loomie.ui.theme.Olive
import com.vizeno.loomie.ui.theme.OliveLight
import com.vizeno.loomie.ui.theme.Paper
import com.vizeno.loomie.ui.theme.Parchment
import com.vizeno.loomie.ui.theme.SoftInk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

private enum class StyleTab(val label: String) {
    FACE("Face shape"),
    COLOUR("Skin & colours"),
    PROPORTIONS("Proportions"),
    ACCESSORIES("Accessories")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StyleStudioScreen(onBack: () -> Unit, embedded: Boolean = false) {
    BackHandler(enabled = !embedded, onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf(JSONObject()) }
    var choices by remember { mutableStateOf(JSONObject()) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }
    // Face tab image store — separate from Fit bodyUri.
    var faceUri by rememberSaveable { mutableStateOf(LocalProfileStore.faceUri(context)) }
    var facePreview by remember { mutableStateOf<Bitmap?>(null) }
    var mannequinUri by rememberSaveable { mutableStateOf<String?>(null) }
    var mannequinPreview by remember { mutableStateOf<Bitmap?>(null) }
    var consent by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf("both") }
    var jobId by rememberSaveable { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf("Preparing your portrait") }
    var deleteDialog by remember { mutableStateOf(false) }
    var accessoryProducts by remember { mutableStateOf<List<ProductCard>>(emptyList()) }
    var accessoriesLoading by remember { mutableStateOf(false) }
    var accessoriesError by remember { mutableStateOf<String?>(null) }
    var accessorySubcategory by rememberSaveable { mutableStateOf("glasses") }
    var closeupKind by rememberSaveable { mutableStateOf("earring") }
    var closeupTargetUri by rememberSaveable { mutableStateOf<String?>(null) }
    var closeupProductUri by rememberSaveable { mutableStateOf<String?>(null) }
    var closeupPreview by remember { mutableStateOf<Bitmap?>(null) }
    var closeupBusy by rememberSaveable { mutableStateOf(false) }
    var closeupError by rememberSaveable { mutableStateOf<String?>(null) }
    var tab by rememberSaveable { mutableStateOf(StyleTab.FACE.name) }
    val activeTab = StyleTab.entries.firstOrNull { it.name == tab } ?: StyleTab.FACE
    val accessorySubcategories = listOf(
        "glasses" to "Glasses",
        "watches" to "Watches",
        "jewellery" to "Jewellery",
        "scarf" to "Scarf",
        "hat" to "Hat",
        "shoes" to "Shoes"
    )

    fun accept(value: JSONObject) {
        profile = value
        choices = value.optJSONObject("choices") ?: JSONObject()
    }
    fun change(key: String, value: Any) {
        choices = JSONObject(choices.toString()).put(key, value)
        notice = null
    }
    fun loadAccessories(source: JSONObject = choices, subcategory: String = accessorySubcategory) {
        accessoriesLoading = true
        accessoriesError = null
        scope.launch {
            runCatching { LookWeaveGateway.discoverAccessories(source, subcategory) }
                .onSuccess { discovery ->
                    accessoryProducts = discovery.products
                    if (discovery.products.isEmpty()) {
                        accessoriesError = "No live ${subcategory} matches were found. Try another subcategory or save preferences again."
                    }
                }
                .onFailure { accessoriesError = it.message ?: "Accessory search is unavailable right now." }
            accessoriesLoading = false
        }
    }

    LaunchedEffect(faceUri) {
        facePreview = withContext(Dispatchers.IO) {
            faceUri?.let { value ->
                runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(Uri.parse(value))?.use {
                        BitmapFactory.decodeStream(it, null, bounds)
                    }
                    val options = BitmapFactory.Options().apply {
                        inSampleSize = maxOf(1, maxOf(bounds.outWidth, bounds.outHeight) / 512)
                    }
                    context.contentResolver.openInputStream(Uri.parse(value))?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }.getOrNull()
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val saved = withContext(Dispatchers.IO) {
                LocalProfileStore.copyFaceToAppFiles(context, uri)
                    ?: LocalProfileStore.copyPortraitToAppFiles(context, uri)
            }
            faceUri = saved ?: uri.toString().also { LocalProfileStore.saveFaceUri(context, it) }
            mannequinUri = null
            mannequinPreview = null
            consent = false
            error = null
        }
    }
    val closeupTargetPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { closeupTargetUri = uri.toString(); closeupPreview = null; closeupError = null }
    }
    val closeupProductPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { closeupProductUri = uri.toString(); closeupPreview = null; closeupError = null }
    }

    fun makeMannequin() {
        val source = facePreview ?: return
        scope.launch {
            val result = withContext(Dispatchers.Default) { createLocalMannequin(source) }
            if (result == null) error = "I could not find a clear face. Try a front-facing photo with even light."
            else {
                val file = File(context.cacheDir, "lookweave-mannequin.png")
                FileOutputStream(file).use { result.compress(Bitmap.CompressFormat.PNG, 100, it) }
                mannequinPreview = result
                mannequinUri = Uri.fromFile(file).toString()
                notice = "Faceless version created locally. Face photo stays on this tab; Fit uses its own body photo."
                consent = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (faceUri == null) faceUri = LocalProfileStore.faceUri(context)
        runCatching { LookWeaveGateway.styleProfile() }.onSuccess {
            accept(it)
            val job = it.optJSONObject("activeJob")
            if (job?.optString("status") == "running") jobId = job.optString("id")
            if (it.optJSONObject("recommendations") != null) loadAccessories(choices)
        }.onFailure { error = it.message }
        loading = false
    }
    LaunchedEffect(jobId) {
        val id = jobId ?: return@LaunchedEffect
        busy = true
        try {
            var finished = false
            repeat(100) {
                if (!finished) {
                    val job = LookWeaveGateway.styleJob(id)
                    progress = job.optString("message", "Analysing your portrait")
                    if (job.optString("status") != "running") {
                        accept(LookWeaveGateway.styleProfile())
                        if (job.optString("status") == "complete") {
                            notice = "Analysis ready. Review tabs below — results stay saved."
                            loadAccessories(choices)
                        } else error = progress
                        finished = true
                    } else delay(5_000)
                }
            }
            if (!finished) error = "Analysis is still running. Reopen this page to check it."
        } catch (_: Exception) {
            error = "Could not check progress. Reopen this page to resume."
        } finally {
            busy = false
            jobId = null
        }
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Delete style evidence?") },
            text = { Text("Remove saved colour samples, face proportions and accessory preferences from LookWeave.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDialog = false
                    scope.launch {
                        busy = true
                        runCatching { LookWeaveGateway.deleteStyle() }
                            .onSuccess {
                                accept(it)
                                notice = "Style evidence deleted"
                                faceUri = null
                                LocalProfileStore.saveFaceUri(context, null)
                                consent = false
                                accessoryProducts = emptyList()
                            }
                            .onFailure { error = it.message }
                        busy = false
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text("Keep") } }
        )
    }

    val stageBitmap = mannequinPreview ?: facePreview
    val colours = profile.optJSONObject("colour")?.optJSONObject("colours")
    val face = profile.optJSONObject("face")
    val recommendations = profile.optJSONObject("recommendations")

    Column(
        Modifier
            .testTag("style-studio-list")
            .fillMaxSize()
            .background(Parchment)
            .then(if (embedded) Modifier else Modifier.statusBarsPadding())
            .navigationBarsPadding()
    ) {
        if (!embedded) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("Back") }
                Spacer(Modifier.weight(1f))
                Text("STYLE PROFILE", style = MaterialTheme.typography.labelMedium, color = Olive)
            }
        } else {
            Text(
                "STYLE PROFILE",
                style = MaterialTheme.typography.labelMedium,
                color = Olive,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Center portrait stage — no whole-page scroll
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .background(Paper, RoundedCornerShape(18.dp))
                .border(1.dp, Hairline, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            when {
                stageBitmap != null -> Image(
                    stageBitmap.asImageBitmap(),
                    contentDescription = "Style portrait",
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Upload a face portrait", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Used only for Face / style analysis — Fit keeps a separate full-body photo.",
                        color = SoftInk,
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !busy) {
                        Text("Choose face photo")
                    }
                }
            }
            if (loading || busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(12.dp))
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !busy, modifier = Modifier.weight(1f)) {
                Text(if (faceUri == null) "Upload face" else "Change face")
            }
            if (faceUri == null) {
                OutlinedButton(
                    onClick = {
                        if (LocalProfileStore.seedSamplePerson1IfNeeded(context, force = true)) {
                            faceUri = LocalProfileStore.faceUri(context)
                        }
                    },
                    enabled = !busy,
                    modifier = Modifier.weight(1f)
                ) { Text("Sample Noah") }
            }
            if (facePreview != null) {
                OutlinedButton(onClick = { makeMannequin() }, enabled = !busy, modifier = Modifier.weight(1f)) {
                    Text(if (mannequinPreview == null) "Faceless" else "Redo faceless")
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StyleTab.entries.forEach { entry ->
                FilterChip(selected = activeTab == entry, onClick = { tab = entry.name }, label = { Text(entry.label) })
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(Paper, RoundedCornerShape(14.dp))
                .border(1.dp, Hairline, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                notice?.let { Text(it, color = Olive, style = MaterialTheme.typography.bodySmall) }
                when (activeTab) {
                    StyleTab.FACE -> {
                        val detectedShape = face?.optString("faceShape")?.takeIf { it.isNotBlank() && !it.equals("unknown", true) && it != "null" }
                        val storedShape = LocalProfileStore.styleChoices(context)["faceShape"]
                        val activeShape = choices.optString("faceShape").takeIf {
                            it.isNotBlank() && it != "null" && !it.equals("Unknown", true)
                        } ?: storedShape?.takeIf { it.isNotBlank() } ?: detectedShape
                        Text("Face analysis", style = MaterialTheme.typography.titleSmall)
                        Text(
                            when {
                                detectedShape != null -> "Detected: $detectedShape"
                                !activeShape.isNullOrBlank() -> "Using your saved shape: $activeShape"
                                else -> "Not analyzed yet — run Apply / Analyse, or pick a shape below."
                            },
                            color = if (detectedShape != null || !activeShape.isNullOrBlank()) Olive else SoftInk,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Edit anytime — your choice syncs to Profile prefs and Discover.",
                            color = SoftInk,
                            style = MaterialTheme.typography.bodySmall
                        )
                        StudioOptions(
                            listOf("Oval", "Round", "Square", "Heart", "Oblong", "Diamond", "Triangle", "InvTriangle"),
                            activeShape ?: "",
                            !busy
                        ) {
                            change("faceShape", it)
                            LocalProfileStore.saveStyleChoice(context, "faceShape", it)
                            notice = "Face shape set to $it"
                        }
                        Text("Colour direction", style = MaterialTheme.typography.titleSmall)
                        StudioOptions(
                            listOf("Explore", "Warm", "Cool", "Mixed"),
                            choices.optString("colourDirection").ifBlank {
                                LocalProfileStore.styleChoices(context)["colourDirection"] ?: "Explore"
                            },
                            !busy
                        ) {
                            change("colourDirection", it)
                            LocalProfileStore.saveStyleChoice(context, "colourDirection", it)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = consent, enabled = !busy, onCheckedChange = { consent = it })
                            Text(
                                "Send selected ${if (mannequinUri != null) "faceless version" else "photo"} to YouCam.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    busy = true
                                    error = null
                                    runCatching {
                                        LookWeaveGateway.analyseStyle(
                                            context.contentResolver,
                                            Uri.parse(mannequinUri ?: faceUri),
                                            mode
                                        )
                                    }.onSuccess { jobId = it.getString("id") }
                                        .onFailure {
                                            error = it.message
                                            busy = false
                                        }
                                }
                            },
                            enabled = (mannequinUri != null || faceUri != null) && consent && !busy && !loading,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) progress else "Apply / Analyse") }
                    }
                    StyleTab.COLOUR -> {
                        Text("Detected colour values", style = MaterialTheme.typography.titleSmall)
                        if (colours != null) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("skin_color" to "Skin", "eye_color" to "Eyes", "hair_color" to "Hair", "lip_color" to "Lips").forEach { (key, label) ->
                                    if (colours.has(key)) {
                                        val hex = colours.optString(key)
                                        Column {
                                            StudioSwatch(label, hex)
                                            Text(hex, color = SoftInk, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("No colour analysis yet — run Apply / Analyse on Face.", color = SoftInk)
                        }
                        OutlinedTextField(
                            value = choices.optString("skinColour").takeUnless { it == "null" }.orEmpty(),
                            onValueChange = {
                                change("skinColour", it.take(7))
                                LocalProfileStore.saveStyleChoice(context, "skinColour", it.take(7))
                            },
                            label = { Text("Skin swatch override (#RRGGBB)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !busy
                        )
                        Text("Jewellery finish", style = MaterialTheme.typography.titleSmall)
                        StudioOptions(
                            listOf("Explore", "Gold", "Silver", "Mixed"),
                            choices.optString("metal").ifBlank {
                                LocalProfileStore.styleChoices(context)["metal"] ?: "Explore"
                            },
                            !busy
                        ) {
                            change("metal", it)
                            LocalProfileStore.saveStyleChoice(context, "metal", it)
                        }
                        recommendations?.optJSONArray("palette")?.let { palette ->
                            Text("Suggested palette", style = MaterialTheme.typography.titleSmall)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                repeat(palette.length()) {
                                    val item = palette.getJSONObject(it)
                                    StudioSwatch(item.getString("name"), item.getString("colour"))
                                }
                            }
                        }
                    }
                    StyleTab.PROPORTIONS -> {
                        if (face != null) {
                            Text("Face proportions", style = MaterialTheme.typography.titleSmall)
                            val ratios = face.optJSONObject("ratios") ?: JSONObject()
                            listOf(
                                "face_aspect_ratio" to "Width : height",
                                "horizontal_third" to "Vertical thirds",
                                "vertical_fifth" to "Horizontal fifths"
                            ).forEach { (key, label) ->
                                ratios.optJSONArray(key)?.let { values ->
                                    Text("$label   ${(0 until values.length()).joinToString(" : ") { "%.2f".format(values.optDouble(it)) }}")
                                }
                            }
                            Text("Descriptive proportions only — not a beauty score.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("Run analysis to see proportions from your photo.", color = SoftInk)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = consent, enabled = !busy, onCheckedChange = { consent = it })
                            Text(
                                "Send selected ${if (mannequinUri != null) "faceless version" else "photo"} to YouCam for analysis.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("both" to "Both", "colour" to "Colours", "face" to "Proportions").forEach { (id, label) ->
                                FilterChip(selected = mode == id, enabled = !busy, onClick = { mode = id }, label = { Text(label) })
                            }
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    busy = true
                                    error = null
                                    runCatching {
                                        LookWeaveGateway.analyseStyle(
                                            context.contentResolver,
                                            Uri.parse(mannequinUri ?: faceUri),
                                            mode
                                        )
                                    }.onSuccess { jobId = it.getString("id") }
                                        .onFailure {
                                            error = it.message
                                            busy = false
                                        }
                                }
                            },
                            enabled = (mannequinUri != null || faceUri != null) && consent && !busy && !loading,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (busy) progress else "Apply / Analyse") }
                    }
                    StyleTab.ACCESSORIES -> {
                        Text("Accessory subcategory", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            accessorySubcategories.forEach { (id, label) ->
                                FilterChip(
                                    selected = accessorySubcategory == id,
                                    enabled = !busy && !accessoriesLoading,
                                    onClick = {
                                        accessorySubcategory = id
                                        loadAccessories(choices, id)
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                        listOf(
                            "nickelAvoidance" to "Avoid nickel contact",
                            "lightweightFrames" to "Prefer lightweight glasses",
                            "hearingDevice" to "Space for a hearing device",
                            "lowBridge" to "Explore low-bridge-fit glasses",
                            "easyClasps" to "Prefer easy jewellery closures"
                        ).forEach { (key, label) ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = choices.optBoolean(key), enabled = !busy, onCheckedChange = { change(key, it) })
                            }
                        }
                        recommendations?.optJSONObject("eyewear")?.let { glasses ->
                            Text("Frame direction · ${glasses.optString("title")}", style = MaterialTheme.typography.titleSmall)
                            FrameSilhouette(glasses.optString("title"))
                            Text(glasses.optString("reason"), style = MaterialTheme.typography.bodySmall, color = SoftInk)
                        }
                        recommendations?.optJSONObject("jewellery")?.let { jewellery ->
                            Text("Finish · ${jewellery.optString("title")}", style = MaterialTheme.typography.titleSmall)
                            Text(jewellery.optString("reason"), style = MaterialTheme.typography.bodySmall, color = SoftInk)
                        }
                        if (accessoriesLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
                        accessoriesError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        Text(
                            "Similar ${accessorySubcategories.firstOrNull { it.first == accessorySubcategory }?.second?.lowercase() ?: "products"} · live discover",
                            style = MaterialTheme.typography.labelMedium,
                            color = Olive
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                            items(accessoryProducts, key = { it.id }) { product ->
                                AccessoryChip(product)
                            }
                        }
                        if (!accessoriesLoading && accessoryProducts.isEmpty() && accessoriesError == null) {
                            Text("Tap Find similar products for live retailer results.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                        }
                        OutlinedButton(
                            onClick = { loadAccessories() },
                            enabled = !accessoriesLoading && !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (accessoriesLoading) "Finding live matches…"
                                else "Find similar ${accessorySubcategories.firstOrNull { it.first == accessorySubcategory }?.second?.lowercase() ?: "products"}"
                            )
                        }
                        HorizontalDivider(color = Hairline, modifier = Modifier.padding(vertical = 10.dp))
                        Text("Accessory Detail Lab", style = MaterialTheme.typography.titleMedium)
                        Text("Create a close-up visual styling preview with YouCam. Use a wrist/hand photo for watches, rings and bracelets; use a clear selfie for earrings and necklaces.", style = MaterialTheme.typography.bodySmall, color = SoftInk)
                        StudioOptions(
                            options = listOf("earring", "necklace", "watch", "ring", "bracelet"),
                            selected = closeupKind,
                            enabled = !closeupBusy,
                            onSelect = { closeupKind = it; closeupPreview = null; closeupError = null }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { closeupTargetPicker.launch("image/*") }, enabled = !closeupBusy, modifier = Modifier.weight(1f)) {
                                Text(if (closeupTargetUri == null) if (closeupKind in listOf("watch", "ring", "bracelet")) "Add wrist / hand" else "Add selfie" else "Change close-up")
                            }
                            OutlinedButton(onClick = { closeupProductPicker.launch("image/*") }, enabled = !closeupBusy, modifier = Modifier.weight(1f)) {
                                Text(if (closeupProductUri == null) "Add accessory" else "Change accessory")
                            }
                        }
                        Button(
                            onClick = {
                                val target = closeupTargetUri ?: return@Button
                                val product = closeupProductUri ?: return@Button
                                closeupBusy = true; closeupError = null
                                scope.launch {
                                    runCatching {
                                        val task = LookWeaveGateway.createAccessoryPreview(context.contentResolver, Uri.parse(target), Uri.parse(product), closeupKind)
                                        val bytes = LookWeaveGateway.download(task.imageUrl)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    }.onSuccess { bitmap ->
                                        closeupPreview = bitmap ?: run { closeupError = "The preview image could not be decoded."; null }
                                    }.onFailure { closeupError = it.message ?: "Could not create this accessory preview." }
                                    closeupBusy = false
                                }
                            },
                            enabled = closeupTargetUri != null && closeupProductUri != null && !closeupBusy,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(if (closeupBusy) "Creating close-up…" else "Preview $closeupKind") }
                        closeupError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        closeupPreview?.let { preview ->
                            Image(preview.asImageBitmap(), "Visual styling preview for selected $closeupKind", Modifier.fillMaxWidth().height(260.dp).background(OliveLight, RoundedCornerShape(14.dp)), contentScale = ContentScale.Fit)
                            Text("Visual styling preview — not a measurement, comfort, or fit guarantee.", color = SoftInk, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Button(
                    onClick = {
                        scope.launch {
                            val swatch = choices.optString("skinColour").takeUnless { it == "null" }.orEmpty()
                            if (swatch.isNotEmpty() && !Regex("#[0-9a-fA-F]{6}").matches(swatch)) {
                                error = "Enter a six-digit hex colour, for example #A87960, or leave it blank."
                                return@launch
                            }
                            busy = true
                            error = null
                            runCatching { LookWeaveGateway.saveStyle(choices) }
                                .onSuccess {
                                    accept(it)
                                    notice = "Style preferences saved. Finding real accessory matches…"
                                    tab = StyleTab.ACCESSORIES.name
                                    loadAccessories(choices)
                                }
                                .onFailure { error = it.message }
                            busy = false
                        }
                    },
                    enabled = !busy && !loading,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save style preferences") }
                TextButton(onClick = { deleteDialog = true }, enabled = !busy && !loading) {
                    Text("Delete style evidence", color = Clay)
                }
            }
        }
    }
}

@Composable
private fun AccessoryChip(product: ProductCard) {
    val context = LocalContext.current
    Column(
        Modifier
            .width(140.dp)
            .background(OliveLight, RoundedCornerShape(12.dp))
            .clickable(enabled = product.productUrl.startsWith("http")) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(product.productUrl)))
            }
            .padding(8.dp)
    ) {
        Text(product.name, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
        Text(
            listOfNotNull(product.retailer, product.price).joinToString(" · "),
            color = Olive,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            product.compatibility.ifBlank { product.reason },
            color = SoftInk,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FrameSilhouette(title: String) {
    val curved = title.contains("oval", true) || title.contains("round", true) || title.contains("Panto", true)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(OliveLight, RoundedCornerShape(12.dp))
            .semantics { contentDescription = "Illustrative frame silhouette" }
    ) {
        val lensWidth = size.width * .29f
        val lensHeight = size.height * .52f
        val top = size.height * .23f
        val left = size.width * .14f
        val right = size.width * .57f
        val outline = Stroke(3.dp.toPx())
        for (x in listOf(left, right)) {
            if (curved) drawOval(Ink, Offset(x, top), Size(lensWidth, lensHeight), style = outline)
            else drawRoundRect(Ink, Offset(x, top), Size(lensWidth, lensHeight), CornerRadius(9.dp.toPx()), style = outline)
        }
        drawLine(Ink, Offset(left + lensWidth, top + lensHeight * .3f), Offset(right, top + lensHeight * .3f), 3.dp.toPx())
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudioOptions(options: List<String>, selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { value ->
            FilterChip(selected = selected == value, enabled = enabled, onClick = { onSelect(value) }, label = { Text(value) })
        }
    }
}

@Composable
private fun StudioSwatch(name: String, hex: String) {
    val colour = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(OliveLight)
    Column(Modifier.width(72.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            Modifier
                .size(56.dp)
                .background(colour, RoundedCornerShape(topEnd = 18.dp, bottomStart = 18.dp))
                .semantics { contentDescription = "$name colour $hex" }
        )
        Text(name, style = MaterialTheme.typography.labelMedium)
        Text(hex, style = MaterialTheme.typography.labelSmall, color = SoftInk)
    }
}
