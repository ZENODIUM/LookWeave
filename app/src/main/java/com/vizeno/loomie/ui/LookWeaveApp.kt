@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.vizeno.loomie.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DryCleaning
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.vizeno.loomie.R
import com.vizeno.loomie.data.LocalProfileStore
import com.vizeno.loomie.data.LookWeaveRepository
import com.vizeno.loomie.data.RetailerDirectory
import com.vizeno.loomie.data.SamplePersonProfiles
import com.vizeno.loomie.data.SamplePersonaProducts
import com.vizeno.loomie.data.TryOnCompositeCache
import com.vizeno.loomie.data.TryOnPrebake
import com.vizeno.loomie.domain.ConstraintSolver
import com.vizeno.loomie.integration.AgentRequest
import com.vizeno.loomie.integration.AgentAction
import com.vizeno.loomie.integration.DayContext
import com.vizeno.loomie.integration.GarmentEvidencePayload
import com.vizeno.loomie.integration.LookWeaveGateway
import com.vizeno.loomie.integration.ToolCall
import com.vizeno.loomie.model.AgentMessage
import com.vizeno.loomie.model.AppDestination
import com.vizeno.loomie.model.CalendarEvent
import com.vizeno.loomie.model.ClothingPurchase
import com.vizeno.loomie.model.Evidence
import com.vizeno.loomie.model.EvidenceSource
import com.vizeno.loomie.model.FilterCatalog
import com.vizeno.loomie.model.FilterCategory
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.Garment
import com.vizeno.loomie.model.GarmentCategory
import com.vizeno.loomie.model.DailyLookSuggestion
import com.vizeno.loomie.model.OutfitPlan
import com.vizeno.loomie.model.NearbyPlace
import com.vizeno.loomie.model.PlannedOutfit
import com.vizeno.loomie.model.RecurrenceRule
import com.vizeno.loomie.model.Retailer
import com.vizeno.loomie.model.GenderOptions
import com.vizeno.loomie.model.SizeLog
import com.vizeno.loomie.model.UserFilter
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.model.WearabilityRule
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.vizeno.loomie.integration.AgentContextBuilder
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import com.vizeno.loomie.ui.theme.Brass
import com.vizeno.loomie.ui.theme.Clay
import com.vizeno.loomie.ui.theme.DarkPaper
import com.vizeno.loomie.ui.theme.Hairline
import com.vizeno.loomie.ui.theme.Ink
import com.vizeno.loomie.ui.theme.Olive
import com.vizeno.loomie.ui.theme.OliveLight
import com.vizeno.loomie.ui.theme.Paper
import com.vizeno.loomie.ui.theme.Parchment
import com.vizeno.loomie.ui.theme.SoftInk
import io.elevenlabs.ConversationClient
import io.elevenlabs.ConversationConfig
import io.elevenlabs.ConversationSession
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.annotations.MarkerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class FeatureScreen { OUTFIT, TRY_ON, DISCOVER, CONTEXT }

@Composable
fun LookWeaveApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("lookweave", 0) }
    var onboarded by rememberSaveable { mutableStateOf(prefs.getBoolean("onboarded", false)) }
    var showStyleStudio by rememberSaveable { mutableStateOf(false) }
    var destination by rememberSaveable { mutableStateOf(AppDestination.TODAY) }
    var feature by rememberSaveable { mutableStateOf<FeatureScreen?>(null) }
    var selectedGarment by remember { mutableStateOf<Garment?>(null) }
    var wearability by remember { mutableStateOf(WearabilityPreferences()) }
    var activePlan by remember { mutableStateOf(ConstraintSolver.plan("Gallery opening", wearability)) }
    var selectedOccasion by rememberSaveable { mutableStateOf("Gallery opening") }
    var activeDayContext by remember { mutableStateOf<DayContext?>(null) }
    var likedPlaceIds by remember { mutableStateOf(emptySet<String>()) }
    var passedPlaceIds by remember { mutableStateOf(emptySet<String>()) }
    var likedProductIds by remember { mutableStateOf(emptySet<String>()) }
    var passedProductIds by remember { mutableStateOf(emptySet<String>()) }
    var displayName by rememberSaveable { mutableStateOf(LocalProfileStore.displayName(context)) }
    var portraitUri by rememberSaveable { mutableStateOf(LocalProfileStore.portraitUri(context)) }
    var bodyUriState by rememberSaveable { mutableStateOf(LocalProfileStore.bodyUri(context)) }
    var profileEpoch by rememberSaveable { mutableIntStateOf(0) }
    var discoveryLiveEpoch by rememberSaveable { mutableIntStateOf(0) }
    var loomieSeed by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun refreshIdentityFromStore() {
        displayName = LocalProfileStore.displayName(context)
        portraitUri = LocalProfileStore.portraitUri(context)
        bodyUriState = LocalProfileStore.bodyUri(context)
        profileEpoch += 1
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            SamplePersonProfiles.seedDefaultIfNeeded(context)
        }
        refreshIdentityFromStore()
        val seeded = LocalProfileStore.wearabilitySeed(context)
        runCatching { LookWeaveGateway.sharedState() }.onSuccess { synced ->
            wearability = (seeded ?: synced.preferences).copy(
                customRules = synced.preferences.customRules.ifEmpty { seeded?.customRules.orEmpty() },
                userFilters = synced.preferences.userFilters.ifEmpty { seeded?.userFilters.orEmpty() }
            )
            selectedOccasion = synced.selectedOccasion
            activePlan = ConstraintSolver.plan(
                selectedOccasion,
                wearability,
                SamplePersonProfiles.wardrobeForActiveProfile(context),
                LocalProfileStore.genderLabel(context)
            )
            likedPlaceIds = synced.likedPlaceIds
            passedPlaceIds = synced.passedPlaceIds
            val localLiked = LocalProfileStore.likedProductIds(context)
            val localPassed = LocalProfileStore.passedProductIds(context)
            likedProductIds = if (localLiked.isNotEmpty() || localPassed.isNotEmpty()) localLiked else synced.likedProductIds
            passedProductIds = if (localLiked.isNotEmpty() || localPassed.isNotEmpty()) localPassed else synced.passedProductIds
        }.onFailure {
            if (seeded != null) {
                wearability = seeded
                activePlan = ConstraintSolver.plan(
                    selectedOccasion,
                    wearability,
                    SamplePersonProfiles.wardrobeForActiveProfile(context),
                    LocalProfileStore.genderLabel(context)
                )
            }
            likedProductIds = LocalProfileStore.likedProductIds(context)
            passedProductIds = LocalProfileStore.passedProductIds(context)
        }
        // Prebake try-on composites for active person (assets/disk first; live only on miss).
        scope.launch(Dispatchers.IO) {
            runCatching { TryOnPrebake.warmActivePerson(context, liveGenerate = true) }
        }
    }

    fun updateWearability(value: WearabilityPreferences) {
        wearability = value
        LocalProfileStore.saveWearabilitySeed(context, value)
        scope.launch { runCatching { LookWeaveGateway.saveSharedState(value, selectedOccasion) } }
    }

    if (showStyleStudio) {
        StyleStudioScreen(onBack = {
            showStyleStudio = false
            portraitUri = LocalProfileStore.portraitUri(context)
            displayName = LocalProfileStore.displayName(context)
        })
        return
    }
    if (!onboarded) {
        WelcomeScreen(onStyle = { showStyleStudio = true }) {
            prefs.edit().putBoolean("onboarded", true).apply()
            onboarded = true
        }
        return
    }

    Scaffold(
        containerColor = Parchment,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (feature == null && destination != AppDestination.PROFILE) {
                LookWeaveNavigation(destination) { destination = it }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            Canvas(Modifier.fillMaxSize()) {
                val spacing = 44.dp.toPx()
                var y = spacing / 2f
                while (y < size.height) {
                    var x = spacing / 2f
                    while (x < size.width) {
                        drawCircle(Hairline.copy(alpha = .24f), .7.dp.toPx(), Offset(x, y))
                        x += spacing
                    }
                    y += spacing
                }
            }
            AnimatedContent(targetState = feature, label = "feature", modifier = Modifier.fillMaxSize()) { screen ->
                when (screen) {
                    FeatureScreen.OUTFIT -> OutfitResultScreen(activePlan, { feature = null }, { feature = FeatureScreen.TRY_ON })
                    FeatureScreen.TRY_ON -> TryOnScreen(activePlan.replacement ?: activePlan.resolved.firstOrNull(), { feature = FeatureScreen.OUTFIT }) {
                        val pieces = activePlan.resolved
                        LocalProfileStore.saveAppliedLook(
                            context,
                            LocalProfileStore.AppliedLook(
                                id = "applied-${System.currentTimeMillis()}",
                                title = activePlan.occasion.ifBlank { "Saved look" },
                                garmentIds = pieces.map { it.id },
                                occasion = activePlan.occasion,
                                savedAtEpochDay = LocalDate.now().toEpochDay()
                            )
                        )
                        profileEpoch += 1
                        scope.launch { snackbar.showSnackbar("Look saved — it will appear on Home") }
                    }
                    FeatureScreen.DISCOVER -> StoreDirectoryScreen(
                        preferences = wearability,
                        likedPlaceIds = likedPlaceIds,
                        passedPlaceIds = passedPlaceIds,
                        likedProductIds = likedProductIds,
                        passedProductIds = passedProductIds,
                        onDecision = { placeId, liked ->
                            if (liked) {
                                likedPlaceIds = likedPlaceIds + placeId
                                passedPlaceIds = passedPlaceIds - placeId
                            } else {
                                passedPlaceIds = passedPlaceIds + placeId
                                likedPlaceIds = likedPlaceIds - placeId
                            }
                            scope.launch { runCatching { LookWeaveGateway.saveDiscoveryState(likedPlaceIds, passedPlaceIds, likedProductIds, passedProductIds) } }
                        },
                        onProductDecision = { productId, liked ->
                            if (liked) { likedProductIds = likedProductIds + productId; passedProductIds = passedProductIds - productId }
                            else { passedProductIds = passedProductIds + productId; likedProductIds = likedProductIds - productId }
                            LocalProfileStore.saveProductDecision(context, productId, liked)
                            scope.launch { runCatching { LookWeaveGateway.saveDiscoveryState(likedPlaceIds, passedPlaceIds, likedProductIds, passedProductIds) } }
                        },
                        onResetDecisions = {
                            likedPlaceIds = emptySet()
                            passedPlaceIds = emptySet()
                            likedProductIds = emptySet(); passedProductIds = emptySet()
                            LocalProfileStore.clearProductDecisions(context)
                            scope.launch { runCatching { LookWeaveGateway.saveDiscoveryState(emptySet(), emptySet(), emptySet(), emptySet()) } }
                        },
                        onBack = { feature = null },
                        profileEpoch = profileEpoch,
                        liveSearchRequest = discoveryLiveEpoch
                    )
                    FeatureScreen.CONTEXT -> DaySignalsScreen(onBack = { feature = null }) { contextResult ->
                        activeDayContext = contextResult
                        feature = null
                        scope.launch { snackbar.showSnackbar("Day Signals added to today’s agent context") }
                    }
                    null -> AnimatedContent(targetState = destination, label = "destination") { target ->
                        when (target) {
                            AppDestination.TODAY -> TodayScreen(
                                plan = activePlan,
                                preferences = wearability,
                                dayContext = activeDayContext,
                                displayName = displayName,
                                portraitUri = portraitUri,
                                bodyUri = bodyUriState,
                                profileEpoch = profileEpoch,
                                onProfile = { destination = AppDestination.PROFILE },
                                onOpenLook = { look ->
                                    activePlan = look.plan
                                    selectedOccasion = look.plan.occasion
                                    feature = FeatureScreen.OUTFIT
                                },
                                onOpenPlan = {
                                    activePlan = ConstraintSolver.plan(
                                        selectedOccasion.ifBlank { "Gallery opening" },
                                        wearability,
                                        SamplePersonProfiles.wardrobeForActiveProfile(context),
                                        LocalProfileStore.genderLabel(context)
                                    )
                                    feature = FeatureScreen.OUTFIT
                                },
                                onLab = { destination = AppDestination.LAB },
                                onDay = { destination = AppDestination.DAY },
                                onSeedSamplePerson = {
                                    val wrote = SamplePersonProfiles.activate(context, "person1")
                                    refreshIdentityFromStore()
                                    scope.launch {
                                        snackbar.showSnackbar(
                                            if (wrote) "Sample Noah loaded for Fit & Face"
                                            else "Could not load sample Noah images"
                                        )
                                        if (wrote) {
                                            withContext(Dispatchers.IO) {
                                                runCatching { TryOnPrebake.warmActivePerson(context) }
                                            }
                                        }
                                    }
                                }
                            )
                            AppDestination.WARDROBE -> WardrobeScreen(
                                onSelect = { selectedGarment = it },
                                onImported = { scope.launch { runCatching { LookWeaveGateway.saveSharedState(wearability, selectedOccasion) } } },
                                onDiscover = { feature = FeatureScreen.DISCOVER }
                            )
                            AppDestination.PLAN -> PlanScreen(wearability, selectedOccasion, { updateWearability(it) }) { occasion ->
                                selectedOccasion = occasion
                                activePlan = ConstraintSolver.plan(
                                    occasion,
                                    wearability,
                                    SamplePersonProfiles.wardrobeForActiveProfile(context),
                                    LocalProfileStore.genderLabel(context),
                                    preferProfileCapsule = true
                                )
                                scope.launch { runCatching { LookWeaveGateway.saveSharedState(wearability, occasion) } }
                                feature = FeatureScreen.OUTFIT
                            }
                            AppDestination.LAB -> {
                                var labMode by rememberSaveable { mutableStateOf("fit") }
                                Column(Modifier.fillMaxSize()) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .statusBarsPadding()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                            .background(OliveLight, RoundedCornerShape(12.dp))
                                            .padding(4.dp)
                                    ) {
                                        listOf("fit" to "Fit", "face" to "Face / style analysis").forEach { (value, label) ->
                                            Box(
                                                Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(if (labMode == value) Paper else Color.Transparent)
                                                    .clickable { labMode = value }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    label,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (labMode == value) Ink else SoftInk,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    Box(Modifier.weight(1f).fillMaxWidth()) {
                                        key(profileEpoch) {
                                            if (labMode == "fit") {
                                                FitFeelStudioScreen(
                                                    preferences = wearability,
                                                    plan = activePlan,
                                                    onPreferences = { updateWearability(it) },
                                                    onPlan = { activePlan = it },
                                                    onSaved = { message ->
                                                        prefs.edit().putBoolean("lab_complete", true).apply()
                                                        scope.launch { snackbar.showSnackbar(message) }
                                                    }
                                                )
                                            } else {
                                                StyleStudioScreen(
                                                    onBack = { labMode = "fit" },
                                                    embedded = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            AppDestination.DAY -> DayHubScreen(
                                onAskLoomie = { seed ->
                                    loomieSeed = seed
                                    destination = AppDestination.AGENT
                                },
                                onUseDayContext = { contextResult ->
                                    activeDayContext = contextResult
                                    scope.launch { snackbar.showSnackbar("Day Signals added to today’s agent context") }
                                }
                            )
                            AppDestination.AGENT -> AgentScreen(
                                activeDayContext,
                                wearability,
                                { updateWearability(it) },
                                { activePlan = it },
                                seedMessage = loomieSeed,
                                onSeedConsumed = { loomieSeed = null },
                                likedProductIds = likedProductIds,
                                likedPlaceIds = likedPlaceIds,
                                onNavigate = { destination = it },
                                onOpenDiscovery = { discoveryLiveEpoch += 1; feature = FeatureScreen.DISCOVER },
                                onOpenWovenLook = { feature = FeatureScreen.OUTFIT }
                            )
                            AppDestination.PROFILE -> ProfileScreen(
                                onStyle = { showStyleStudio = true },
                                preferences = wearability,
                                onPreferences = { updateWearability(it) },
                                displayName = displayName,
                                portraitUri = portraitUri,
                                profileEpoch = profileEpoch,
                                onDisplayName = {
                                    displayName = it
                                    LocalProfileStore.saveDisplayName(context, it)
                                    val active = SamplePersonProfiles.activeId(context)
                                    LocalProfileStore.saveDisplayNameForProfile(context, active, it)
                                    profileEpoch++
                                },
                                onPortraitUri = {
                                    portraitUri = it
                                    LocalProfileStore.savePortraitUri(context, it)
                                },
                                onProfileActivated = {
                                    refreshIdentityFromStore()
                                    LocalProfileStore.wearabilitySeed(context)?.let { seeded ->
                                        wearability = seeded
                                        updateWearability(seeded)
                                        activePlan = ConstraintSolver.plan(
                                            selectedOccasion,
                                            seeded,
                                            SamplePersonProfiles.wardrobeForActiveProfile(context),
                                            LocalProfileStore.genderLabel(context)
                                        )
                                    }
                                    likedProductIds = LocalProfileStore.likedProductIds(context)
                                    passedProductIds = LocalProfileStore.passedProductIds(context)
                                    scope.launch(Dispatchers.IO) {
                                        runCatching { TryOnPrebake.warmActivePerson(context) }
                                    }
                                    scope.launch {
                                        val profile = SamplePersonProfiles.activeProfile(context)
                                        val meta = LocalProfileStore.personaMeta(context)
                                        snackbar.showSnackbar(
                                            buildString {
                                                append("Switched to ${profile.displayName}")
                                                meta.age?.let { append(" · $it") }
                                                meta.occupation.takeIf { it.isNotBlank() }?.let { append(" $it") }
                                                append(" · ${profile.gender} · ZIP ${profile.postalCode}")
                                            }
                                        )
                                    }
                                },
                                onBack = { destination = AppDestination.TODAY }
                            ) {
                                scope.launch {
                                    runCatching { LookWeaveGateway.resetProfile() }.onSuccess {
                                        wearability = it.preferences
                                        likedPlaceIds = emptySet(); passedPlaceIds = emptySet(); likedProductIds = emptySet(); passedProductIds = emptySet()
                                        LocalProfileStore.clearLocalIdentity(context)
                                        displayName = ""
                                        portraitUri = null
                                        bodyUriState = null
                                        profileEpoch += 1
                                        prefs.edit().clear().apply()
                                        context.deleteFile("saved-look.png")
                                        onboarded = false
                                    }.onFailure { snackbar.showSnackbar("Profile could not be cleared. Please retry when connected.") }
                                }
                                }
                        }
                    }
                }
            }
        }
    }

    selectedGarment?.let { garment ->
        GarmentSheet(garment, wearability) { selectedGarment = null }
    }
}

@Composable
private fun WelcomeScreen(onStyle: () -> Unit, onContinue: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Ink)) {
        Image(
            painter = painterResource(R.drawable.lookweave_editorial),
            contentDescription = "Woman wearing an olive adaptive jacket and charcoal trousers",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .28f)))
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Wordmark(light = true)
            GarmentSculpture(Modifier.align(Alignment.End))
            Column {
                Text("Style that works\nwith you.", style = MaterialTheme.typography.displayLarge, color = Color.White)
                Spacer(Modifier.height(14.dp))
                Text(
                    "A personal fashion agent that learns what feels right—not just what looks right.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = .9f),
                    modifier = Modifier.widthIn(max = 330.dp)
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Ink)
                ) {
                    Text("Enter LookWeave")
                    Spacer(Modifier.width(10.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onStyle, modifier = Modifier.fillMaxWidth()) { Text("Explore my colours & accessories", color = Color.White) }
                Text("Built around what feels right for you.", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun GarmentSculpture(modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "garment sculpture")
    val drift by motion.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200), RepeatMode.Reverse),
        label = "garment sculpture drift"
    )
    Box(
        modifier.size(206.dp, 178.dp).semantics {
            contentDescription = "Layered garment forms representing adaptable style"
        }
    ) {
        Box(
            Modifier.size(116.dp, 142.dp).align(Alignment.CenterEnd)
                .graphicsLayer { rotationZ = -11f + drift; rotationY = 16f; cameraDistance = 18f }
                .background(Color(0xFFB8C7BE).copy(alpha = .88f), RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = .45f), RoundedCornerShape(18.dp))
        )
        Box(
            Modifier.size(106.dp, 136.dp).align(Alignment.Center)
                .graphicsLayer { rotationZ = 7f - drift * .7f; rotationY = -12f; cameraDistance = 18f }
                .background(Olive.copy(alpha = .9f), RoundedCornerShape(16.dp))
        )
        Box(
            Modifier.size(82.dp, 124.dp).align(Alignment.Center)
                .graphicsLayer { translationX = drift * 3f; rotationZ = -2f }
                .background(Color.White.copy(alpha = .92f), RoundedCornerShape(14.dp))
        ) {
            Canvas(Modifier.fillMaxSize().padding(16.dp)) {
                drawLine(Ink.copy(alpha = .22f), Offset(size.width * .5f, 0f), Offset(size.width * .5f, size.height), 1.dp.toPx())
                drawCircle(Olive, 4.dp.toPx(), Offset(size.width * .5f, size.height * .3f))
                drawCircle(Olive, 4.dp.toPx(), Offset(size.width * .5f, size.height * .48f))
                drawCircle(Olive, 4.dp.toPx(), Offset(size.width * .5f, size.height * .66f))
            }
        }
        Text(
            "FORM / FUNCTION",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomStart).background(Ink.copy(alpha = .72f)).padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun LookWeaveNavigation(selected: AppDestination, onSelect: (AppDestination) -> Unit) {
    val items = listOf(
        AppDestination.TODAY to Icons.Rounded.Home,
        AppDestination.WARDROBE to Icons.Rounded.Checkroom,
        AppDestination.PLAN to Icons.AutoMirrored.Rounded.EventNote,
        AppDestination.LAB to Icons.Rounded.Science,
        AppDestination.DAY to Icons.Rounded.Today,
        AppDestination.AGENT to Icons.Rounded.ChatBubbleOutline
    )
    NavigationBar(containerColor = Paper, tonalElevation = 0.dp, modifier = Modifier.border(1.dp, Hairline)) {
        items.forEach { (item, icon) ->
            NavigationBarItem(
                selected = selected == item,
                onClick = { onSelect(item) },
                icon = { Icon(icon, contentDescription = item.label) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = OliveLight, selectedIconColor = DarkPaper, selectedTextColor = DarkPaper, unselectedIconColor = SoftInk, unselectedTextColor = SoftInk)
            )
        }
    }
}

@Composable
private fun PageHeader(title: String, eyebrow: String? = null, icon: ImageVector? = null, action: @Composable (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(Modifier.size(42.dp).background(OliveLight, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Olive, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
            } else {
                Box(Modifier.width(3.dp).height(42.dp).background(Brass, RoundedCornerShape(3.dp)))
                Spacer(Modifier.width(12.dp))
            }
            Column {
                eyebrow?.let { Text(it.uppercase(Locale.US), style = MaterialTheme.typography.labelSmall, color = SoftInk) }
                Text(title, style = MaterialTheme.typography.headlineMedium, color = Ink)
            }
        }
        action?.invoke()
    }
}

@Composable
private fun Wordmark(light: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.loomie_mark),
            contentDescription = "Loomie",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (light) Color.White.copy(alpha = .18f) else OliveLight)
                .border(1.dp, if (light) Color.White.copy(alpha = .35f) else Olive.copy(alpha = .25f), CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(10.dp))
        Text("LookWeave", color = if (light) Color.White else Ink, fontWeight = FontWeight.Bold, letterSpacing = (-.2).sp, fontSize = 18.sp)
    }
}

@Composable
private fun TodayScreen(
    plan: OutfitPlan,
    preferences: WearabilityPreferences,
    dayContext: DayContext?,
    displayName: String,
    portraitUri: String?,
    bodyUri: String?,
    profileEpoch: Int,
    onProfile: () -> Unit,
    onOpenLook: (DailyLookSuggestion) -> Unit,
    onOpenPlan: () -> Unit,
    onLab: () -> Unit,
    onDay: () -> Unit,
    onSeedSamplePerson: () -> Unit
) {
    val context = LocalContext.current
    val resolvedBodyUri = remember(bodyUri, portraitUri, profileEpoch) {
        bodyUri?.takeIf { it.isNotBlank() }
            ?: LocalProfileStore.bodyUri(context)
            ?: LocalProfileStore.faceUri(context)
            ?: portraitUri
    }
    val todayEvents = remember {
        val day = LocalDate.now().toEpochDay()
        LocalProfileStore.loadEvents(context).filter { it.dateEpochDay == day }.map { it.title }
    }
    val dayNotes = remember(dayContext) {
        dayContext?.let { day ->
            buildList {
                day.signals.take(2).forEach { add("${it.label}: ${it.value}") }
                day.recommendations.filter { it.action == "apply" }.take(2).forEach { add(it.title) }
            }
        }.orEmpty()
    }
    val gender = remember(profileEpoch) { LocalProfileStore.genderLabel(context) }
    val wardrobe = remember(profileEpoch, gender) { SamplePersonProfiles.wardrobeForActiveProfile(context) }
    val appliedLooks = remember(profileEpoch) { LocalProfileStore.loadAppliedLooks(context) }
    val looks = remember(preferences, dayContext, todayEvents, profileEpoch, wardrobe, gender, appliedLooks) {
        val suggested = ConstraintSolver.suggestDailyLooks(
            preferences = preferences,
            wardrobe = wardrobe,
            occasion = "Today",
            dayNotes = dayNotes,
            eventTitles = todayEvents,
            gender = gender,
            sizes = LocalProfileStore.loadSizes(context),
            preferredGarmentIds = SamplePersonProfiles.activeProfile(context).preferredGarmentIds,
            count = 5
        )
        val saved = appliedLooks.mapIndexed { index, applied ->
            val pieces = applied.garmentIds.mapNotNull { id -> wardrobe.firstOrNull { it.id == id } }
                .ifEmpty { applied.garmentIds.mapNotNull { id -> LookWeaveRepository.allGarments.firstOrNull { it.id == id } } }
            if (pieces.isEmpty()) null
            else DailyLookSuggestion(
                id = applied.id,
                title = "Saved · ${applied.title}",
                plan = OutfitPlan(
                    occasion = applied.occasion.ifBlank { "Today" },
                    original = pieces,
                    resolved = pieces,
                    replaced = null,
                    replacement = null,
                    explanation = "You saved this look after try-on.",
                    preservedCount = pieces.size,
                    score = 99
                ),
                reasons = listOf(
                    "Saved for ${LocalDate.ofEpochDay(applied.savedAtEpochDay)}",
                    "Pieces: " + pieces.joinToString(" · ") { it.name },
                    gender?.let { "Profile gender: $it" } ?: "Active profile capsule"
                ),
                score = 99 - index
            )
        }.filterNotNull()
        (saved + suggested.filterNot { look -> saved.any { it.plan.resolved.map { g -> g.id } == look.plan.resolved.map { g -> g.id } } })
            .take(6)
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Wordmark()
                IconButton(onClick = onProfile, modifier = Modifier.border(1.dp, Hairline, CircleShape)) {
                    ProfileAvatar(displayName = displayName, portraitUri = portraitUri, size = 36.dp)
                }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())), style = MaterialTheme.typography.labelLarge, color = SoftInk)
                Text("Your day, your way.", style = MaterialTheme.typography.headlineLarge)
                Text(
                    buildString {
                        append("Looks for ")
                        val currentName = displayName.ifBlank { LocalProfileStore.displayName(context) }.ifBlank {
                            SamplePersonProfiles.activeProfile(context).displayName.substringBefore(" ·")
                        }
                        append(currentName)
                        gender?.takeIf { it.isNotBlank() && it != GenderOptions.PREFER_NOT }?.let { append(" · $it") }
                        LocalProfileStore.postalCode(context).takeIf { it.length == 5 }?.let { append(" · ZIP $it") }
                        append(". Saved try-ons appear first.")
                    },
                    color = SoftInk,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                if (resolvedBodyUri.isNullOrBlank()) {
                    OutlinedButton(onClick = onSeedSamplePerson, modifier = Modifier.fillMaxWidth()) {
                        Text("Use sample Noah")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        items(looks, key = { "${profileEpoch}-${it.id}" }) { look ->
            DailyLookCard(
                look = look,
                bodyUri = resolvedBodyUri,
                profileEpoch = profileEpoch,
                onOpen = { onOpenLook(look) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
        if (looks.isEmpty()) {
            item {
                Box(
                    Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onOpenPlan)
                ) {
                    Image(painterResource(R.drawable.lookweave_editorial), "Plan a look for today", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .2f)))
                    Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        StatusLabel("NO LOOKS YET")
                        Text("Plan a look", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Text("Uses this profile’s wardrobe and prefs", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onDay, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Text("Day context & signals live on Day")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(16.dp))
            }
        }
        item {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onLab).padding(vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(Ink, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Science, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Fit & Feel Studio", style = MaterialTheme.typography.titleLarge)
                    Text("Preview, comfort map, and fit insights.", style = MaterialTheme.typography.bodyMedium, color = SoftInk)
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Ink)
            }
        }
    }
}

@Composable
private fun DailyLookCard(
    look: DailyLookSuggestion,
    bodyUri: String?,
    profileEpoch: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Paper)
            .border(1.dp, Hairline, RoundedCornerShape(14.dp))
            .clickable(onClick = onOpen)
            .padding(12.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OliveLight)
        ) {
            // Prefer prebaked try-on for a hero outer/top; else full-body person photo (Fit, not half-crop).
            // profileEpoch invalidates when sample body.jpg is overwritten in place (same URI path).
            LookHeroImage(
                bodyUri = bodyUri,
                garmentIds = look.plan.resolved.map { it.id },
                profileEpoch = profileEpoch,
                contentDescription = "Your look for ${look.title}"
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .12f)))
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                look.plan.resolved.take(5).forEach { garment ->
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Paper)
                            .border(1.dp, Hairline, RoundedCornerShape(8.dp))
                    ) {
                        GarmentProductImage(garment, Modifier.fillMaxSize())
                    }
                }
            }
            Column(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .background(Paper.copy(alpha = .92f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text("${look.score}", color = Olive, fontWeight = FontWeight.Medium, fontSize = 22.sp)
                Text("SCORE", color = SoftInk, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(look.title, style = MaterialTheme.typography.titleLarge, color = Ink)
        Text(
            "${look.plan.resolved.size} pieces · ${look.plan.resolved.joinToString(" · ") { it.category.label }}",
            color = SoftInk,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        look.reasons.take(5).forEach { reason ->
            Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                Text("•", color = Olive, modifier = Modifier.padding(end = 8.dp))
                Text(reason, color = Ink, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LookHeroImage(
    bodyUri: String?,
    garmentIds: List<String>,
    profileEpoch: Int,
    contentDescription: String
) {
    val context = LocalContext.current
    val garmentKey = garmentIds.joinToString("|")
    var bitmap by remember(bodyUri, garmentKey, profileEpoch) {
        mutableStateOf<android.graphics.Bitmap?>(null)
    }
    LaunchedEffect(bodyUri, garmentKey, profileEpoch) {
        bitmap = withContext(Dispatchers.IO) {
            // Try prebaked composites for outer → top → first garment before raw body.
            val tryStacks = buildList {
                val outer = garmentIds.firstOrNull { id ->
                    LookWeaveRepository.allGarments.firstOrNull { it.id == id }?.category == GarmentCategory.OUTERWEAR
                }
                val top = garmentIds.firstOrNull { id ->
                    LookWeaveRepository.allGarments.firstOrNull { it.id == id }?.category == GarmentCategory.TOP
                }
                val bottom = garmentIds.firstOrNull { id ->
                    LookWeaveRepository.allGarments.firstOrNull { it.id == id }?.category == GarmentCategory.BOTTOM
                }
                if (garmentIds.isNotEmpty()) add(garmentIds)
                val clothing = listOfNotNull(outer, top, bottom)
                if (clothing.isNotEmpty()) add(clothing)
                val topBottom = listOfNotNull(top, bottom)
                if (topBottom.isNotEmpty()) add(topBottom)
                val outerTop = listOfNotNull(outer, top)
                if (outerTop.isNotEmpty()) add(outerTop)
                outer?.let { add(listOf(it)) }
                top?.let { add(listOf(it)) }
                bottom?.let { add(listOf(it)) }
                garmentIds.firstOrNull()?.let { add(listOf(it)) }
            }.distinct()
            for (stack in tryStacks) {
                TryOnCompositeCache.load(context, bodyUri, stack)?.bitmap?.let { return@withContext it }
            }
            val bodyBmp = bodyUri?.let { value ->
                runCatching {
                    when {
                        value.startsWith("file:") || value.startsWith("/") -> {
                            val path = if (value.startsWith("file:")) Uri.parse(value).path else value
                            path?.let {
                                BitmapFactory.decodeFile(it, BitmapFactory.Options().apply {
                                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                                })
                            }
                        }
                        else -> context.contentResolver.openInputStream(Uri.parse(value))?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                }.getOrNull()
            }
            // No YouCam hit: stitch body + first unique capsule garment so Home isn't bare tee+jeans.
            val heroGarmentId = tryStacks.flatten().firstOrNull() ?: garmentIds.firstOrNull()
            val garmentBmp = heroGarmentId?.let { id ->
                garmentDrawableRes(id)?.let { resId ->
                    runCatching { BitmapFactory.decodeResource(context.resources, resId) }.getOrNull()
                }
            }
            when {
                bodyBmp != null && garmentBmp != null -> {
                    val h = maxOf(bodyBmp.height, garmentBmp.height).coerceAtLeast(1)
                    val bodyW = (h.toFloat() / bodyBmp.height * bodyBmp.width).toInt().coerceAtLeast(1)
                    val gW = (h.toFloat() / garmentBmp.height * garmentBmp.width).toInt().coerceAtLeast(1)
                    val out = android.graphics.Bitmap.createBitmap(bodyW + gW + 24, h, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(out)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    val scaledBody = android.graphics.Bitmap.createScaledBitmap(bodyBmp, bodyW, h, true)
                    val scaledG = android.graphics.Bitmap.createScaledBitmap(garmentBmp, gW, h, true)
                    canvas.drawBitmap(scaledBody, 0f, 0f, null)
                    canvas.drawBitmap(scaledG, (bodyW + 24).toFloat(), 0f, null)
                    out
                }
                else -> bodyBmp ?: garmentBmp
            }
        }
    }
    when {
        bitmap != null -> Image(
            bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
            contentScale = ContentScale.Fit
        )
        else -> Image(
            painterResource(R.drawable.lookweave_editorial),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
internal fun DaySignalsScreen(onBack: (() -> Unit)? = null, onUse: (DayContext) -> Unit) {
    var place by rememberSaveable { mutableStateOf("Brooklyn, NY") }
    var departure by rememberSaveable { mutableStateOf("08:00") }
    var returnTime by rememberSaveable { mutableStateOf("18:00") }
    var result by remember { mutableStateOf<DayContext?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        if (place.isBlank() || loading) return
        loading = true
        error = null
        scope.launch {
            runCatching { LookWeaveGateway.dayContext(place, departure, returnTime) }
                .onSuccess { result = it }
                .onFailure { error = it.message ?: "Day Signals could not be loaded." }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize()) {
        if (onBack != null) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                Box(Modifier.size(36.dp).background(OliveLight, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.WbSunny, contentDescription = null, tint = Olive, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Live decision layer", style = MaterialTheme.typography.labelLarge, color = SoftInk)
                    Text("Day Signals", style = MaterialTheme.typography.headlineMedium)
                }
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                Text("Live decision layer", style = MaterialTheme.typography.labelLarge, color = SoftInk)
                Text("Day Signals", style = MaterialTheme.typography.titleLarge)
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("A forecast tells you what is outside. LookWeave decides whether it justifies changing what you wear.", style = MaterialTheme.typography.bodyLarge, color = SoftInk)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = place,
                        onValueChange = { place = it },
                        placeholder = { Text("City or postal code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(2.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedIndicatorColor = Olive, unfocusedIndicatorColor = Hairline)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(onClick = { refresh() }, enabled = !loading, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Olive)) {
                        if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Paper)
                        else Icon(Icons.Rounded.Search, "Refresh Day Signals")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextField(departure, { departure = it.take(5) }, label = { Text("Leave") }, placeholder = { Text("08:00") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(2.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedIndicatorColor = Olive, unfocusedIndicatorColor = Hairline))
                    TextField(returnTime, { returnTime = it.take(5) }, label = { Text("Return") }, placeholder = { Text("18:00") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(2.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedIndicatorColor = Olive, unfocusedIndicatorColor = Hairline))
                }
                error?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = Clay, style = MaterialTheme.typography.bodyMedium)
                }
            }
            result?.let { day ->
                item {
                    Box(
                        Modifier.fillMaxWidth().height(244.dp)
                            .clip(RoundedCornerShape(topEnd = 34.dp, bottomStart = 34.dp))
                            .background(Brush.linearGradient(listOf(Ink, Olive, Color(0xFF6F806C))))
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val step = size.width / 7f
                            repeat(9) { index ->
                                val x = (index - 1) * step
                                drawLine(Color.White.copy(alpha = .08f), Offset(x, 0f), Offset(x + step * 1.8f, size.height), 1.dp.toPx())
                            }
                            drawCircle(Brass.copy(alpha = .16f), radius = size.minDimension * .34f, center = Offset(size.width * .88f, size.height * .08f))
                            drawCircle(Color.White.copy(alpha = .10f), radius = size.minDimension * .18f, center = Offset(size.width * .88f, size.height * .08f), style = Stroke(1.dp.toPx()))
                        }
                        Column(Modifier.fillMaxSize().padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("LIVE DECISION LAYER", color = Brass, style = MaterialTheme.typography.labelSmall)
                                Spacer(Modifier.weight(1f))
                                Box(Modifier.size(8.dp).background(Color(0xFFA8D5A2), CircleShape))
                            }
                            Spacer(Modifier.weight(1f))
                            Text(day.location, color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelLarge)
                            Text(day.signals.firstOrNull()?.value ?: "Live", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                day.signals.drop(1).take(3).forEach { signal ->
                                    Column(
                                        Modifier.weight(1f).background(Color.White.copy(alpha = .10f), RoundedCornerShape(12.dp)).padding(10.dp)
                                    ) {
                                        Text(signal.label.uppercase(), color = Color.White.copy(alpha = .62f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        Text(signal.value, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Text("What changes the weave", style = MaterialTheme.typography.headlineMedium)
                    Text("Day conditions can gently reshape today’s look when they matter.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                }
                items(day.recommendations) { recommendation ->
                    Row(Modifier.fillMaxWidth().background(if (recommendation.action == "confirm") Color(0xFFF3E8DC) else OliveLight, RoundedCornerShape(2.dp)).padding(16.dp), verticalAlignment = Alignment.Top) {
                        Box(Modifier.size(32.dp).border(1.dp, if (recommendation.action == "confirm") Clay else Olive, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(if (recommendation.action == "confirm") Icons.Rounded.PersonOutline else Icons.Rounded.Check, null, tint = if (recommendation.action == "confirm") Clay else Olive, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(if (recommendation.action == "confirm") "Ask first" else if (recommendation.action == "none") "No change" else "Day condition", style = MaterialTheme.typography.labelLarge, color = if (recommendation.action == "confirm") Clay else Olive)
                            Text(recommendation.title, style = MaterialTheme.typography.titleMedium)
                            Text(recommendation.reason, style = MaterialTheme.typography.bodyMedium, color = SoftInk)
                        }
                    }
                }
                if (day.hourly.isNotEmpty()) {
                    item {
                        Text("Whole day outlook", style = MaterialTheme.typography.headlineSmall)
                        Text("${day.departure}–${day.returnTime} · Conditions can change while you are out. Carry decisions remain suggestions until you confirm them.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(day.hourly.take(10)) { hour ->
                                Column(Modifier.width(92.dp).background(Paper, RoundedCornerShape(10.dp)).border(1.dp, Hairline, RoundedCornerShape(10.dp)).padding(10.dp)) {
                                    Text(hour.time.takeLast(5), style = MaterialTheme.typography.labelSmall, color = Olive)
                                    Text(hour.temperatureF?.let { "${it.toInt()}°" } ?: "—", style = MaterialTheme.typography.titleMedium)
                                    Text(hour.precipitationPercent?.let { "${it.toInt()}% rain" } ?: "No rain data", style = MaterialTheme.typography.labelSmall, color = SoftInk)
                                }
                            }
                        }
                    }
                }
                item {
                    Button(onClick = { onUse(day) }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(2.dp)) {
                        Text("Use in today’s weave")
                        Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(day.attribution, color = SoftInk, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun InsightRow(title: String, subtitle: String, color: Color, positive: Boolean) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(46.dp).background(color.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(if (positive) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = null, tint = color)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = SoftInk)
        }
        Text(if (positive) "KEEP" else "SWAP", style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun SectionTitle(title: String, eyebrow: String? = null, icon: ImageVector? = null) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        eyebrow?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = SoftInk) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(Modifier.size(36.dp).background(OliveLight, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Olive, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
            }
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun SectionLabel(title: String, icon: ImageVector) {
    Row(Modifier.padding(top = 4.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).background(OliveLight, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Olive, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = Olive)
    }
}

@Composable
private fun StatusLabel(text: String, dark: Boolean = true) {
    Text(
        text,
        color = if (dark) Ink else Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.background(if (dark) Paper.copy(alpha = .92f) else Ink.copy(alpha = .85f), RoundedCornerShape(2.dp)).padding(horizontal = 9.dp, vertical = 6.dp)
    )
}

@Composable
private fun WardrobeScreen(
    onSelect: (Garment) -> Unit,
    onImported: (Garment) -> Unit,
    onDiscover: () -> Unit
) {
    var category by rememberSaveable { mutableStateOf<GarmentCategory?>(null) }
    var clothingType by rememberSaveable { mutableStateOf<String?>(null) }
    var forYouId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var showImport by rememberSaveable { mutableStateOf(false) }
    var productUrl by rememberSaveable { mutableStateOf("") }
    var product by remember { mutableStateOf<org.json.JSONObject?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    var photoName by rememberSaveable { mutableStateOf("") }
    val importScope = rememberCoroutineScope()
    val wardrobeContext = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importing = true
        importError = null
        importScope.launch {
            runCatching {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val id = "photo-${System.currentTimeMillis()}"
                    val saved = LocalProfileStore.copyGarmentImageToAppFiles(wardrobeContext, uri, id)
                        ?: error("Could not save the garment photo")
                    val name = photoName.trim().ifBlank { "Logged garment" }
                    Garment(
                        id = id,
                        name = name,
                        subtitle = "Logged from photo · on device",
                        category = GarmentCategory.TOP,
                        colorName = "Imported",
                        color = Color(0xFF7C897E),
                        closure = Evidence("Unknown", EvidenceSource.ESTIMATED, 0),
                        fabric = Evidence("Unknown", EvidenceSource.ESTIMATED, 0),
                        wearability = 50,
                        oneHandFriendly = false,
                        seatedComfort = false,
                        lowSensoryLoad = false,
                        deviceClearance = false,
                        note = "Logged from a photo. Closure, fabric and wearability stay unknown until verified.",
                        imageUrl = saved
                    )
                }
            }.onSuccess { garment ->
                LookWeaveRepository.addImported(garment)
                onImported(garment)
                showImport = false
                photoName = ""
                product = null
                productUrl = ""
            }.onFailure { importError = it.message ?: "Photo import failed" }
            importing = false
        }
    }
    val activeProfileId = SamplePersonProfiles.activeId(wardrobeContext)
    val activeWardrobe = remember(activeProfileId) {
        SamplePersonProfiles.wardrobeForActiveProfile(wardrobeContext)
    }
    val forYouSuggestions = remember(activeProfileId) {
        activeWardrobe.take(10)
    }
    val shown = activeWardrobe.filter {
        (category == null || it.category == category) &&
            garmentMatchesClothingType(it, clothingType) &&
            (forYouId == null || it.id == forYouId) &&
            (query.isBlank() || it.name.contains(query, true))
    }
    val sampleCount = LookWeaveRepository.garments.size
    val importedCount = LookWeaveRepository.importedGarments.size
    val preferredCount = SamplePersonProfiles.activeProfile(wardrobeContext).preferredGarmentIds.size

    Column(Modifier.fillMaxSize()) {
        PageHeader(
            "Your wardrobe",
            buildString {
                append("${activeWardrobe.size} pieces")
                if (preferredCount > 0) append(" · $preferredCount for this profile")
                if (importedCount > 0) append(" · $importedCount logged")
                if (sampleCount > 0) append(" · $sampleCount sample")
            },
            Icons.Rounded.Checkroom
        ) {
            FilledIconButton(onClick = { showImport = true }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = Olive, contentColor = Color.White)) {
                Icon(Icons.Rounded.Add, "Add a garment")
            }
        }
        WardrobeActionCard(
            eyebrow = "FIND MORE",
            title = "Adaptive store directory",
            subtitle = "Nearby places, product picks, and curated retailers matched to your filters.",
            onClick = onDiscover,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Find a piece") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            shape = RoundedCornerShape(2.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedIndicatorColor = Olive, unfocusedIndicatorColor = Hairline)
        )
        ClothingAndForYouFilters(
            clothingType = clothingType,
            onClothingType = { clothingType = it; forYouId = null },
            suggestions = forYouSuggestions,
            forYouId = forYouId,
            onForYou = { garment ->
                forYouId = garment?.id
                if (garment != null) category = garment.category
            },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { CompactChip("All categories", category == null) { category = null; forYouId = null } }
            items(GarmentCategory.entries) { item -> CompactChip(item.label, category == item) { category = item; forYouId = null } }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(shown.chunked(2)) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { garment -> GarmentCard(garment, Modifier.weight(1f)) { onSelect(garment) } }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            item { Spacer(Modifier.height(22.dp)) }
        }
    }
    if (showImport) AlertDialog(
        onDismissRequest = { if (!importing) showImport = false },
        title = { Text("Add a product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Log a garment from a photo, or paste a product URL to review live details.", style = MaterialTheme.typography.bodyMedium)
                TextField(
                    photoName,
                    { photoName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Name for photo log (optional)") },
                    singleLine = true
                )
                OutlinedButton(
                    onClick = { photoPicker.launch("image/*") },
                    enabled = !importing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Checkroom, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (importing) "Saving photo…" else "Upload garment photo")
                }
                Text("Or review from a product URL", color = SoftInk, style = MaterialTheme.typography.labelMedium)
                TextField(productUrl, { productUrl = it; product = null; importError = null }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("https://...") }, singleLine = true)
                importError?.let { Text(it, color = Clay, style = MaterialTheme.typography.bodySmall) }
                product?.let { value ->
                    Text(value.optString("name"), style = MaterialTheme.typography.titleMedium)
                    Text("Category: ${value.optString("category").ifBlank { "Unknown" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Material: ${value.optString("material").ifBlank { "Unknown" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Closure: ${value.optString("closure").ifBlank { "Unknown" }}", style = MaterialTheme.typography.bodySmall)
                    Text("Price: ${value.optString("price").ifBlank { "Unknown" }} · Stock: Unknown", style = MaterialTheme.typography.bodySmall)
                    Text("Review every field. Unknown does not mean incompatible.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (product == null) Button(onClick = {
                importing = true; importError = null
                importScope.launch { runCatching { LookWeaveGateway.ingestProductUrl(productUrl) }.onSuccess { product = it }.onFailure { importError = it.message ?: "Product details unavailable" }; importing = false }
            }, enabled = productUrl.startsWith("http") && !importing) { Text(if (importing) "Checking…" else "Review source") }
            else Button(onClick = { product?.let { val garment = LookWeaveGateway.importedGarment(it); LookWeaveRepository.addImported(garment); onImported(garment); showImport = false; product = null; productUrl = "" } }) { Text("Save reviewed garment") }
        },
        dismissButton = { TextButton(onClick = { showImport = false }) { Text("Cancel") } }
    )
}

@Composable
private fun WardrobeActionCard(
    eyebrow: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF465C54))))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(eyebrow, color = Brass, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(4.dp))
            Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.bodyMedium)
        }
        Box(
            Modifier.size(44.dp).background(Paper.copy(alpha = .14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

private enum class DiscoveryMode { PICKS, MAP, DIRECTORY }

@Composable
private fun StoreDirectoryScreen(
    preferences: WearabilityPreferences,
    likedPlaceIds: Set<String>,
    passedPlaceIds: Set<String>,
    likedProductIds: Set<String>,
    passedProductIds: Set<String>,
    onDecision: (String, Boolean) -> Unit,
    onProductDecision: (String, Boolean) -> Unit,
    onResetDecisions: () -> Unit,
    onBack: () -> Unit,
    profileEpoch: Int = 0,
    liveSearchRequest: Int = 0
) {
    val discoverContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeProfile = remember(profileEpoch) { SamplePersonProfiles.activeProfile(discoverContext) }
    var mode by rememberSaveable { mutableStateOf(DiscoveryMode.PICKS) }
    var postalCode by remember(profileEpoch) {
        mutableStateOf(
            LocalProfileStore.postalCode(discoverContext).ifBlank {
                activeProfile.postalCode
            }.ifBlank { "78701" }
        )
    }
    var places by remember { mutableStateOf(emptyList<NearbyPlace>()) }
    var area by rememberSaveable { mutableStateOf("Your area") }
    var showFeedback by rememberSaveable { mutableStateOf(false) }
    var reviewedProducts by remember { mutableStateOf(emptyList<com.vizeno.loomie.integration.ProductCard>()) }
    var mapLatitude by rememberSaveable { mutableStateOf(40.6816) }
    var mapLongitude by rememberSaveable { mutableStateOf(-73.9798) }
    var loading by remember { mutableStateOf(true) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var accessorySubcategory by rememberSaveable { mutableStateOf<String?>(null) }
    var clothingTypeQuery by rememberSaveable { mutableStateOf<String?>(null) }
    var products by remember(activeProfile.id, profileEpoch) {
        mutableStateOf(SamplePersonaProducts.getOrPrebaked(activeProfile.id))
    }
    var productLoading by remember { mutableStateOf(false) }
    val accessorySubcategories = listOf(
        "glasses" to "Glasses",
        "watches" to "Watches",
        "jewellery" to "Jewellery",
        "scarf" to "Scarf",
        "hat" to "Hat",
        "shoes" to "Shoes"
    )

    fun searchNearby() {
        if (!postalCode.matches(Regex("\\d{5}"))) { searchError = "Enter a five-digit ZIP code"; return }
        LocalProfileStore.savePostalCode(discoverContext, postalCode)
        loading = true; searchError = null
        scope.launch {
            runCatching { LookWeaveGateway.nearbyPlaces(postalCode) }
                .onSuccess { result ->
                    places = result.places; area = result.area
                    mapLatitude = result.latitude; mapLongitude = result.longitude; loading = false
                }
                .onFailure { error -> searchError = error.message ?: "Nearby search is unavailable"; loading = false }
        }
    }

    fun loadProducts(subcategory: String? = accessorySubcategory, typeQuery: String? = clothingTypeQuery, forceLive: Boolean = false) {
        val category = subcategory ?: typeQuery
        if (!forceLive) {
            products = SamplePersonaProducts.getOrPrebaked(activeProfile.id, category)
            searchError = null
            return
        }
        productLoading = true
        searchError = null
        scope.launch {
            val priceLimit = LocalProfileStore.priceLimit(discoverContext)
            val gender = LocalProfileStore.genderLabel(discoverContext)
            val sizes = LocalProfileStore.loadSizes(discoverContext).asMap()
            val colors = LocalProfileStore.preferredColors(discoverContext)
            val styles = LocalProfileStore.preferredStyles(discoverContext)
            val interests = LocalProfileStore.interests(discoverContext)
            val zip = postalCode.takeIf { it.length == 5 } ?: LocalProfileStore.postalCode(discoverContext)
            val avoid = LocalProfileStore.avoidColors(discoverContext)
            val personalizedQuery = buildList {
                typeQuery?.takeIf { it.isNotBlank() }?.let { add(it) }
                addAll(interests.take(2))
                addAll(styles.take(1))
                addAll(colors.take(2))
                avoid.take(1).forEach { add("avoid $it") }
                zip.takeIf { it.length == 5 }?.let { add("near $it") }
                gender?.takeIf { it.isNotBlank() && it != GenderOptions.PREFER_NOT }?.let { add(it) }
            }.joinToString(" ").ifBlank { LocalProfileStore.discoverQueryHints(discoverContext) }
            runCatching {
                if (subcategory.isNullOrBlank()) {
                    LookWeaveGateway.discoverProducts(
                        preferences,
                        query = personalizedQuery,
                        priceLimit = priceLimit,
                        gender = gender,
                        sizes = sizes,
                        colors = colors,
                        styles = styles,
                        interests = interests,
                        postalCode = zip
                    )
                } else {
                    LookWeaveGateway.discoverAccessories(
                        org.json.JSONObject()
                            .put("subcategory", subcategory)
                            .put("oneHandClosure", preferences.oneHandClosure)
                            .put("easyFootwear", preferences.easyFootwear)
                            .put("lowSensoryLoad", preferences.lowSensoryLoad)
                            .put("gripStrength", preferences.gripStrength)
                            .put("gender", gender ?: "")
                            .put("colourDirection", LocalProfileStore.styleChoices(discoverContext)["colourDirection"] ?: "Explore")
                            .put("metal", LocalProfileStore.styleChoices(discoverContext)["metal"] ?: "Explore")
                            .put("faceShape", LocalProfileStore.styleChoices(discoverContext)["faceShape"] ?: "")
                            .put("colors", org.json.JSONArray(colors))
                            .put("interests", org.json.JSONArray(interests)),
                        subcategory
                    ).products
                }
            }.onSuccess { liveItems ->
                if (liveItems.isNotEmpty()) {
                    products = liveItems
                    SamplePersonaProducts.cache(activeProfile.id, category, liveItems)
                    reviewedProducts = (reviewedProducts + liveItems).distinctBy { card -> card.id }
                }
            }.onFailure { error ->
                searchError = "Live search unavailable · showing curated picks"
            }
            productLoading = false
        }
    }

    LaunchedEffect(postalCode) { searchNearby() }
    LaunchedEffect(activeProfile.id, profileEpoch, accessorySubcategory, clothingTypeQuery) {
        loadProducts(accessorySubcategory, clothingTypeQuery, forceLive = false)
    }
    LaunchedEffect(liveSearchRequest) {
        if (liveSearchRequest > 0) loadProducts(accessorySubcategory, clothingTypeQuery, forceLive = true)
    }

    Column(Modifier.fillMaxSize().background(Parchment)) {
        if (mode == DiscoveryMode.PICKS) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF29312D),
                shadowElevation = 2.dp
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to wardrobe", tint = Color.White)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("PRODUCT DISCOVERY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .65f))
                        Text("Curated Fashion Deck", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.clickable { searchNearby() }
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocationOn, null, tint = Brass, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (postalCode.isNotBlank()) postalCode else area, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(onClick = { loadProducts(accessorySubcategory, clothingTypeQuery, forceLive = true) }, enabled = !productLoading) {
                        Icon(Icons.Rounded.Refresh, "Refresh products", tint = Color.White)
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().height(260.dp).background(Brush.linearGradient(listOf(Color(0xFF29312D), Color(0xFF465C54), Color(0xFF82968B))))) {
                Canvas(Modifier.fillMaxSize()) {
                    repeat(5) { index -> drawCircle(Color.White.copy(alpha = .035f), 80f + index * 70f, Offset(size.width * .88f, size.height * .1f), style = Stroke(2f)) }
                }
                Column(Modifier.statusBarsPadding().padding(start = 8.dp, end = 20.dp, top = 5.dp, bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to wardrobe", tint = Color.White) }
                        Column(Modifier.weight(1f)) {
                            Text("NEARBY DISCOVERY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .62f))
                            Text("Find what earns the trip", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        }
                        Text(area, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = .8f))
                    }
                    Row(Modifier.padding(start = 12.dp, top = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = postalCode, onValueChange = { postalCode = it.filter(Char::isDigit).take(5) },
                            modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Enter ZIP") },
                            leadingIcon = { Icon(Icons.Rounded.LocationOn, null) }, shape = RoundedCornerShape(14.dp),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                        )
                        Button(onClick = { searchNearby() }, enabled = !loading, modifier = Modifier.height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Brass, contentColor = Ink)) { Text("Explore", fontWeight = FontWeight.Bold) }
                    }
                    Row(
                        Modifier.padding(start = 12.dp, top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                mode = DiscoveryMode.PICKS
                                loadProducts(accessorySubcategory, forceLive = true)
                            },
                            modifier = Modifier.height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = .45f))
                        ) {
                            Icon(Icons.Rounded.Language, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Search online")
                        }
                        Text(
                            searchError ?: "ZIP for nearby · Search online for products",
                            color = if (searchError == null) Color.White.copy(alpha = .65f) else Color(0xFFFFD0C7),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        if (loading || productLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Brass, trackColor = OliveLight)
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).background(OliveLight, RoundedCornerShape(12.dp)).padding(3.dp)) {
            listOf(
                Triple(DiscoveryMode.PICKS, "Products", Icons.Rounded.Style),
                Triple(DiscoveryMode.MAP, "Map", Icons.Rounded.Map),
                Triple(DiscoveryMode.DIRECTORY, "All stores", Icons.Rounded.Storefront)
            ).forEach { (value, label, tabIcon) ->
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (mode == value) Paper else Color.Transparent)
                        .clickable { mode = value }.padding(vertical = 8.dp), contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(tabIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (mode == value) Olive else SoftInk)
                        Text(label, style = MaterialTheme.typography.labelLarge, color = if (mode == value) Ink else SoftInk)
                    }
                }
            }
        }
        AnimatedContent(
            targetState = mode,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            label = "discovery-mode"
        ) { activeMode ->
            when (activeMode) {
                DiscoveryMode.PICKS -> Column(Modifier.fillMaxSize()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filterOptions = listOf(
                            null to "All",
                            "tops" to "Tops",
                            "bottoms" to "Bottoms",
                            "layers" to "Outerwear",
                            "shoes" to "Shoes",
                            "glasses" to "Glasses",
                            "watches" to "Watches",
                            "jewellery" to "Jewellery",
                            "scarf" to "Scarf",
                            "hat" to "Hat"
                        )
                        items(filterOptions) { (key, label) ->
                            val isSelected = (clothingTypeQuery == key) || (accessorySubcategory == key) || (key == null && clothingTypeQuery == null && accessorySubcategory == null)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    when (key) {
                                        null -> { clothingTypeQuery = null; accessorySubcategory = null }
                                        "shoes", "glasses", "watches", "jewellery", "scarf", "hat" -> {
                                            accessorySubcategory = key
                                            clothingTypeQuery = null
                                        }
                                        else -> {
                                            clothingTypeQuery = key
                                            accessorySubcategory = null
                                        }
                                    }
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Olive,
                                    selectedLabelColor = Color.White,
                                    containerColor = Paper,
                                    labelColor = Ink
                                )
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            listOfNotNull(
                                LocalProfileStore.genderLabel(discoverContext),
                                LocalProfileStore.preferredColors(discoverContext).take(2).joinToString(" · ").takeIf { it.isNotBlank() }
                            ).joinToString(" · ").ifBlank { "Personalized live search" },
                            color = SoftInk,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showFeedback = !showFeedback }) {
                            Text(if (showFeedback) "Back to deck" else "Decisions (${likedProductIds.size + passedProductIds.size})")
                        }
                    }
                    if (showFeedback) {
                        ProductFeedbackPanel(
                            likedIds = likedProductIds,
                            passedIds = passedProductIds,
                            catalog = reviewedProducts,
                            onClear = onResetDecisions,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        )
                    } else {
                        ProductDiscoveryDeck(
                            products.filterNot { it.id in likedProductIds || it.id in passedProductIds },
                            likedProductIds.size,
                            productLoading,
                            onProductDecision,
                            onResetDecisions,
                            subtitle = accessorySubcategory?.let { key ->
                                "Live ${accessorySubcategories.firstOrNull { it.first == key }?.second?.lowercase() ?: key} · gender+ZIP+colors"
                            } ?: "Live clothing · gender+ZIP+colors (gateway required)"
                        )
                    }
                }
                DiscoveryMode.MAP -> InteractiveAccessMap(
                    places.sortedWith(compareBy<NearbyPlace> { place ->
                        val wanted = RetailerDirectory.wantedFeatures(preferences)
                        if (wanted.isEmpty() || place.features.any { it in wanted }) 0 else 1
                    }.thenBy { place ->
                        if (!preferences.stepFreeAccess) 0 else when (place.accessibility) {
                            "yes" -> 0
                            "unknown" -> 1
                            else -> 2
                        }
                    }),
                    likedPlaceIds, mapLatitude, mapLongitude, area, { onDecision(it.id, true) },
                    RetailerDirectory.retailers.filter { RetailerDirectory.isOnline(it) && RetailerDirectory.matchesPreferences(it, preferences) }.ifEmpty {
                        RetailerDirectory.retailers.filter { RetailerDirectory.isOnline(it) }
                    },
                    preferences.stepFreeAccess
                )
                DiscoveryMode.DIRECTORY -> StoreDirectoryList(preferences, onBack, embedded = true)
            }
        }
    }
}

@Composable
private fun DiscoveryDeck(
    places: List<NearbyPlace>, likedCount: Int,
    onDecision: (String, Boolean) -> Unit, onShowMap: () -> Unit, onResetDecisions: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val dragX = remember { Animatable(0f) }
    val current = places.firstOrNull()
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("WEARABILITY MATCHES", style = MaterialTheme.typography.labelSmall, color = SoftInk)
                Text(if (current == null) "Your shortlist is ready" else "${places.size} places left", style = MaterialTheme.typography.titleLarge)
            }
            if (likedCount > 0) TextButton(onClick = onShowMap) { Icon(Icons.Rounded.Map, null); Spacer(Modifier.width(5.dp)); Text("$likedCount saved") }
        }
        Spacer(Modifier.height(12.dp))
        if (current == null) {
            Box(Modifier.fillMaxWidth().weight(1f).background(Paper, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Map, null, tint = Olive, modifier = Modifier.size(52.dp)); Spacer(Modifier.height(14.dp))
                    Text("See your places together", style = MaterialTheme.typography.headlineSmall)
                    Text("Saved pins are highlighted on the map.", color = SoftInk)
                    OutlinedButton(onClick = onResetDecisions, modifier = Modifier.padding(top = 12.dp)) { Text("Start a new pick round") }
                    Button(onClick = onShowMap, modifier = Modifier.padding(top = 16.dp)) { Text("Open map") }
                }
            }
        } else {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                places.getOrNull(1)?.let { Box(Modifier.fillMaxSize(.94f).background(it.color.copy(alpha = .16f), RoundedCornerShape(24.dp))) }
                Column(
                    Modifier.fillMaxSize().graphicsLayer {
                        translationX = dragX.value; rotationZ = dragX.value / 32f
                        alpha = 1f - (abs(dragX.value) / 1100f).coerceIn(0f, .35f)
                    }.pointerInput(current.id) {
                        detectDragGestures(
                            onDragEnd = { scope.launch {
                                if (abs(dragX.value) > 120f) {
                                    val liked = dragX.value > 0; dragX.animateTo(if (liked) 1100f else -1100f, tween(190))
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDecision(current.id, liked); dragX.snapTo(0f)
                                } else dragX.animateTo(0f)
                            } },
                            onDragCancel = { scope.launch { dragX.animateTo(0f) } }
                        ) { _, amount -> scope.launch { dragX.snapTo(dragX.value + amount.x) } }
                    }.clip(RoundedCornerShape(24.dp)).background(Paper)
                ) {
                    Box(Modifier.fillMaxWidth().weight(1f).background(Brush.linearGradient(listOf(current.color, current.color.copy(alpha = .62f))))) {
                        Canvas(Modifier.fillMaxSize()) {
                            repeat(7) { i -> drawCircle(Color.White.copy(alpha = .07f), size.minDimension * (.12f + i * .035f), Offset(size.width * .8f, size.height * .2f)) }
                            drawLine(Color.White.copy(alpha = .32f), Offset(0f, size.height * .8f), Offset(size.width, size.height * .42f), 2f)
                        }
                        Text("${current.match}", color = Color.White, fontSize = 62.sp, fontWeight = FontWeight.Light, modifier = Modifier.align(Alignment.TopEnd).padding(22.dp))
                        Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) {
                            Text(current.neighborhood.uppercase(Locale.US), color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.labelSmall)
                            Text(current.name, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            Text("${current.distanceMiles} mi · ${current.address}", color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Column(Modifier.padding(20.dp)) {
                        Text(current.reason, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilledIconButton(onClick = { scope.launch { dragX.animateTo(-1100f, tween(190)); haptics.performHapticFeedback(HapticFeedbackType.LongPress); onDecision(current.id, false); dragX.snapTo(0f) } }, colors = IconButtonDefaults.filledIconButtonColors(Paper, Ink), modifier = Modifier.size(58.dp)) { Icon(Icons.Rounded.Close, "Pass") }
                FilledIconButton(onClick = { scope.launch { dragX.animateTo(1100f, tween(190)); haptics.performHapticFeedback(HapticFeedbackType.LongPress); onDecision(current.id, true); dragX.snapTo(0f) } }, colors = IconButtonDefaults.filledIconButtonColors(Olive, Color.White), modifier = Modifier.size(66.dp)) { Icon(Icons.Rounded.Favorite, "Save place") }
                FilledIconButton(onClick = onShowMap, colors = IconButtonDefaults.filledIconButtonColors(Paper, Ink), modifier = Modifier.size(58.dp)) { Icon(Icons.Rounded.Map, "Open map") }
            }
            Text("Swipe left to pass · right to save", color = SoftInk, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun ProductFeedbackPanel(
    likedIds: Set<String>,
    passedIds: Set<String>,
    catalog: List<com.vizeno.loomie.integration.ProductCard>,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liked = catalog.filter { it.id in likedIds }
    val passed = catalog.filter { it.id in passedIds }
    Column(modifier.padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Your product feedback", style = MaterialTheme.typography.titleLarge)
        Text(
            "Liked and disliked items stay with this profile and steer Loomie + discover.",
            color = SoftInk,
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Liked (${likedIds.size})", style = MaterialTheme.typography.titleMedium, color = Olive)
        if (liked.isEmpty()) {
            Text(
                if (likedIds.isEmpty()) "No likes yet — swipe right on products."
                else "Liked ${likedIds.size} product id(s); reopen search to refresh titles.",
                color = SoftInk
            )
        } else {
            liked.forEach { card ->
                Row(
                    Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Favorite, null, tint = Olive, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(card.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(card.retailer, color = SoftInk, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Text("Disliked (${passedIds.size})", style = MaterialTheme.typography.titleMedium, color = Clay)
        if (passed.isEmpty()) {
            Text(
                if (passedIds.isEmpty()) "No passes yet — swipe left to skip."
                else "Passed ${passedIds.size} product id(s); reopen search to refresh titles.",
                color = SoftInk
            )
        } else {
            passed.forEach { card ->
                Row(
                    Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Close, null, tint = Clay, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(card.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(card.retailer, color = SoftInk, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear likes & dislikes for this profile") }
    }
}

@Composable
private fun ProductDiscoveryDeck(
    products: List<com.vizeno.loomie.integration.ProductCard>,
    likedCount: Int,
    loading: Boolean,
    onDecision: (String, Boolean) -> Unit,
    onReset: () -> Unit,
    subtitle: String = "Live clothing discover results"
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val dragX = remember { Animatable(0f) }
    val current = products.firstOrNull()
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Olive)
                    Text("Finding live product matches…", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else if (current == null) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Paper, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    LoomieMascot(LoomieMood.Happy, size = 72.dp)
                    Text("You reviewed every product", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
                    Text("Your likes and passes help LookWeave learn what to surface next.", color = SoftInk, textAlign = TextAlign.Center)
                    OutlinedButton(onClick = onReset, modifier = Modifier.padding(top = 16.dp)) { Text("Start a new product round") }
                }
            }
        } else {
            // Card container with high-res product photo & metadata
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                products.getOrNull(1)?.let {
                    Box(
                        Modifier
                            .fillMaxSize(0.95f)
                            .offset(y = 8.dp)
                            .background(OliveLight.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    )
                }
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Paper,
                    tonalElevation = 4.dp,
                    border = BorderStroke(1.dp, Hairline),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = dragX.value
                            rotationZ = dragX.value / 28f
                            alpha = 1f - (abs(dragX.value) / 1100f).coerceIn(0f, .35f)
                        }
                        .pointerInput(current.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        if (abs(dragX.value) > 120f) {
                                            val liked = dragX.value > 0
                                            dragX.animateTo(if (liked) 1100f else -1100f, tween(190))
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onDecision(current.id, liked)
                                            dragX.snapTo(0f)
                                        } else {
                                            dragX.animateTo(0f)
                                        }
                                    }
                                }
                            ) { _, amount ->
                                scope.launch { dragX.snapTo(dragX.value + amount.x) }
                            }
                        }
                ) {
                    Column(Modifier.fillMaxSize()) {
                        // High-res product image on clean white
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .weight(1.5f)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            ProductImage(current.imageUrl, current.name, Modifier.fillMaxSize().padding(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Paper.copy(alpha = 0.94f),
                                border = BorderStroke(0.5.dp, Hairline),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    current.compatibility.ifBlank { "Live Match" },
                                    color = Olive,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                        // Product details below image
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                current.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    current.retailer,
                                    color = Olive,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                current.price?.let {
                                    Text("•", color = SoftInk)
                                    Text(
                                        it,
                                        color = Olive,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                current.compatibility.ifBlank { current.reason },
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftInk,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Bottom Tinder controls: Pass (X) and Like (Heart)
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pass button (X)
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            dragX.animateTo(-1100f, tween(190))
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDecision(current.id, false)
                            dragX.snapTo(0f)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFF2EAE5),
                        contentColor = Clay
                    ),
                    modifier = Modifier.size(58.dp)
                ) {
                    Icon(Icons.Rounded.Close, "Pass product", modifier = Modifier.size(28.dp))
                }

                Text(
                    "Swipe or tap",
                    color = SoftInk.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )

                // Like button (Heart)
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            dragX.animateTo(1100f, tween(190))
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDecision(current.id, true)
                            dragX.snapTo(0f)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Olive,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Rounded.Favorite, "Like product", modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductImage(url: String?, description: String, modifier: Modifier) {
    val context = LocalContext.current
    val resolvedUrl = remember(url) {
        if (url?.contains("media-amazon.com/images") == true) {
            url.replace(Regex("\\._AC_[^.]*_\\."), "._AC_SL800_.")
        } else url
    }
    var bitmap by remember(resolvedUrl) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var failed by remember(resolvedUrl) { mutableStateOf(resolvedUrl.isNullOrBlank()) }
    LaunchedEffect(resolvedUrl) {
        if (resolvedUrl.isNullOrBlank()) {
            bitmap = null
            failed = true
            return@LaunchedEffect
        }
        failed = false
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                if (resolvedUrl.startsWith("drawable://") || resolvedUrl.startsWith("res://") || (!resolvedUrl.startsWith("http://") && !resolvedUrl.startsWith("https://"))) {
                    val resName = resolvedUrl.removePrefix("drawable://").removePrefix("res://")
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) {
                        BitmapFactory.decodeResource(context.resources, resId)
                    } else null
                } else {
                    val connection = (java.net.URL(resolvedUrl).openConnection() as java.net.HttpURLConnection).apply {
                        connectTimeout = 12_000
                        readTimeout = 12_000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                        setRequestProperty("Connection", "close")
                    }
                    try {
                        if (connection.responseCode !in 200..299) return@withContext null
                        val buffer = java.io.ByteArrayOutputStream()
                        val chunk = ByteArray(16384)
                        var bytesRead: Int
                        val stream = java.io.BufferedInputStream(connection.inputStream)
                        try {
                            while (stream.read(chunk).also { bytesRead = it } != -1) {
                                buffer.write(chunk, 0, bytesRead)
                            }
                        } catch (_: java.io.IOException) {}
                        val bytes = buffer.toByteArray()
                        if (bytes.isEmpty()) return@withContext null
                        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                        var sample = 1
                        while (sample < 16 && (bounds.outWidth / sample > 1400 || bounds.outHeight / sample > 1400)) sample *= 2
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
                    } finally {
                        connection.disconnect()
                    }
                }
            }.getOrNull()
        }
        failed = bitmap == null
    }
    when {
        bitmap != null -> Image(bitmap!!.asImageBitmap(), description, modifier, contentScale = ContentScale.Fit)
        !failed -> Box(modifier, contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Olive) }
        else -> Box(modifier, contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                Icon(Icons.Rounded.Checkroom, null, tint = Olive, modifier = Modifier.size(52.dp))
                Spacer(Modifier.height(10.dp))
                Text("Product image unavailable", color = SoftInk, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}

/** Minimal colored light basemap (OpenFreeMap Liberty) — readable streets, not greyscale. */
private const val MAP_STYLE_URI = "https://tiles.openfreemap.org/styles/liberty"

@Composable
private fun InteractiveAccessMap(
    places: List<NearbyPlace>, likedIds: Set<String>, centerLatitude: Double, centerLongitude: Double, area: String,
    onLike: (NearbyPlace) -> Unit, onlineShops: List<com.vizeno.loomie.model.Retailer> = emptyList(), stepFree: Boolean = false
) {
    val context = LocalContext.current
    var selectedId by rememberSaveable(places.map { it.id }) { mutableStateOf(places.firstOrNull { it.id in likedIds }?.id ?: places.firstOrNull()?.id) }
    val selected = places.firstOrNull { it.id == selectedId } ?: places.firstOrNull()

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ACCESS MAP", style = MaterialTheme.typography.labelSmall, color = Olive)
                Text(
                    if (places.isEmpty()) "No verified matches yet" else "${places.size} places around $area",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                "ZIP area",
                style = MaterialTheme.typography.labelMedium,
                color = SoftInk,
                modifier = Modifier.background(Paper, CircleShape).padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        if (onlineShops.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(onlineShops.take(6)) { shop ->
                    Row(
                        Modifier
                            .background(Paper, RoundedCornerShape(14.dp))
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shop.shopUrl)))
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(shop.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        Text("Shop", color = Olive, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (places.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(22.dp)).background(Paper),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(28.dp)) {
                    Icon(Icons.Rounded.LocationOn, null, tint = Olive, modifier = Modifier.size(48.dp))
                    Text("Try another nearby ZIP", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
                    Text("Only public store locations with usable coordinates appear here.", color = SoftInk, textAlign = TextAlign.Center)
                }
            }
        } else {
            MapLibre.getInstance(context)
            val markerIcon = remember {
                val size = 72
                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                // Pin stem + head so markers stay visible on a light colored basemap
                paint.color = android.graphics.Color.parseColor("#3D4F48")
                canvas.drawCircle(size / 2f, size / 2f + 18f, 5f, paint)
                paint.color = android.graphics.Color.parseColor("#60786F")
                canvas.drawCircle(size / 2f, size / 2f - 6f, 18f, paint)
                paint.color = android.graphics.Color.WHITE
                canvas.drawCircle(size / 2f, size / 2f - 6f, 7f, paint)
                org.maplibre.android.annotations.IconFactory.getInstance(context).fromBitmap(bitmap)
            }
            var mapRef by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
            var styleReady by remember { mutableStateOf(false) }
            val mapView = remember {
                MapView(context).apply {
                    onCreate(null)
                    getMapAsync { map ->
                        mapRef = map
                        map.uiSettings.isAttributionEnabled = false
                        map.setStyle(Style.Builder().fromUri(MAP_STYLE_URI)) {
                            styleReady = true
                        }
                    }
                }
            }
            DisposableEffect(mapView) {
                mapView.onStart(); mapView.onResume()
                onDispose {
                    styleReady = false
                    mapRef = null
                    mapView.onPause(); mapView.onStop(); mapView.onDestroy()
                }
            }
            LaunchedEffect(styleReady, places.map { it.id }.joinToString(), likedIds, centerLatitude, centerLongitude, selectedId) {
                val map = mapRef ?: return@LaunchedEffect
                if (!styleReady) return@LaunchedEffect
                map.clear()
                places.forEach { place ->
                    if (place.latitude == 0.0 && place.longitude == 0.0) return@forEach
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(place.latitude, place.longitude))
                            .icon(markerIcon)
                            .title((if (place.id in likedIds) "♥ " else "") + place.name)
                            .snippet("${place.match}% · ${place.distanceMiles} mi")
                    )
                }
                map.setOnMarkerClickListener { marker ->
                    places.minByOrNull {
                        kotlin.math.abs(it.latitude - marker.position.latitude) +
                            kotlin.math.abs(it.longitude - marker.position.longitude)
                    }?.let { selectedId = it.id }
                    true
                }
                val focus = places.firstOrNull { it.id == selectedId } ?: places.firstOrNull()
                val targetLat = focus?.latitude ?: centerLatitude
                val targetLon = focus?.longitude ?: centerLongitude
                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder().target(LatLng(targetLat, targetLon)).zoom(12.2).build()
                    ),
                    700
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            ) {
                AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                Text(
                    "© OpenStreetMap · OpenFreeMap",
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = .88f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
            selected?.let { place ->
                val retailer = RetailerDirectory.retailers.firstOrNull { it.id.equals(place.retailerId, true) }
                    ?: RetailerDirectory.retailers.firstOrNull {
                        place.name.contains(it.name.substringBefore(" "), ignoreCase = true) ||
                            it.name.contains(place.name.substringBefore(" "), ignoreCase = true)
                    }
                val suggestedPieces = (
                    place.suggestedPieces.ifEmpty { retailer?.notableFinds.orEmpty() }
                        .ifEmpty { place.features }
                    ).distinct().take(4)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Paper, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .border(1.dp, Hairline, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .background(Hairline, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.height(10.dp))
                    // Wearability-first card: title, match, reason, suggested pieces — then distance / actions.
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(place.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Row(
                                Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${place.match}% match",
                                    color = Paper,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .background(Olive, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                                Text("${place.distanceMiles} mi", color = SoftInk, style = MaterialTheme.typography.labelMedium)
                                if (place.id in likedIds) Icon(Icons.Rounded.Favorite, "Saved", tint = Olive, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Text(
                        place.reason,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (suggestedPieces.isNotEmpty()) {
                        Text(
                            "Suggested pieces",
                            modifier = Modifier.padding(top = 8.dp),
                            color = SoftInk,
                            style = MaterialTheme.typography.labelSmall
                        )
                        LazyRow(
                            Modifier.padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(suggestedPieces) { piece ->
                                Text(
                                    piece,
                                    color = Ink,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier
                                        .background(OliveLight, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    if (stepFree) Text(
                        when (place.accessibility) {
                            "yes" -> "OpenStreetMap reports step-free access. Confirm it with the store."
                            "no", "limited" -> "OpenStreetMap reports limited step-free access. Call before you go."
                            else -> "Step-free access is not recorded for this store. Call before you go."
                        },
                        modifier = Modifier.padding(top = 6.dp),
                        color = SoftInk,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (place.id !in likedIds) {
                            TextButton(onClick = { onLike(place) }) {
                                Icon(Icons.Rounded.FavoriteBorder, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Save")
                            }
                        }
                        TextButton(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(place.directionsUrl))) }
                        ) {
                            Text("Directions")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreDirectoryList(
    preferences: WearabilityPreferences,
    onBack: () -> Unit,
    embedded: Boolean = false
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var channel by rememberSaveable { mutableStateOf("All") }
    val context = LocalContext.current
    val preferenceMatched = RetailerDirectory.retailers.filter { RetailerDirectory.matchesPreferences(it, preferences) }
    val base = if (preferenceMatched.isEmpty()) RetailerDirectory.retailers else preferenceMatched
    val stores = base.filter { retailer ->
        (channel == "All" || (channel == "Online" && RetailerDirectory.isOnline(retailer)) || (channel == "In person" && retailer.region.contains("store", true))) &&
            (filter == null || filter in retailer.features) &&
            (query.isBlank() || retailer.name.contains(query, true) || retailer.focus.contains(query, true) ||
                retailer.features.any { it.contains(query, true) } || retailer.notableFinds.any { it.contains(query, true) })
    }.sortedWith(
        compareByDescending<com.vizeno.loomie.model.Retailer> { retailer ->
            val wanted = RetailerDirectory.wantedFeatures(preferences)
            retailer.features.count { it in wanted }
        }.thenBy { if (RetailerDirectory.isOnline(it)) 0 else 1 }
    )

    // Header fixed + LazyColumn(weight) — parent AnimatedContent must also use weight(1f).
    Column(Modifier.fillMaxSize().background(Parchment)) {
        if (!embedded) {
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to wardrobe") }
                Column(Modifier.weight(1f)) {
                    Text("Store directory", style = MaterialTheme.typography.headlineMedium)
                    Text("${stores.size} matches", style = MaterialTheme.typography.labelLarge, color = SoftInk)
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Online", "In person").forEach { value ->
                    CompactChip(value, channel == value) { channel = value }
                }
            }
            Spacer(Modifier.height(8.dp))
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search stores, features or pieces") },
                leadingIcon = { Icon(Icons.Rounded.Search, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(2.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Paper, unfocusedContainerColor = Paper, focusedIndicatorColor = Olive, unfocusedIndicatorColor = Hairline)
            )
            Spacer(Modifier.height(6.dp))
            SimpleDropdown(
                label = "Feature filter",
                options = listOf("All features") + RetailerDirectory.filters,
                selected = filter ?: "All features",
                onSelect = { chosen -> filter = chosen.takeUnless { it == "All features" } }
            )
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${stores.size} ${if (stores.size == 1) "match" else "matches"}", style = MaterialTheme.typography.labelLarge, color = SoftInk)
                Spacer(Modifier.weight(1f))
                Text("Scroll for all retailers", style = MaterialTheme.typography.bodySmall, color = SoftInk)
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stores, key = { it.id }) { retailer ->
                RetailerCard(retailer) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(retailer.shopUrl)))
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun RetailerCard(retailer: Retailer, onOpen: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(2.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(retailer.color, CircleShape), contentAlignment = Alignment.Center) {
                Text(retailer.name.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(retailer.name, style = MaterialTheme.typography.titleLarge)
                Text(retailer.focus, style = MaterialTheme.typography.bodyMedium, color = SoftInk)
            }
            Text(
                when {
                    RetailerDirectory.isOnline(retailer) && retailer.region.contains("store", true) -> "Online + stores"
                    RetailerDirectory.isOnline(retailer) -> "Online"
                    else -> "In person"
                },
                style = MaterialTheme.typography.labelLarge, color = Olive
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(retailer.description, style = MaterialTheme.typography.bodyMedium, color = SoftInk)
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            retailer.features.take(4).forEach { feature ->
                Text(feature, style = MaterialTheme.typography.labelSmall, color = Olive, modifier = Modifier.background(OliveLight, RoundedCornerShape(2.dp)).padding(horizontal = 8.dp, vertical = 5.dp))
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Hairline)
        retailer.notableFinds.forEach { find ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(5.dp).background(Brass, CircleShape))
                Spacer(Modifier.width(9.dp))
                Text(find, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(retailer.region, style = MaterialTheme.typography.bodySmall, color = SoftInk, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onOpen, shape = RoundedCornerShape(2.dp)) {
                Text("Visit store")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun CompactChip(label: String, selected: Boolean, onDark: Boolean = false, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(2.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if (onDark) Color.White else Ink,
            selectedLabelColor = if (onDark) Ink else Color.White,
            containerColor = if (onDark) Color.White.copy(alpha = .12f) else Color.Transparent,
            labelColor = if (onDark) Color.White.copy(alpha = .92f) else Ink
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (onDark) Color.White.copy(alpha = .42f) else Hairline,
            selectedBorderColor = if (onDark) Color.White else Ink
        )
    )
}

@Composable
private fun GarmentCard(garment: Garment, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier.clickable(role = Role.Button, onClick = onClick)) {
        Box(Modifier.fillMaxWidth().aspectRatio(.86f).clip(RoundedCornerShape(topEnd = 22.dp, bottomStart = 22.dp)).background(Brush.linearGradient(listOf(garment.color.copy(alpha = .18f), Paper))).border(1.dp, garment.color.copy(alpha = .16f), RoundedCornerShape(topEnd = 22.dp, bottomStart = 22.dp)), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(alpha = .38f), size.minDimension * .42f, Offset(size.width * .82f, size.height * .12f), style = Stroke(1.dp.toPx()))
                drawLine(garment.color.copy(alpha = .16f), Offset(0f, size.height * .78f), Offset(size.width, size.height * .52f), 1.dp.toPx())
            }
            GarmentProductImage(garment, Modifier.fillMaxSize())
            Box(Modifier.align(Alignment.TopEnd).padding(9.dp).background(Paper.copy(alpha = .9f), RoundedCornerShape(10.dp)).padding(horizontal = 7.dp, vertical = 4.dp)) {
                Text(if (garment.wearability >= 80) "VERIFIED" else "REVIEW", style = MaterialTheme.typography.labelSmall, color = if (garment.wearability >= 80) Olive else Clay)
            }
            Row(Modifier.align(Alignment.BottomStart).padding(9.dp).background(Ink.copy(alpha = .82f), RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(garment.wearability.toString(), color = Color.White, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.width(4.dp)); Text("match", color = Color.White.copy(alpha = .64f), style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(garment.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(garment.subtitle.substringBefore(" · "), style = MaterialTheme.typography.bodyMedium, color = SoftInk, maxLines = 1)
    }
}

// GarmentProductImage lives in GarmentImagery.kt (shared with Fit carousel).

@Composable
private fun GarmentArt(garment: Garment, modifier: Modifier = Modifier) {
    Canvas(modifier.semantics { contentDescription = garment.name }) {
        val w = size.width
        val h = size.height
        val c = garment.color
        when (garment.category) {
            GarmentCategory.TOP -> {
                val top = Path().apply {
                    moveTo(w * .30f, h * .20f); lineTo(w * .10f, h * .35f); lineTo(w * .23f, h * .56f)
                    lineTo(w * .27f, h * .88f); lineTo(w * .73f, h * .88f); lineTo(w * .77f, h * .56f)
                    lineTo(w * .90f, h * .35f); lineTo(w * .70f, h * .20f); lineTo(w * .59f, h * .15f)
                    quadraticTo(w * .50f, h * .28f, w * .41f, h * .15f); close()
                }
                drawPath(top, c)
                when (garment.id) {
                    "g1" -> {
                        repeat(5) { index ->
                            val x = w * (.34f + index * .08f)
                            drawLine(Paper.copy(alpha = .36f), androidx.compose.ui.geometry.Offset(x, h * .35f), androidx.compose.ui.geometry.Offset(x, h * .80f), 1.dp.toPx())
                        }
                        drawLine(Paper.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(w * .28f, h * .79f), androidx.compose.ui.geometry.Offset(w * .72f, h * .79f), 1.5.dp.toPx())
                    }
                    "g7" -> {
                        drawLine(Paper.copy(alpha = .8f), androidx.compose.ui.geometry.Offset(w * .50f, h * .28f), androidx.compose.ui.geometry.Offset(w * .50f, h * .84f), 2.dp.toPx())
                        repeat(4) { index -> drawCircle(Paper, 2.dp.toPx(), androidx.compose.ui.geometry.Offset(w * .50f, h * (.40f + index * .11f))) }
                    }
                    "g8" -> {
                        drawLine(Paper.copy(alpha = .72f), androidx.compose.ui.geometry.Offset(w * .38f, h * .26f), androidx.compose.ui.geometry.Offset(w * .66f, h * .70f), 2.dp.toPx())
                        drawLine(Paper.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(w * .66f, h * .70f), androidx.compose.ui.geometry.Offset(w * .78f, h * .74f), 2.dp.toPx())
                    }
                }
            }
            GarmentCategory.OUTERWEAR -> {
                val jacket = Path().apply {
                    moveTo(w * .32f, h * .17f); lineTo(w * .16f, h * .26f); lineTo(w * .07f, h * .68f)
                    lineTo(w * .19f, h * .73f); lineTo(w * .29f, h * .49f); lineTo(w * .27f, h * .90f)
                    lineTo(w * .73f, h * .90f); lineTo(w * .71f, h * .49f); lineTo(w * .81f, h * .73f)
                    lineTo(w * .93f, h * .68f); lineTo(w * .84f, h * .26f); lineTo(w * .68f, h * .17f)
                    lineTo(w * .58f, h * .14f); quadraticTo(w * .50f, h * .27f, w * .42f, h * .14f); close()
                }
                drawPath(jacket, c)
                drawLine(Paper.copy(alpha = .75f), androidx.compose.ui.geometry.Offset(w * .50f, h * .28f), androidx.compose.ui.geometry.Offset(w * .50f, h * .86f), 1.5.dp.toPx())
                when (garment.id) {
                    "g2" -> repeat(3) { index ->
                        drawRoundRect(Paper.copy(alpha = .72f), androidx.compose.ui.geometry.Offset(w * .48f, h * (.38f + index * .15f)), androidx.compose.ui.geometry.Size(w * .04f, h * .055f), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                    }
                    "g6" -> {
                        drawLine(Paper.copy(alpha = .58f), androidx.compose.ui.geometry.Offset(w * .40f, h * .23f), androidx.compose.ui.geometry.Offset(w * .50f, h * .45f), 2.dp.toPx())
                        drawLine(Paper.copy(alpha = .58f), androidx.compose.ui.geometry.Offset(w * .60f, h * .23f), androidx.compose.ui.geometry.Offset(w * .50f, h * .45f), 2.dp.toPx())
                        repeat(2) { index -> drawCircle(Paper.copy(alpha = .85f), 2.5.dp.toPx(), androidx.compose.ui.geometry.Offset(w * .47f, h * (.56f + index * .10f))) }
                    }
                    "g12" -> {
                        drawRoundRect(Paper.copy(alpha = .18f), androidx.compose.ui.geometry.Offset(w * .32f, h * .56f), androidx.compose.ui.geometry.Size(w * .13f, h * .14f))
                        drawRoundRect(Paper.copy(alpha = .18f), androidx.compose.ui.geometry.Offset(w * .55f, h * .56f), androidx.compose.ui.geometry.Size(w * .13f, h * .14f))
                        repeat(4) { index -> drawCircle(Paper.copy(alpha = .8f), 2.dp.toPx(), androidx.compose.ui.geometry.Offset(w * .50f, h * (.38f + index * .11f))) }
                    }
                }
            }
            GarmentCategory.BOTTOM -> {
                val p = Path().apply {
                    moveTo(w * .25f, h * .13f); lineTo(w * .75f, h * .13f); lineTo(w * .85f, h * .9f)
                    lineTo(w * .56f, h * .9f); lineTo(w * .5f, h * .44f); lineTo(w * .44f, h * .9f)
                    lineTo(w * .15f, h * .9f); close()
                }; drawPath(p, c)
                drawLine(Paper.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(w * .26f, h * .21f), androidx.compose.ui.geometry.Offset(w * .74f, h * .21f), 2.dp.toPx())
                drawLine(Paper.copy(alpha = .24f), androidx.compose.ui.geometry.Offset(w * .42f, h * .23f), androidx.compose.ui.geometry.Offset(w * .39f, h * .84f), 1.dp.toPx())
                drawLine(Paper.copy(alpha = .24f), androidx.compose.ui.geometry.Offset(w * .58f, h * .23f), androidx.compose.ui.geometry.Offset(w * .61f, h * .84f), 1.dp.toPx())
                if (garment.id == "g9") repeat(7) { index ->
                    val x = w * (.29f + index * .07f)
                    drawLine(Paper.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(x, h * .14f), androidx.compose.ui.geometry.Offset(x, h * .21f), 1.dp.toPx())
                }
            }
            GarmentCategory.SHOES -> {
                val shoe = Path().apply {
                    moveTo(w * .14f, h * .62f); quadraticTo(w * .20f, h * .45f, w * .38f, h * .45f)
                    lineTo(w * .55f, h * .55f); quadraticTo(w * .69f, h * .62f, w * .86f, h * .64f)
                    quadraticTo(w * .91f, h * .67f, w * .86f, h * .73f); lineTo(w * .22f, h * .73f)
                    quadraticTo(w * .13f, h * .72f, w * .14f, h * .62f); close()
                }
                drawPath(shoe, c)
                drawLine(Paper.copy(alpha = .68f), androidx.compose.ui.geometry.Offset(w * .20f, h * .70f), androidx.compose.ui.geometry.Offset(w * .85f, h * .70f), 2.dp.toPx())
                if (garment.id == "g10") {
                    drawLine(Paper.copy(alpha = .75f), androidx.compose.ui.geometry.Offset(w * .43f, h * .49f), androidx.compose.ui.geometry.Offset(w * .54f, h * .64f), 2.dp.toPx())
                    drawCircle(Paper.copy(alpha = .75f), 3.dp.toPx(), androidx.compose.ui.geometry.Offset(w * .43f, h * .48f), style = Stroke(1.5.dp.toPx()))
                } else {
                    drawLine(Paper.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(w * .31f, h * .52f), androidx.compose.ui.geometry.Offset(w * .57f, h * .61f), 2.dp.toPx())
                }
            }
            GarmentCategory.ACCESSORY -> {
                if (garment.id == "g11") {
                    val scarf = Path().apply {
                        moveTo(w * .50f, h * .18f); lineTo(w * .82f, h * .50f); lineTo(w * .50f, h * .82f); lineTo(w * .18f, h * .50f); close()
                    }
                    drawPath(scarf, c)
                    drawLine(Paper.copy(alpha = .35f), androidx.compose.ui.geometry.Offset(w * .31f, h * .31f), androidx.compose.ui.geometry.Offset(w * .69f, h * .69f), 1.5.dp.toPx())
                    drawLine(Paper.copy(alpha = .35f), androidx.compose.ui.geometry.Offset(w * .69f, h * .31f), androidx.compose.ui.geometry.Offset(w * .31f, h * .69f), 1.5.dp.toPx())
                } else {
                    drawCircle(c, radius = w * .27f, style = Stroke(7.dp.toPx()))
                    drawCircle(c, radius = w * .055f, center = androidx.compose.ui.geometry.Offset(w * .5f, h * .68f))
                    drawCircle(Paper.copy(alpha = .65f), radius = w * .018f, center = androidx.compose.ui.geometry.Offset(w * .50f, h * .15f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GarmentSheet(
    garment: Garment,
    preferences: WearabilityPreferences,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var similar by remember(garment.id) { mutableStateOf<List<com.vizeno.loomie.integration.ProductCard>>(emptyList()) }
    var similarLoading by remember(garment.id) { mutableStateOf(true) }
    var similarError by remember(garment.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(garment.id, preferences) {
        similarLoading = true
        similarError = null
        val priceLimit = LocalProfileStore.priceLimit(context)
        val query = buildString {
            append(garment.name)
            append(' ')
            append(garment.category.label.lowercase())
            if (garment.category == GarmentCategory.ACCESSORY || garment.category == GarmentCategory.SHOES) {
                append(" accessory product")
            } else {
                append(" clothing product")
            }
        }
        runCatching {
            when (garment.category) {
                GarmentCategory.SHOES -> LookWeaveGateway.discoverAccessories(
                    org.json.JSONObject().put("subcategory", "shoes").put("easyFootwear", preferences.easyFootwear),
                    "shoes"
                ).products
                GarmentCategory.ACCESSORY -> {
                    val subcategory = when {
                        garment.name.contains("glass", true) || garment.name.contains("frame", true) -> "glasses"
                        garment.name.contains("watch", true) -> "watches"
                        garment.name.contains("scarf", true) -> "scarf"
                        garment.name.contains("hat", true) || garment.name.contains("cap", true) -> "hat"
                        garment.name.contains("jewel", true) || garment.name.contains("pendant", true) ||
                            garment.name.contains("necklace", true) || garment.name.contains("earring", true) -> "jewellery"
                        else -> "jewellery"
                    }
                    LookWeaveGateway.discoverAccessories(
                        org.json.JSONObject().put("subcategory", subcategory),
                        subcategory
                    ).products
                }
                else -> LookWeaveGateway.discoverProducts(preferences, query = query, priceLimit = priceLimit)
            }
        }.onSuccess { similar = it }
            .onFailure { similarError = it.message ?: "Similar products unavailable" }
        similarLoading = false
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Paper, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 34.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(Modifier.size(120.dp).clip(RoundedCornerShape(16.dp)).background(Parchment), contentAlignment = Alignment.Center) { GarmentProductImage(garment, Modifier.fillMaxSize()) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(garment.category.label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Brass)
                    Text(garment.name, style = MaterialTheme.typography.headlineMedium)
                    Text(garment.subtitle, color = SoftInk)
                    Spacer(Modifier.height(8.dp))
                    Text(if (garment.wearability >= 80) "Ready for your priorities" else "One conflict to review", style = MaterialTheme.typography.titleMedium, color = if (garment.wearability >= 80) Olive else Clay)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(garment.note, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            EvidenceRow("Closure", garment.closure.value, garment.closure.source.label, garment.closure.confidence)
            EvidenceRow("Fabric", garment.fabric.value, garment.fabric.source.label, garment.fabric.confidence)
            EvidenceRow("Color", garment.colorName, "User confirmed", 100)
            Spacer(Modifier.height(18.dp))
            Text("WORKS WITH", style = MaterialTheme.typography.labelSmall, color = Brass)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                if (garment.oneHandFriendly) StatusLabel("One-hand use")
                if (garment.seatedComfort) StatusLabel("Seated comfort")
                if (garment.lowSensoryLoad) StatusLabel("Low sensory load")
                if (garment.deviceClearance) StatusLabel("Device clearance")
            }
            Spacer(Modifier.height(22.dp))
            Text("SIMILAR PRODUCTS", style = MaterialTheme.typography.labelSmall, color = Brass)
            Text("Live discover results — not placeholders.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            when {
                similarLoading -> LinearProgressIndicator(Modifier.fillMaxWidth(), color = Olive, trackColor = OliveLight)
                similarError != null -> Text(similarError!!, color = Clay, style = MaterialTheme.typography.bodySmall)
                similar.isEmpty() -> Text("No live similar products found right now. Try Adaptive store directory.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    similar.take(4).forEach { product ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(OliveLight)
                                .clickable(enabled = product.productUrl.startsWith("http")) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(product.productUrl)))
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(product.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(
                                    listOfNotNull(product.retailer, product.price).joinToString(" · "),
                                    color = Olive,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, tint = Olive, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(2.dp)) { Text("Style this piece") }
        }
    }
}

@Composable
private fun EvidenceRow(label: String, value: String, source: String, confidence: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(label, color = SoftInk, style = MaterialTheme.typography.bodyMedium); Text(value, style = MaterialTheme.typography.titleMedium) }
        Column(horizontalAlignment = Alignment.End) { Text(source, style = MaterialTheme.typography.labelSmall, color = Olive); Text("$confidence% confidence", style = MaterialTheme.typography.labelSmall, color = SoftInk) }
    }
    HorizontalDivider(color = Hairline)
}

@Composable
private fun PlanScreen(
    preferences: WearabilityPreferences,
    initialOccasion: String,
    onPreferences: (WearabilityPreferences) -> Unit,
    onCreate: (String) -> Unit
) {
    val context = LocalContext.current
    var occasion by rememberSaveable { mutableStateOf(initialOccasion) }
    LaunchedEffect(initialOccasion) { occasion = initialOccasion }
    var savedOccasions by remember { mutableStateOf(LocalProfileStore.savedOccasions(context)) }
    val occasions = (listOf("Gallery opening", "Work presentation", "Dinner", "Travel day", "Whole day") + savedOccasions)
        .distinctBy(String::lowercase)
    val activePriorities = listOf(
        preferences.oneHandClosure, preferences.seatedComfort, preferences.lowSensoryLoad,
        preferences.deviceClearance, preferences.easyFootwear, preferences.avoidWool,
        preferences.animalFree, preferences.limitedOverheadReach, preferences.gripStrength,
        preferences.seamPressureSensitivity, preferences.easyRemoval, preferences.mobilityAidCompatibility
    ).count { it } + preferences.userFilters.count { it.enabled } +
        preferences.customRules.count { it.enabled && it.confirmed }
    Column(Modifier.fillMaxSize()) {
        PageHeader("Plan a look", "Whole-look solver", Icons.Rounded.Tune)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(occasion, style = MaterialTheme.typography.headlineSmall)
            Text(
                "$activePriorities active priorities · ZIP ${LocalProfileStore.postalCode(context).ifBlank { "—" }}",
                color = SoftInk,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(14.dp))
            SectionLabel("Occasion", Icons.Rounded.Place)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                occasions.forEach { CompactChip(it, occasion == it) { occasion = it } }
            }
            Spacer(Modifier.height(10.dp))
            TextField(
                value = occasion,
                onValueChange = { occasion = it.take(80) },
                label = { Text("Custom occasion") },
                placeholder = { Text("e.g. Outdoor wedding reception") },
                supportingText = { Text("Use any occasion, then save it for this profile.") },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            LocalProfileStore.saveOccasion(context, occasion)
                            savedOccasions = LocalProfileStore.savedOccasions(context)
                        },
                        enabled = occasion.isNotBlank()
                    ) { Text("Save") }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            PreferenceHubSection(context = context, profileEpoch = 0, preferences = preferences, onPreferences = onPreferences)
            Spacer(Modifier.height(12.dp))
            AgentRuleChoices(preferences, onPreferences)
            Spacer(Modifier.height(16.dp))
        }
        Button(
            onClick = {
                val clean = occasion.trim()
                if (clean.isNotBlank()) {
                    LocalProfileStore.saveOccasion(context, clean)
                    onCreate(clean)
                }
            },
            enabled = occasion.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
            shape = RoundedCornerShape(2.dp)
        ) {
            Text("Weave with $activePriorities priorities")
            Spacer(Modifier.width(10.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
        }
    }
}

@Composable
private fun AgentRuleChoices(preferences: WearabilityPreferences, onPreferences: (WearabilityPreferences) -> Unit) {
    SectionLabel("From your agent", Icons.Rounded.AutoAwesome)
    Text("A confirmed custom rule is its own choice. Turning it on or off leaves the switches above as they are.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
    val agentRules = preferences.customRules.filter { it.confirmed }
    if (agentRules.isEmpty()) {
        Text("Ask the agent for a rule, then confirm it. It will show up in this list.", color = SoftInk, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 10.dp))
    }
    agentRules.forEach { rule ->
        Row(Modifier.fillMaxWidth().clickable {
            onPreferences(preferences.copy(customRules = preferences.customRules.map { if (it.id == rule.id) it.copy(enabled = !it.enabled) else it }))
        }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = rule.enabled,
                onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = Olive, unselectedColor = SoftInk)
            )
            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                Text(rule.label, style = MaterialTheme.typography.titleMedium)
                Text(if (rule.enabled) "On · applied with your other filters" else "Off · your other filters still apply", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
            }
        }
        HorizontalDivider(color = Hairline)
    }
}

@Composable
private fun PreferenceToggle(title: String, detail: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChecked(!checked) }.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = SoftInk)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(checkedThumbColor = Paper, checkedTrackColor = Olive, uncheckedThumbColor = SoftInk, uncheckedTrackColor = Hairline)
        )
    }
    HorizontalDivider(color = Hairline)
}

@Composable
private fun OutfitResultScreen(plan: OutfitPlan, onBack: () -> Unit, onTryOn: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Parchment)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            Wordmark()
            Spacer(Modifier.weight(1f))
            if (plan.score > 0) {
                Text("${plan.score}", color = Olive, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 12.dp))
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 26.dp)) {
            item {
                Box(Modifier.fillMaxWidth().height(430.dp).padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(topEnd = 32.dp, bottomStart = 32.dp))) {
                    Image(painterResource(R.drawable.wardrobe_editorial), "Woven look for ${plan.occasion}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Ink.copy(alpha = .08f), Ink.copy(alpha = .06f), Ink.copy(alpha = .88f)))))
                    Column(Modifier.align(Alignment.TopStart).padding(18.dp)) {
                        StatusLabel(if (plan.unresolved.isNotEmpty()) "NEEDS YOUR REVIEW" else if (plan.replaced != null) "1 CHANGE · ${plan.preservedCount} KEPT" else "NO CHANGE · ${plan.preservedCount} KEPT")
                    }
                    Column(Modifier.align(Alignment.BottomStart).padding(20.dp).padding(end = 104.dp)) {
                        Text(plan.occasion.uppercase(Locale.US), color = Brass, style = MaterialTheme.typography.labelSmall)
                        Text(if (plan.replaced != null) plan.replacement?.name ?: plan.occasion else plan.occasion, color = Color.White, style = MaterialTheme.typography.headlineLarge)
                        Text(if (plan.unresolved.isEmpty()) "All priorities met · ${plan.resolved.size} pieces" else "Incomplete look · ${plan.unresolved.size} gaps", color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.bodyLarge)
                    }
                    FilledIconButton(
                        onClick = onTryOn,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).size(76.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Paper, contentColor = Ink)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(20.dp))
                            Text("Preview\non you", textAlign = TextAlign.Center, fontSize = 10.sp, lineHeight = 11.sp)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(18.dp))
                SectionTitle("The weave", "Explained", Icons.Rounded.AutoAwesome)
                Column(Modifier.padding(horizontal = 20.dp)) {
                    DecisionTrace(plan)
                    Spacer(Modifier.height(18.dp))
                    Text(plan.explanation, style = MaterialTheme.typography.bodyLarge)
                    if (plan.triggeredRules.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text("Rules that caused the change", style = MaterialTheme.typography.titleMedium, color = Olive)
                        plan.triggeredRules.forEach { rule -> Text("• $rule", style = MaterialTheme.typography.bodyMedium, color = SoftInk) }
                    }
                    if (plan.unverifiable.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text("Evidence to verify", style = MaterialTheme.typography.titleMedium, color = Clay)
                        plan.unverifiable.take(4).forEach { item -> Text("• $item", style = MaterialTheme.typography.bodyMedium, color = SoftInk) }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth().background(OliveLight, RoundedCornerShape(2.dp)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Olive)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("${plan.preservedCount} pieces preserved", style = MaterialTheme.typography.titleMedium)
                            Text(
                                when {
                                    plan.unresolved.isNotEmpty() -> "Some slots still need a compatible garment"
                                    plan.replaced != null -> "Only the conflicting layer changed"
                                    else -> "Nothing needed to change"
                                },
                                color = SoftInk, style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(20.dp))
                SectionTitle("Evidence used", icon = Icons.Rounded.CheckCircle)
                Column(Modifier.padding(horizontal = 20.dp)) {
                    plan.replaced?.let { EvidenceRow("Removed piece", "${it.name} · ${it.closure.value}", it.closure.source.label, it.closure.confidence) }
                    plan.replacement?.let { EvidenceRow("Replacement", "${it.name} · ${it.closure.value}", it.closure.source.label, it.closure.confidence) }
                    if (plan.triggeredRules.isNotEmpty()) EvidenceRow("Active rules", plan.triggeredRules.joinToString(", "), "Your confirmed preferences", 100)
                    if (plan.unverifiable.isNotEmpty()) EvidenceRow("Still unknown", plan.unverifiable.first(), "Needs review", 0)
                    if (plan.resolved.isEmpty()) {
                        EvidenceRow("Wardrobe coverage", "No complete owned look was available", "Your wardrobe", 0)
                        Text(
                            "Add or import garments in Wardrobe, or ask Loomie to browse for the missing category.",
                            color = SoftInk,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    } else {
                        plan.resolved.take(5).forEach { garment ->
                            val known = garment.closure.confidence > 0 && !garment.closure.value.equals("Unknown", true)
                            EvidenceRow(
                                garment.name,
                                "${garment.closure.value} closure · ${garment.fabric.value}",
                                if (known) garment.closure.source.label else "Needs product evidence",
                                if (known) garment.closure.confidence else 0
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DecisionTrace(plan: OutfitPlan) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TraceStep("CHECKED", "${plan.original.size} pcs")
        TraceConnector()
        TraceStep("RULES", if (plan.triggeredRules.isEmpty()) "Clear" else "${plan.triggeredRules.size}")
        TraceConnector()
        TraceStep("CHANGE", if (plan.replaced == null) "None" else "1 layer")
    }
}

@Composable
private fun RowScope.TraceStep(label: String, value: String) {
    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(30.dp).border(1.5.dp, Olive, CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(8.dp).background(Olive, CircleShape))
        }
        Spacer(Modifier.height(7.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Brass, textAlign = TextAlign.Center)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RowScope.TraceConnector() {
    HorizontalDivider(Modifier.weight(.45f), thickness = 1.dp, color = Olive.copy(alpha = .45f))
}

@Composable
private fun TryOnScreen(selectedGarment: Garment?, onBack: () -> Unit, onSave: () -> Unit) {
    var reveal by rememberSaveable { mutableStateOf(false) }
    var processing by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var personUri by remember {
        mutableStateOf(
            LocalProfileStore.bodyUri(context)?.let { Uri.parse(it) }
        )
    }
    var garmentUri by remember(selectedGarment?.id) {
        mutableStateOf(
            selectedGarment?.let { garmentTryOnUri(context.packageName, it) }
                ?: selectedGarment?.let { Uri.parse("android.resource://com.vizeno.loomie/drawable/garment_${it.id}") }
        )
    }
    var previewCategory by remember(selectedGarment?.id) {
        mutableStateOf(
            when (selectedGarment?.category) {
                GarmentCategory.SHOES -> "shoes"
                GarmentCategory.OUTERWEAR -> "outer"
                GarmentCategory.BOTTOM -> "lower_body"
                GarmentCategory.TOP -> "upper_body"
                else -> "auto"
            }
        )
    }
    var preview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val personPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val saved = withContext(Dispatchers.IO) {
                LocalProfileStore.copyBodyToAppFiles(context, uri)
            }
            personUri = Uri.parse(saved ?: uri.toString())
            reveal = false
            preview = null
            error = null
        }
    }
    val garmentPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { garmentUri = uri; previewCategory = "auto"; reveal = false; preview = null; error = null }
    }

    // Prefer memory → disk → seeded assets when opening with a known garment.
    LaunchedEffect(personUri, selectedGarment?.id) {
        val body = personUri?.toString() ?: return@LaunchedEffect
        val garmentId = selectedGarment?.id ?: return@LaunchedEffect
        val hit = withContext(Dispatchers.IO) {
            TryOnCompositeCache.load(context, body, listOf(garmentId))
        } ?: return@LaunchedEffect
        preview = hit.bitmap
        reveal = true
        error = null
    }

    fun generatePreview() {
        if (processing) return
        if (personUri == null || garmentUri == null) {
            error = "Choose your photo and a garment image first."
            return
        }
        val garmentIds = listOfNotNull(selectedGarment?.id)
        if (garmentIds.isNotEmpty()) {
            val cached = TryOnCompositeCache.load(context, personUri?.toString(), garmentIds)
            if (cached != null) {
                preview = cached.bitmap
                reveal = true
                error = null
                return
            }
        }
        processing = true
        error = null
        scope.launch {
            val source = personUri
            val garment = garmentUri
            if (source != null && garment != null) {
                runCatching {
                    val task = LookWeaveGateway.createPreview(context.contentResolver, source, garment, previewCategory)
                    val bytes = LookWeaveGateway.download(task.imageUrl)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: error("The completed preview image could not be decoded")
                }.onSuccess { bitmap ->
                    preview = bitmap
                    reveal = true
                    if (garmentIds.isNotEmpty()) {
                        withContext(Dispatchers.IO) {
                            TryOnCompositeCache.put(context, source.toString(), garmentIds, bitmap)
                            val bodySource = LocalProfileStore.bodySource(context)
                            if (TryOnCompositeCache.isBuiltInPersonAlias(bodySource)) {
                                TryOnCompositeCache.put(
                                    context,
                                    TryOnCompositeCache.cacheKey(bodySource!!, garmentIds),
                                    bitmap
                                )
                            }
                        }
                    }
                }.onFailure { failure ->
                    error = failure.message ?: "The studio could not complete this preview."
                }
            }
            processing = false
        }
    }

    Box(Modifier.fillMaxSize().background(Ink)) {
        if (preview != null) {
            Image(preview!!.asImageBitmap(), "Virtual preview wearing the selected garment", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Image(painterResource(R.drawable.lookweave_editorial), "Virtual preview wearing the recommended outfit", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        if (!reveal) Box(Modifier.fillMaxSize().background(Ink.copy(alpha = .78f)))
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(10.dp).align(Alignment.TopCenter), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(alpha = .3f), CircleShape)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White) }
            Spacer(Modifier.width(10.dp)); Text("VIRTUAL STUDIO", color = Color.White, style = MaterialTheme.typography.labelLarge)
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(20.dp)) {
            if (!reveal) {
                Box(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(2.dp)).padding(20.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("See the complete look", style = MaterialTheme.typography.headlineMedium)
                        Text("Preview the selected garment on your photo, then compare the result with your original. Physical fit and comfort still need your judgment.", textAlign = TextAlign.Center, color = SoftInk)
                        selectedGarment?.let { Text("From your look: ${it.name}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp)) }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { personPicker.launch("image/*") }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(2.dp)) {
                                Icon(if (personUri == null) Icons.Rounded.PersonOutline else Icons.Rounded.Check, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp)); Text(if (personUri == null) "Your photo" else "Photo ready")
                            }
                            OutlinedButton(onClick = { garmentPicker.launch("image/*") }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(2.dp)) {
                                Icon(if (garmentUri == null) Icons.Rounded.Checkroom else Icons.Rounded.Check, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp)); Text(if (garmentUri == null) "Garment" else "Garment ready")
                            }
                        }
                        if (error != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(error!!, color = Clay, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(18.dp))
                        Button(onClick = { generatePreview() }, enabled = !processing && personUri != null && garmentUri != null, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(10.dp)) {
                            if (processing) { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White); Spacer(Modifier.width(10.dp)); Text("Preparing preview") }
                            else Text("Generate preview")
                        }
                    }
                }
            } else {
                Column(Modifier.background(Color.Black.copy(alpha = .34f), RoundedCornerShape(2.dp)).padding(14.dp)) {
                    StatusLabel("LOOK PREVIEW", dark = false)
                    Spacer(Modifier.height(8.dp))
                    Text(selectedGarment?.name ?: "Your selected garment", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text("Preview how this piece looks on you", color = Color.White.copy(alpha = .9f))
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(2.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Adjust") }
                        Button(onClick = {
                            scope.launch {
                                runCatching {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        val bitmap = requireNotNull(preview)
                                        context.openFileOutput("saved-look.png", android.content.Context.MODE_PRIVATE).use { stream ->
                                            check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream))
                                        }
                                    }
                                }.onSuccess { onSave() }.onFailure { error = "The look could not be saved. Please retry." }
                            }
                        }, enabled = preview != null, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Paper, contentColor = Ink)) { Text("Save look") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Save looks you like and refine them anytime.", color = Color.White.copy(alpha = .82f), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AgentScreen(
    dayContext: DayContext?,
    preferences: WearabilityPreferences,
    onPreferences: (WearabilityPreferences) -> Unit,
    onPlan: (OutfitPlan) -> Unit,
    seedMessage: String? = null,
    onSeedConsumed: () -> Unit = {},
    likedProductIds: Set<String> = emptySet(),
    likedPlaceIds: Set<String> = emptySet(),
    onNavigate: (AppDestination) -> Unit = {},
    onOpenDiscovery: () -> Unit = {},
    onOpenWovenLook: () -> Unit = {}
) {
    val messages = remember {
        mutableStateListOf(
            AgentMessage(false, "What would you like your clothes to do for you today? We can work on a look, talk through a difficult garment, or plan an outing below.")
        )
    }
    var input by rememberSaveable { mutableStateOf("") }
    var pendingDayNotes by remember { mutableStateOf(seedMessage) }
    LaunchedEffect(seedMessage) {
        if (!seedMessage.isNullOrBlank()) {
            pendingDayNotes = seedMessage
            messages += AgentMessage(false, "Day context loaded. Ask me about budget, events, or a planned outfit for that day.")
            onSeedConsumed()
        }
    }
    var awaitingReply by rememberSaveable { mutableStateOf(false) }
    var voiceState by rememberSaveable { mutableStateOf("ready") }
    var audioLevel by remember { mutableStateOf(0f) }
    var voiceRequest by remember { mutableIntStateOf(0) }
    var voiceSession by remember { mutableStateOf<ConversationSession?>(null) }
    var voiceTranscriptSeen by remember { mutableStateOf(false) }
    var pendingRule by remember { mutableStateOf<WearabilityRule?>(null) }
    var pendingDayDraft by remember { mutableStateOf<DayToolDraft?>(null) }
    var showLookInside by rememberSaveable { mutableStateOf(false) }
    val actionTrace = remember { mutableStateListOf<AgentAction>() }
    val resultCards = remember { mutableStateListOf<AgentUiCard>() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) voiceRequest++ else {
            voiceState = "error"
            messages += AgentMessage(false, "Microphone access is needed for a voice conversation. Text remains available below.")
        }
    }

    LaunchedEffect(voiceRequest) {
        if (voiceRequest == 0) return@LaunchedEffect
        voiceState = "connecting"
        runCatching {
            val credentials = LookWeaveGateway.createVoiceCredentials()
            ConversationClient.startSession(
                ConversationConfig(
                    conversationToken = credentials.token,
                    userId = "lookweave-user",
                    onModeChange = { mode -> scope.launch { voiceState = mode.name.lowercase() } },
                    onStatusChange = { status ->
                        scope.launch {
                            when (status.name) {
                                "CONNECTED" -> if (voiceState == "connecting") voiceState = "listening"
                                "ERROR" -> voiceState = "error"
                                "DISCONNECTED" -> voiceState = "ready"
                            }
                        }
                    },
                    onAudioLevelChanged = { level -> scope.launch { audioLevel = level.coerceIn(0f, 1f) } },
                    onUserTranscriptEvent = { transcript, _ ->
                        if (transcript.isNotBlank()) scope.launch {
                            voiceTranscriptSeen = true
                            messages += AgentMessage(true, transcript)
                            val isOutfitRequest = Regex("\\b(outfit|look|wear|dressed|interview|meeting|dinner|wedding)\\b", RegexOption.IGNORE_CASE).containsMatchIn(transcript)
                            if (isOutfitRequest) {
                                val plan = ConstraintSolver.plan(
                                    transcript.take(80), preferences,
                                    SamplePersonProfiles.wardrobeForActiveProfile(context),
                                    LocalProfileStore.genderLabel(context),
                                    preferProfileCapsule = true
                                )
                                onPlan(plan)
                                messages += AgentMessage(false, outfitAnswer(plan))
                                resultCards += AgentUiCard(
                                    id = "voice-look-${System.nanoTime()}",
                                    eyebrow = "FROM YOUR WARDROBE",
                                    title = if (plan.unresolved.isEmpty()) "Your look is ready" else "This look needs another piece",
                                    detail = outfitCardDetail(plan),
                                    actionLabel = "View the outfit",
                                    action = "look",
                                    afterMessage = messages.lastIndex
                                )
                                actionTrace += AgentAction("solve_outfit", "complete", "Checked the whole look", plan.explanation)
                            }
                            runCatching {
                                LookWeaveGateway.respond(AgentContextBuilder.build(
                                    context = context, message = transcript, preferences = preferences,
                                    dayContext = dayContext, dayContextNotes = pendingDayNotes,
                                    likedProductIds = likedProductIds, likedPlaceIds = likedPlaceIds
                                ))
                            }.onSuccess { response ->
                                actionTrace.addAll(response.executedActions)
                                if (!isOutfitRequest) messages += AgentMessage(false, cleanAgentDisplayText(response.message))
                                response.proposedToolCalls.filterNot { isOutfitRequest && it.name == "solve_outfit" }.forEach { call ->
                                    agentCardFor(call, context)?.let { resultCards += it.copy(afterMessage = messages.lastIndex) }
                                }
                            }
                        }
                    },
                    onAgentResponseEvent = { response, _ ->
                        if (response.isNotBlank()) scope.launch {
                            // ElevenLabs supplies voice audio. The app's verified
                            // agent supplies the visible answer and action cards.
                            if (!voiceTranscriptSeen) messages += AgentMessage(false, cleanAgentDisplayText(response))
                        }
                    },
                    onError = { _, message ->
                        scope.launch {
                            voiceState = "error"
                            messages += AgentMessage(false, message?.takeIf { it.isNotBlank() }
                                ?: "The voice session could not connect. Text remains available.")
                        }
                    }
                ),
                context
            )
        }.onSuccess { session ->
            voiceSession = session
        }.onFailure { error ->
            voiceState = "error"
            messages += AgentMessage(false, error.message ?: "The voice session could not connect. Text remains available.")
        }
    }

    DisposableEffect(Unit) {
        onDispose { voiceSession?.let { session -> scope.launch { session.endSession() } } }
    }

    fun fallbackReply(text: String) = when {
        text.contains("why", true) || text.contains("reason", true) -> "Your slate blazer uses small buttons. In your last confirmed trial, fastening took 42 seconds with two retries. I chose the olive jacket because its magnetic front satisfies your easy-closure preference while keeping the same cropped, tailored line. The other four pieces stay unchanged."
        text.contains("casual", true) -> "I can soften the look without losing its structure: keep the ivory knit and charcoal trouser, swap the jacket for your sand snap overshirt, and use the bone zip sneaker. All selected pieces satisfy today’s preferences."
        text.contains("shoe", true) -> "Your black soft loafer is feasible: step-in closure, flexible upper, and stable heel. The bone zip sneaker is the easier alternative if you expect more walking."
        else -> "I’ll preserve the silhouette and check every change against your confirmed preferences. For this look, the olive magnet jacket is the smallest useful adjustment."
    }

    val transcript = rememberLazyListState()

    // Tool/evidence cards are appended after the agent response arrives. Keep
    // the newest card in view instead of leaving the user scrolled above it.
    LaunchedEffect(messages.size, resultCards.size, pendingRule != null, pendingDayDraft != null, awaitingReply) {
        if (!showLookInside) {
            delay(120)
            val last = transcript.layoutInfo.totalItemsCount - 1
            if (last >= 0) transcript.animateScrollToItem(last)
        }
    }

    fun send(text: String) {
        if (text.isBlank() || awaitingReply) return
        messages += AgentMessage(true, text)
        input = ""
        awaitingReply = true
        // Only jump down when the user sends — voice replies must not steal scroll position.
        scope.launch {
            val index = transcript.layoutInfo.totalItemsCount - 1
            if (index >= 0) transcript.animateScrollToItem(index)
        }
        scope.launch {
            val styleFacts = runCatching {
                val profile = LookWeaveGateway.styleProfile()
                val face = profile.optJSONObject("face") ?: org.json.JSONObject()
                val colours = profile.optJSONObject("colours") ?: org.json.JSONObject()
                buildMap {
                    face.optString("faceShape").takeIf { it.isNotBlank() }?.let { put("faceShape", it) }
                    face.optString("skinTone").takeIf { it.isNotBlank() }?.let { put("skinTone", it) }
                    colours.keys().asSequence().forEach { key -> put("colour:$key", colours.optString(key)) }
                }
            }.getOrDefault(emptyMap())
            val request = AgentContextBuilder.build(
                context = context,
                message = text,
                preferences = preferences,
                dayContext = dayContext,
                dayContextNotes = pendingDayNotes,
                likedProductIds = likedProductIds,
                likedPlaceIds = likedPlaceIds,
                style = styleFacts,
                extraEvidence = emptyList()
            )
            val outcome = runCatching { LookWeaveGateway.respond(request) }
            val response = outcome.getOrNull()
            val firstNewCard = resultCards.size
            response?.executedActions?.let { actionTrace.addAll(it) }
            val requestedSolver = response?.proposedToolCalls?.any { it.name == "solve_outfit" || it.name == "suggest_outfit_swap" } == true || text.contains("outfit", true) || text.contains("look", true) || text.contains("swap", true) || text.contains("closure", true)
            var solvedPlan: OutfitPlan? = null
            if (requestedSolver) {
                val occasion = response?.proposedToolCalls?.firstOrNull { it.name == "solve_outfit" || it.name == "suggest_outfit_swap" }?.arguments?.get("occasion")?.ifBlank { null }
                    ?: text.trim().take(80).ifBlank { "Today" }
                val plan = ConstraintSolver.plan(
                    occasion,
                    preferences,
                    SamplePersonProfiles.wardrobeForActiveProfile(context),
                    LocalProfileStore.genderLabel(context),
                    preferProfileCapsule = true
                )
                solvedPlan = plan
                onPlan(plan)
                actionTrace += AgentAction("solve_outfit", "complete", "Checked the whole look", "${plan.replaced?.name?.let { "Changed $it" } ?: "No garment change needed"}; ${plan.preservedCount} pieces stayed unchanged.")
                resultCards += AgentUiCard(
                    id = "look-${System.currentTimeMillis()}",
                    eyebrow = "FROM YOUR WARDROBE",
                    title = if (plan.unresolved.isEmpty()) "Owned look ready" else "Owned look needs ${plan.unresolved.size} piece${if (plan.unresolved.size == 1) "" else "s"}",
                    detail = outfitCardDetail(plan),
                    actionLabel = "View evidence",
                    action = "look"
                )
            }
            val reply = solvedPlan?.let(::outfitAnswer) ?: response?.message ?: outcome.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                ?: "I couldn’t reach the conversation service. Please retry; your saved preferences are still available in Plan."
            messages += AgentMessage(false, cleanAgentDisplayText(reply))
            response?.proposedToolCalls.orEmpty().forEach { call ->
                agentCardFor(call, context)?.let { resultCards += it }
            }
            if (text.contains(Regex("weather|uv|pollen|air quality|temperature", RegexOption.IGNORE_CASE))) {
                resultCards += AgentUiCard(
                    id = "signals-${System.currentTimeMillis()}",
                    eyebrow = "LIVE CONTEXT",
                    title = "Check Day Signals",
                    detail = dayContext?.let { contextValue ->
                        contextValue.signals.take(3).joinToString(" · ") { "${it.label}: ${it.value}" }
                            .ifBlank { "Live context is ready for ${contextValue.location}." }
                    } ?: "Open weather, UV, air quality and pollen evidence before deciding.",
                    actionLabel = "Open signals",
                    action = "signals"
                )
            }
            val proposedRule = response?.proposedToolCalls?.firstOrNull { it.name == "draft_rule" || it.name == "create_rule" }
            val askedForRule = text.contains(Regex("rule|filter|remember|avoid|need|prefer", RegexOption.IGNORE_CASE))
            if (proposedRule != null || askedForRule) {
                val ruleText = proposedRule?.arguments?.get("ruleText")?.takeIf { it.isNotBlank() }
                    ?: proposedRule?.arguments?.get("query")?.takeIf { it.isNotBlank() }
                    ?: text
                runCatching { LookWeaveGateway.draftRule(ruleText) }.onSuccess { pendingRule = it }
            }
            pendingDayDraft = response?.proposedToolCalls
                ?.firstOrNull { it.name in DAY_CONFIRM_TOOLS }
                ?.let { DayToolDraft.from(it) }
            for (index in firstNewCard until resultCards.size) {
                resultCards[index] = resultCards[index].copy(afterMessage = messages.lastIndex)
            }
            awaitingReply = false
        }
    }

    val startVoice: () -> Unit = {
        if (voiceSession != null) {
            scope.launch {
                voiceSession?.endSession()
                voiceSession = null
                voiceState = "ready"
                audioLevel = 0f
            }
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            voiceRequest++
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        Unit
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        PageHeader("Loomie", "Your wool companion", Icons.Rounded.ChatBubbleOutline) {
            Box(Modifier.size(10.dp).background(if (voiceState == "error") Clay else Olive, CircleShape).semantics { contentDescription = "Loomie ${voiceState}" })
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).background(OliveLight, RoundedCornerShape(12.dp)).padding(4.dp)) {
            Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (!showLookInside) Paper else Color.Transparent).clickable { showLookInside = false }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("Conversation", style = MaterialTheme.typography.labelLarge)
            }
            Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (showLookInside) Paper else Color.Transparent).clickable { showLookInside = true }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.Visibility, null, modifier = Modifier.size(16.dp)); Text("Look inside", style = MaterialTheme.typography.labelLarge) }
            }
        }
        LazyColumn(
            state = transcript,
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showLookInside) {
                item { AgentTraceIntro() }
                if (actionTrace.isEmpty()) item { EmptyAgentTrace() }
                items(actionTrace) { action -> AgentActionCard(action) }
            } else {
                item {
                    val liveDialogue = messages.lastOrNull { !it.fromUser }?.text.orEmpty()
                    LiveVoiceAgentPanel(
                        state = voiceState,
                        audioLevel = audioLevel,
                        dialogue = liveDialogue,
                        onActivate = startVoice
                    )
                }
                item { OutingPlanPanel() }
                messages.forEachIndexed { messageIndex, message ->
                    item { ChatBubble(message) }
                    resultCards.filter { it.afterMessage == messageIndex }.forEach { card ->
                        item(key = card.id) {
                            AgentUiResultCard(card) {
                                when (card.action) {
                                    "look" -> onOpenWovenLook()
                                    "browse", "nearby" -> onOpenDiscovery()
                                    "wardrobe" -> onNavigate(AppDestination.WARDROBE)
                                    "day", "signals" -> onNavigate(AppDestination.DAY)
                                    "profile" -> onNavigate(AppDestination.PROFILE)
                                    "plan" -> onNavigate(AppDestination.PLAN)
                                }
                            }
                        }
                    }
                }
            pendingRule?.let { rule ->
                item {
                    Column(Modifier.fillMaxWidth().background(OliveLight).padding(14.dp)) {
                        Text("Rule draft", style = MaterialTheme.typography.labelLarge, color = Olive)
                        Text(rule.label, style = MaterialTheme.typography.titleMedium)
                        Text("Review this before it changes your future outfits.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launch { runCatching { LookWeaveGateway.createRule(rule) }.onSuccess { saved -> onPreferences(preferences.copy(customRules = preferences.customRules + saved)); pendingRule = null; messages += AgentMessage(false, "Saved. I will apply ${saved.label.lowercase()} as a confirmed rule.") } }
                            }) { Text("Confirm rule") }
                            OutlinedButton(onClick = { pendingRule = null }) { Text("Discard") }
                        }
                    }
                }
            }
            pendingDayDraft?.let { draft ->
                item {
                    Column(Modifier.fillMaxWidth().background(OliveLight).padding(14.dp)) {
                        Text("Day draft", style = MaterialTheme.typography.labelLarge, color = Olive)
                        Text(draft.title, style = MaterialTheme.typography.titleMedium)
                        Text(draft.detail, color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launch {
                                    val saved = confirmDayDraft(context, draft)
                                    pendingDayDraft = null
                                    messages += AgentMessage(false, saved)
                                    actionTrace += AgentAction(draft.tool, "complete", draft.title, saved)
                                }
                            }) { Text("Confirm") }
                            OutlinedButton(onClick = { pendingDayDraft = null }) { Text("Discard") }
                        }
                    }
                }
            }
            if (awaitingReply) item {
                Row(Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LoomieMascot(LoomieMood.Thinking, size = 42.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Loomie is weaving a response…", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (!showLookInside && messages.size == 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Why this swap?", "Make it more casual", "Are the shoes easy?")) { prompt -> CompactChip(prompt, false) { send(prompt) } }
                    }
                }
            }
            }
        }
        Row(Modifier.fillMaxWidth().background(Paper).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask about your look") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Parchment, unfocusedContainerColor = Parchment, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
            )
            IconButton(onClick = { send(input) }, enabled = input.isNotBlank() && !awaitingReply) {
                Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = if (input.isNotBlank() && !awaitingReply) Olive else Hairline)
            }
        }
    }
}

@Composable
private fun AgentTraceIntro() {
    Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Ink, Color(0xFF3D5A4D))), RoundedCornerShape(topEnd = 26.dp, bottomStart = 26.dp)).padding(18.dp)) {
        Text("LOOK INSIDE", color = Brass, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Text("A short record of what LookWeave checked and changed.", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Text("A clear view of what changed and why.", color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyAgentTrace() {
    Column(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(16.dp)).padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        LoomieMascot(LoomieMood.Idle, size = 64.dp)
        Text("Nothing checked yet", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
        Text("Ask LookWeave about a garment or rule and the useful steps will appear here.", color = SoftInk, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AgentActionCard(action: AgentAction) {
    val accent = when (action.status) { "complete" -> Olive; "needs_confirmation" -> Brass; "blocked" -> Clay; else -> SoftInk }
    val icon = when (action.status) { "complete" -> Icons.Rounded.CheckCircle; "needs_confirmation" -> Icons.Rounded.Info; "blocked" -> Icons.Rounded.Lock; else -> Icons.Rounded.Search }
    Row(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(16.dp)).padding(15.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(action.title, style = MaterialTheme.typography.titleMedium)
            Text(action.detail, color = SoftInk, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private val DAY_CONFIRM_TOOLS = setOf(
    "add_calendar_event",
    "update_calendar_event",
    "add_purchase",
    "update_price_limit",
    "link_outfit",
    "log_observation",
    "update_profile_prefs",
    "update_budget_note"
)

private data class DayToolDraft(
    val tool: String,
    val title: String,
    val detail: String,
    val args: Map<String, String>
) {
    companion object {
        fun from(call: ToolCall): DayToolDraft {
            val args = call.arguments
            return when (call.name) {
                "add_calendar_event" -> DayToolDraft(
                    tool = call.name,
                    title = "Add calendar event",
                    detail = listOfNotNull(
                        args["title"]?.takeIf { it.isNotBlank() },
                        args["date"]?.takeIf { it.isNotBlank() },
                        args["recurrence"]?.takeIf { it.isNotBlank() },
                        args["occasion"]?.takeIf { it.isNotBlank() }?.let { "outfit: $it" }
                    ).joinToString(" · ").ifBlank { "Save this event into Day Hub." },
                    args = args
                )
                "update_calendar_event" -> DayToolDraft(
                    tool = call.name,
                    title = "Update calendar event",
                    detail = listOfNotNull(
                        args["eventId"]?.takeIf { it.isNotBlank() }?.let { "id: $it" },
                        args["title"]?.takeIf { it.isNotBlank() },
                        args["date"]?.takeIf { it.isNotBlank() },
                        args["note"]?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").ifBlank { "Update an existing Day Hub event." },
                    args = args
                )
                "add_purchase" -> DayToolDraft(
                    tool = call.name,
                    title = "Log clothing purchase",
                    detail = listOfNotNull(
                        args["name"]?.takeIf { it.isNotBlank() },
                        args["amount"]?.takeIf { it.isNotBlank() }?.let { "$$it" }
                    ).joinToString(" · ").ifBlank { "Save this purchase into this month’s budget." },
                    args = args
                )
                "update_price_limit" -> DayToolDraft(
                    tool = call.name,
                    title = "Update shopping price limit",
                    detail = args["priceLimit"]?.takeIf { it.isNotBlank() }?.let { "Set max price to $$it" }
                        ?: "Clear the optional shopping price limit.",
                    args = args
                )
                "link_outfit" -> DayToolDraft(
                    tool = call.name,
                    title = "Link planned outfit",
                    detail = listOfNotNull(
                        args["occasion"]?.takeIf { it.isNotBlank() },
                        args["date"]?.takeIf { it.isNotBlank() },
                        args["title"]?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").ifBlank { "Save a planned outfit for the selected day." },
                    args = args
                )
                "update_profile_prefs" -> DayToolDraft(
                    tool = call.name,
                    title = "Update profile preferences",
                    detail = listOfNotNull(
                        args["gender"]?.takeIf { it.isNotBlank() }?.let { "gender: $it" },
                        args["postalCode"]?.takeIf { it.isNotBlank() }?.let { "ZIP $it" },
                        args["colors"]?.takeIf { it.isNotBlank() },
                        args["styles"]?.takeIf { it.isNotBlank() },
                        args["interests"]?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").ifBlank { "Update gender, ZIP, colors, or interests." },
                    args = args
                )
                "update_budget_note" -> DayToolDraft(
                    tool = call.name,
                    title = "Update budget note",
                    detail = args["budgetNote"]?.takeIf { it.isNotBlank() }
                        ?: args["note"]?.takeIf { it.isNotBlank() }
                        ?: "Save a note on this month’s clothing budget.",
                    args = args
                )
                else -> DayToolDraft(
                    tool = call.name,
                    title = "Log fit observation",
                    detail = listOfNotNull(
                        args["garmentId"]?.takeIf { it.isNotBlank() },
                        args["task"]?.takeIf { it.isNotBlank() },
                        args["difficulty"]?.takeIf { it.isNotBlank() }
                    ).joinToString(" · ").ifBlank { "Save this Fit observation after you confirm." },
                    args = args
                )
            }
        }
    }
}

private suspend fun confirmDayDraft(context: android.content.Context, draft: DayToolDraft): String {
    val today = LocalDate.now().toEpochDay()
    return when (draft.tool) {
        "add_calendar_event" -> {
            val title = draft.args["title"]?.trim().orEmpty().ifBlank { draft.args["query"]?.trim().orEmpty() }
            if (title.isBlank()) return "Event needs a title before it can be saved."
            val date = draft.args["date"]?.let { runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull() } ?: today
            val recurrence = runCatching {
                RecurrenceRule.valueOf(draft.args["recurrence"]?.uppercase(Locale.US) ?: "NONE")
            }.getOrDefault(RecurrenceRule.NONE)
            val next = LocalProfileStore.loadEvents(context) + CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = title,
                dateEpochDay = date,
                recurrence = recurrence,
                notes = draft.args["note"].orEmpty(),
                outfitOccasion = draft.args["occasion"]?.takeIf { it.isNotBlank() }
            )
            LocalProfileStore.saveEvents(context, next)
            "Saved event “$title” to Day Hub."
        }
        "add_purchase" -> {
            val name = draft.args["name"]?.trim().orEmpty().ifBlank { draft.args["title"]?.trim().orEmpty() }
            val amount = draft.args["amount"]?.toDoubleOrNull()
            if (name.isBlank() || amount == null) return "Purchase needs a name and amount."
            val budget = LocalProfileStore.loadBudget(context)
            val next = budget.copy(
                purchases = budget.purchases + ClothingPurchase(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    amount = amount,
                    dateEpochDay = draft.args["date"]?.let { runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull() } ?: today
                )
            )
            LocalProfileStore.saveBudget(context, next)
            "Logged purchase “$name” ($${String.format(Locale.US, "%.2f", amount)})."
        }
        "update_price_limit" -> {
            val limit = draft.args["priceLimit"]?.toDoubleOrNull()
            LocalProfileStore.savePriceLimit(context, limit)
            if (limit == null) "Cleared the shopping price limit." else "Updated shopping price limit to $${String.format(Locale.US, "%.0f", limit)}."
        }
        "link_outfit" -> {
            val occasion = draft.args["occasion"]?.trim().orEmpty().ifBlank { draft.args["title"]?.trim().orEmpty() }
            if (occasion.isBlank()) return "Outfit link needs an occasion."
            val date = draft.args["date"]?.let { runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull() } ?: today
            val garmentIds = draft.args["garmentId"]?.split(',', '|', ' ')
                ?.map { it.trim() }?.filter { it.isNotBlank() }.orEmpty()
            val next = LocalProfileStore.loadPlannedOutfits(context) + PlannedOutfit(
                dateEpochDay = date,
                occasion = occasion,
                note = draft.args["note"].orEmpty(),
                garmentIds = garmentIds
            )
            LocalProfileStore.savePlannedOutfits(context, next)
            if (garmentIds.isNotEmpty()) {
                LocalProfileStore.saveAppliedLook(
                    context,
                    LocalProfileStore.AppliedLook(
                        id = "applied-${System.currentTimeMillis()}",
                        title = occasion,
                        garmentIds = garmentIds,
                        occasion = occasion,
                        savedAtEpochDay = date
                    )
                )
            }
            "Saved planned outfit “$occasion” to Day Hub."
        }
        "update_calendar_event" -> {
            val events = LocalProfileStore.loadEvents(context).toMutableList()
            val eventId = draft.args["eventId"]?.trim().orEmpty()
            val index = when {
                eventId.isNotBlank() -> events.indexOfFirst { it.id == eventId }
                else -> events.indexOfFirst {
                    draft.args["title"]?.let { title -> it.title.equals(title, true) } == true
                }.takeIf { it >= 0 } ?: events.lastIndex
            }
            if (index < 0 || index >= events.size) return "No matching calendar event to update."
            val current = events[index]
            events[index] = current.copy(
                title = draft.args["title"]?.takeIf { it.isNotBlank() } ?: current.title,
                dateEpochDay = draft.args["date"]?.let { runCatching { LocalDate.parse(it).toEpochDay() }.getOrNull() }
                    ?: current.dateEpochDay,
                notes = draft.args["note"]?.takeIf { it.isNotBlank() } ?: current.notes,
                outfitOccasion = draft.args["occasion"]?.takeIf { it.isNotBlank() } ?: current.outfitOccasion,
                recurrence = draft.args["recurrence"]?.let {
                    runCatching { RecurrenceRule.valueOf(it.uppercase(Locale.US)) }.getOrNull()
                } ?: current.recurrence
            )
            LocalProfileStore.saveEvents(context, events)
            "Updated event “${events[index].title}”."
        }
        "update_profile_prefs" -> {
            draft.args["gender"]?.takeIf { it.isNotBlank() }?.let { LocalProfileStore.saveGender(context, it) }
            draft.args["postalCode"]?.takeIf { it.isNotBlank() }?.let { LocalProfileStore.savePostalCode(context, it) }
            draft.args["colors"]?.takeIf { it.isNotBlank() }?.let {
                LocalProfileStore.savePreferredColors(context, it.split(',', '|').map(String::trim).filter { c -> c.isNotBlank() })
            }
            draft.args["avoidColors"]?.takeIf { it.isNotBlank() }?.let {
                LocalProfileStore.saveAvoidColors(context, it.split(',', '|').map(String::trim).filter { c -> c.isNotBlank() })
            }
            draft.args["styles"]?.takeIf { it.isNotBlank() }?.let {
                LocalProfileStore.savePreferredStyles(context, it.split(',', '|').map(String::trim).filter { s -> s.isNotBlank() })
            }
            draft.args["interests"]?.takeIf { it.isNotBlank() }?.let {
                LocalProfileStore.saveInterests(context, it.split(',', '|').map(String::trim).filter { i -> i.isNotBlank() })
            }
            draft.args["monthlyBudget"]?.toDoubleOrNull()?.let { amount ->
                val budget = LocalProfileStore.loadBudget(context)
                LocalProfileStore.saveBudget(context, budget.copy(monthlyLimit = amount))
                LocalProfileStore.savePriceLimit(context, amount)
            }
            "Updated profile preferences for discovery and Loomie."
        }
        "update_budget_note" -> {
            val note = draft.args["budgetNote"]?.takeIf { it.isNotBlank() }
                ?: draft.args["note"]?.takeIf { it.isNotBlank() }
                ?: return "Budget note was empty."
            val sizes = LocalProfileStore.loadSizes(context)
            LocalProfileStore.saveSizes(context, sizes.copy(notes = note))
            "Saved budget/size note."
        }
        "log_observation" -> {
            val garmentId = draft.args["garmentId"]?.ifBlank { null }
                ?: LookWeaveRepository.garments.firstOrNull()?.id
                ?: return "No garment available for this observation."
            val task = draft.args["task"]?.ifBlank { null } ?: "Comfort note"
            val difficulty = draft.args["difficulty"]?.ifBlank { null } ?: "Some effort"
            runCatching { LookWeaveGateway.recordObservation(garmentId, task, 0, difficulty) }
                .fold(
                    onSuccess = { "Saved observation for $garmentId ($task · $difficulty)." },
                    onFailure = { "Could not save observation: ${it.message ?: "unknown error"}" }
                )
        }
        else -> "Nothing was saved."
    }
}

/** Small themed speech cloud; Loomie stays fixed — cloud overlays on the right. */
@Composable
private fun ComicSpeechCloud(text: String, modifier: Modifier = Modifier) {
    val clipped = remember(text) {
        val trimmed = text.trim().replace(Regex("\\s+"), " ")
        if (trimmed.length <= 90) trimmed else trimmed.take(89).trimEnd() + "…"
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        // Soft stem connecting into the cloud.
        Canvas(Modifier.size(10.dp, 14.dp)) {
            val stem = Path().apply {
                moveTo(size.width, size.height * .2f)
                quadraticTo(size.width * .2f, size.height * .5f, size.width, size.height * .8f)
                lineTo(size.width, size.height * .2f)
                close()
            }
            drawPath(stem, OliveLight)
        }
        Box(
            Modifier
                .width(118.dp)
                .height(112.dp)
                .background(OliveLight, RoundedCornerShape(18.dp))
                .border(1.dp, Olive.copy(alpha = .28f), RoundedCornerShape(18.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Text(
                clipped,
                color = Olive,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LiveVoiceAgentPanel(state: String, audioLevel: Float, dialogue: String, onActivate: () -> Unit) {
    val active = state == "listening" || state == "speaking" || state == "connecting"
    val loomieMood = when (state) {
        "listening" -> LoomieMood.Listening
        "speaking" -> LoomieMood.Speaking
        "connecting" -> LoomieMood.Thinking
        else -> LoomieMood.Idle
    }
    val showSpeechCloud = state == "speaking" && dialogue.isNotBlank()
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFF7FAF7), Color(0xFFEEF4EF), Color(0xFFE4EBE6))
                ),
                RoundedCornerShape(topEnd = 30.dp, bottomStart = 30.dp)
            )
            .border(1.dp, Olive.copy(alpha = .14f), RoundedCornerShape(topEnd = 30.dp, bottomStart = 30.dp))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Loomie", color = Ink, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text(
                when (state) {
                    "listening" -> "Listening"
                    "speaking" -> "Speaking"
                    "connecting" -> "Connecting"
                    "error" -> "Unavailable"
                    else -> "Ready"
                },
                color = Olive,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(10.dp))
        // Fixed stage: Loomie never shifts when the cloud appears.
        Box(
            Modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            LoomieMascot(
                mood = loomieMood,
                size = 156.dp,
                talking = state == "speaking",
                talkLevel = audioLevel
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = showSpeechCloud,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp),
                enter = fadeIn(tween(180)) + scaleIn(initialScale = .92f, animationSpec = tween(180)),
                exit = fadeOut(tween(140))
            ) {
                ComicSpeechCloud(dialogue)
            }
        }
        Spacer(Modifier.height(4.dp))
        VoiceWave(active = active, level = audioLevel, onActivate = onActivate)
        Spacer(Modifier.height(8.dp))
        Text(
            when (state) {
                "listening" -> "Go ahead—I’m listening."
                "speaking" -> "Here’s what I’d change."
                "connecting" -> "Connecting…"
                "error" -> "Voice is unavailable right now."
                else -> "Talk through the whole look."
            },
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (active) "Tap the signal to end" else "Tap the signal to begin",
            color = SoftInk,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LegacyAgentScreen() {
    val messages = remember {
        mutableStateListOf(
            AgentMessage(false, "Morning, Maya. I kept your gallery look intact and changed one layer that conflicted with your closure preference. Want the reasoning or another direction?")
        )
    }
    var input by rememberSaveable { mutableStateOf("") }
    var listening by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        listening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) input = spoken
        }
    }
    val startVoice = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask LookWeave")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            listening = true
            speechLauncher.launch(intent)
        }
    }

    fun send(text: String) {
        if (text.isBlank()) return
        messages += AgentMessage(true, text)
        val reply = when {
            text.contains("why", true) || text.contains("reason", true) -> "Your slate blazer uses small buttons. In your last confirmed trial, fastening took 42 seconds with two retries. I chose the olive jacket because its magnetic front satisfies your easy-closure preference while keeping the same cropped, tailored line. The other four pieces stay unchanged."
            text.contains("casual", true) -> "I can soften the look without losing its structure: keep the ivory knit and charcoal trouser, swap the jacket for your sand snap overshirt, and use the bone zip sneaker. All selected pieces satisfy today’s preferences."
            text.contains("shoe", true) -> "Your black soft loafer is feasible: step-in closure, flexible upper, and stable heel. The bone zip sneaker is the easier alternative if you expect more walking."
            else -> "I’ll preserve the silhouette and check every change against your confirmed preferences. For this look, the olive magnet jacket is the smallest useful adjustment."
        }
        messages += AgentMessage(false, reply)
        input = ""
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        PageHeader("Loomie", "Your wool companion", Icons.Rounded.ChatBubbleOutline) {
            Box(Modifier.size(10.dp).background(Olive, CircleShape).semantics { contentDescription = "Loomie available" })
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                VoiceAgentPanel(listening = listening, onActivate = startVoice)
            }
            items(messages) { message -> ChatBubble(message) }
            if (messages.size == 1) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf("Why this swap?", "Make it more casual", "Are the shoes easy?")) { prompt ->
                            CompactChip(prompt, false) { send(prompt) }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().background(Paper).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask about your look") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Parchment, unfocusedContainerColor = Parchment, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
            )
            IconButton(onClick = { send(input) }, enabled = input.isNotBlank()) { Icon(Icons.AutoMirrored.Rounded.Send, "Send", tint = if (input.isNotBlank()) Olive else Hairline) }
        }
    }
}

@Composable
private fun VoiceAgentPanel(listening: Boolean, onActivate: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Ink, RoundedCornerShape(2.dp)).padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(if (listening) Clay else Brass, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(if (listening) "LISTENING" else "CONVERSATIONAL VOICE", color = Brass, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.weight(1f))
            Text(if (listening) "SPEAK NOW" else "READY", color = Color.White.copy(alpha = .62f), style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(4.dp))
        VoiceOrb(listening = listening, onActivate = onActivate)
        Text(
            if (listening) "I’m listening…" else "Talk through the whole look.",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            if (listening) "Describe what needs to feel different." else "Tap the weave to start a private voice conversation.",
            color = Color.White.copy(alpha = .66f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VoiceOrb(listening: Boolean, audioLevel: Float = 0f, onActivate: () -> Unit) {
    val level by animateFloatAsState(
        targetValue = if (listening) audioLevel.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(110),
        label = "voice level"
    )
    val motion = rememberInfiniteTransition(label = "voice weave")
    val phase by motion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6200, easing = LinearEasing), RepeatMode.Restart),
        label = "voice weave phase"
    )
    val activePhase = if (listening) phase else 0f

    Box(
        Modifier
            .size(184.dp)
            .semantics { contentDescription = if (listening) "LookWeave is listening" else "Start voice conversation" }
            .clickable(role = Role.Button, onClick = onActivate),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension * .31f
            drawCircle(Color.White.copy(alpha = .035f), radius * 1.58f, center)
            drawCircle(Color.White.copy(alpha = .07f), radius * 1.35f, center, style = Stroke(width = 1.dp.toPx()))
            repeat(3) { index ->
                drawArc(
                    color = if (index == 1) Brass.copy(alpha = if (listening) .72f else .28f) else OliveLight.copy(alpha = if (listening) .42f else .16f),
                    startAngle = activePhase * 360f + index * 118f,
                    sweepAngle = 48f + index * 19f + level * 34f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * (1.22f + index * .12f), center.y - radius * (1.22f + index * .12f)),
                    size = androidx.compose.ui.geometry.Size(radius * 2f * (1.22f + index * .12f), radius * 2f * (1.22f + index * .12f)),
                    style = Stroke(width = (1.2f + index * .45f).dp.toPx(), cap = StrokeCap.Round)
                )
            }
            val weave = Path()
            repeat(97) { index ->
                val angle = index / 96f * 2f * PI.toFloat()
                val ripple = sin(angle * 4f + activePhase * 2f * PI.toFloat()) * radius * (.025f + level * .075f)
                val r = radius * 1.08f + ripple
                val point = Offset(center.x + cos(angle) * r, center.y + sin(angle) * r)
                if (index == 0) weave.moveTo(point.x, point.y) else weave.lineTo(point.x, point.y)
            }
            weave.close()
            drawPath(weave, OliveLight.copy(alpha = if (listening) .5f else .18f), style = Stroke(width = 1.dp.toPx()))
            drawCircle(Brush.radialGradient(listOf(if (listening) Color(0xFF789287) else Color(0xFF424B46), Color(0xFF29302C))), radius, center)
            drawCircle(Color.White.copy(alpha = .15f), radius, center, style = Stroke(width = 1.dp.toPx()))
            val bars = listOf(.26f, .44f, .68f, .92f, .68f, .44f, .26f)
            bars.forEachIndexed { index, strength ->
                val energy = if (listening) .55f + level * 1.2f else .46f
                val halfHeight = radius * strength * .34f * energy
                val x = center.x + (index - 3) * radius * .16f
                drawLine(
                    Color.White.copy(alpha = .94f),
                    Offset(x, center.y - halfHeight),
                    Offset(x, center.y + halfHeight),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private data class AgentUiCard(
    val id: String,
    val eyebrow: String,
    val title: String,
    val detail: String,
    val actionLabel: String,
    val action: String,
    val afterMessage: Int = -1
)

private fun outfitAnswer(plan: OutfitPlan): String {
    if (plan.resolved.isEmpty()) return "I don't have enough wardrobe pieces to build this look yet. ${plan.explanation}"
    val pieces = plan.resolved.joinToString(" · ") { it.name }
    val opening = if (plan.unresolved.isEmpty()) "Wear: $pieces." else "Start with: $pieces. A complete look still needs another suitable piece."
    return "$opening ${plan.explanation}".trim()
}

private fun outfitCardDetail(plan: OutfitPlan): String {
    val pieces = plan.resolved.joinToString(" · ") { it.name }
    return listOf(pieces, plan.explanation).filter { it.isNotBlank() }.joinToString("\n")
}

private fun cleanAgentDisplayText(value: String): String {
    var clean = value.trim()
        .replace(Regex("^```(?:json|markdown|text)?\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\s*```$", RegexOption.IGNORE_CASE), "")
        .trim()
    if (clean.startsWith("{") && clean.endsWith("}")) {
        clean = runCatching { org.json.JSONObject(clean).optString("message").ifBlank { clean } }.getOrDefault(clean)
    }
    return clean
        .replace(Regex("\\[\\s*(?:solve|search|research|browse|location|weather|signals|profile|calendar|rule)\\s*\\]", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\b(?:I['’]?ll|I will|I can)\\s+(?:use|call|invoke)\\s+(?:the\\s+)?[a-z_]+\\s+tool(?:\\s+for\\s+this|\\s+to\\s+do\\s+this)?\\.?", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\b(?:the\\s+)?[a-z_]+\\s+tool\\s+(?:for\\s+this|to\\s+do\\s+this)\\.?", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\[\\s*proposeToolCall\\s*\\][\\s\\S]*?\\[\\s*\\/\\s*proposeToolCall\\s*\\]", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\[\\s*\\/?(?:tool_call|function_call|tool_calls?)\\s*\\]", RegexOption.IGNORE_CASE), "")
        .replace(Regex("\\{\\s*[\\\"']tool_code[\\\"']\\s*:\\s*[\\s\\S]*?\\}\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<tool_call>[\\s\\S]*?</tool_call>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</?(?:function_call|tool_calls?)>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("(?:^|\\n)\\s*(?:tool[_ ]?calls?|function[_ ]?call|proposedToolCalls)\\s*[:=][\\s\\S]*$", RegexOption.IGNORE_CASE), "")
        .replace(Regex("(?:^|\\n)\\s*(?:search_wardrobe|research_products|solve_outfit|suggest_outfit_swap|draft_rule|retrieve_rules|update_profile_prefs|add_calendar_event|update_calendar_event|log_observation)\\s*\\([^\\n]*\\)\\s*", RegexOption.IGNORE_CASE), "")
        .replace(Regex("```(?:json|markdown|text)?", RegexOption.IGNORE_CASE), "")
        .replace(Regex("^\\s*(?:message|answer)\\s*:\\s*", RegexOption.IGNORE_CASE), "")
        .trim()
        .take(1200)
        .ifBlank { "I prepared the useful next step below." }
}

private fun agentCardFor(call: ToolCall, context: android.content.Context): AgentUiCard? {
    val stamp = System.nanoTime()
    return when (call.name) {
        "search_wardrobe" -> {
            val owned = SamplePersonProfiles.wardrobeForActiveProfile(context)
            AgentUiCard(
                "owned-$stamp", "WHAT YOU OWN", "${owned.size} pieces checked",
                owned.take(4).joinToString { it.name }.ifBlank { "Your wardrobe is ready for review." },
                "Open wardrobe", "wardrobe"
            )
        }
        "get_user_location" -> AgentUiCard(
            "location-$stamp", "PRIVATE LOCATION", "General area resolved",
            "Loomie uses your ZIP for context and never needs your street address.",
            "Open nearby", "nearby"
        )
        "get_day_signals" -> AgentUiCard(
            "signals-$stamp", "LIVE CONTEXT", "Weather and comfort signals",
            "Temperature, UV, wind, air quality and pollen can now inform the look.",
            "Open day signals", "signals"
        )
        "research_products" -> AgentUiCard(
            "browse-$stamp", "WHAT YOU MAY NEED", "Browse evidence-backed alternatives",
            call.arguments["query"]?.takeIf { it.isNotBlank() }
                ?: "Search live product sources. Price, stock and accessibility remain unverified until shown by the retailer.",
            "Open live discovery", "browse"
        )
        "retrieve_rules", "draft_rule", "create_rule", "edit_rule", "delete_rule", "apply_rules" -> AgentUiCard(
            "rules-$stamp", "YOUR PRIORITIES", "Wearability rules",
            "Review confirmed requirements and decide what should affect future looks.",
            "Review in Plan", "plan"
        )
        "add_calendar_event", "update_calendar_event", "add_purchase", "update_price_limit", "link_outfit", "update_budget_note" -> AgentUiCard(
            "day-$stamp", "DAY HUB", "A change is ready to review",
            "Nothing is saved until you confirm the draft in this conversation.",
            "Open Day Hub", "day"
        )
        "update_profile_prefs", "log_observation" -> AgentUiCard(
            "profile-$stamp", "LIVING PROFILE", "Profile evidence and preferences",
            "Review what Loomie knows, including recent confirmed wearability evidence.",
            "Open profile", "profile"
        )
        else -> null
    }
}

@Composable
private fun AgentUiResultCard(card: AgentUiCard, onOpen: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(18.dp)).border(1.dp, Hairline, RoundedCornerShape(18.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(card.eyebrow, color = Brass, style = MaterialTheme.typography.labelSmall)
        Text(card.title, color = Ink, style = MaterialTheme.typography.titleMedium)
        Text(card.detail, color = SoftInk, style = MaterialTheme.typography.bodyMedium, maxLines = 7, overflow = TextOverflow.Ellipsis)
        OutlinedButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
            Text(card.actionLabel)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ChatBubble(message: AgentMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
        Column(
            Modifier.widthIn(max = 320.dp).background(if (message.fromUser) Olive else Paper, RoundedCornerShape(if (message.fromUser) 16.dp else 6.dp)).padding(14.dp)
        ) {
            if (!message.fromUser) Text("LookWeave", style = MaterialTheme.typography.labelLarge, color = SoftInk)
            Text(cleanAgentDisplayText(message.text), color = if (message.fromUser) Color.White else Ink, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ProfileAvatar(displayName: String, portraitUri: String?, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    var bitmap by remember(portraitUri) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(portraitUri) {
                bitmap = withContext(Dispatchers.IO) {
            portraitUri?.let { value ->
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(value))?.use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
            }
        }
    }
    Box(Modifier.size(size).clip(CircleShape).background(Ink), contentAlignment = Alignment.Center) {
        val photo = bitmap
        if (photo != null) {
            Image(photo.asImageBitmap(), "Profile photo", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            val letter = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Text(letter, color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ProfileScreen(
    preferences: WearabilityPreferences,
    onPreferences: (WearabilityPreferences) -> Unit,
    onBack: () -> Unit,
    onStyle: () -> Unit,
    displayName: String,
    portraitUri: String?,
    profileEpoch: Int,
    onDisplayName: (String) -> Unit,
    onPortraitUri: (String?) -> Unit,
    onProfileActivated: () -> Unit,
    onReset: () -> Unit
) {
    val profileContext = LocalContext.current
    var showReset by remember { mutableStateOf(false) }
    var nameDraft by rememberSaveable { mutableStateOf(displayName) }
    LaunchedEffect(displayName) { if (displayName.isNotBlank()) nameDraft = displayName }
    var observations by remember { mutableStateOf(emptyList<org.json.JSONObject>()) }
    var evidenceError by remember { mutableStateOf(false) }
    var showProfileFacts by rememberSaveable { mutableStateOf(true) }
    var showEvidence by rememberSaveable { mutableStateOf(true) }
    var showEditProfile by rememberSaveable { mutableStateOf(false) }
    var profileEditText by rememberSaveable { mutableStateOf("") }
    var profileEditBusy by rememberSaveable { mutableStateOf(false) }
    var profileEditError by rememberSaveable { mutableStateOf<String?>(null) }
    val profileEditScope = rememberCoroutineScope()
    val portraitPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) onPortraitUri(LocalProfileStore.copyPortraitToAppFiles(profileContext, uri))
    }
    val budget = remember { LocalProfileStore.loadBudget(profileContext) }
    val events = remember { LocalProfileStore.loadEvents(profileContext) }
    val month = YearMonth.now()
    val remaining = budget.remaining(month.atDay(1).toEpochDay(), month.plusMonths(1).atDay(1).toEpochDay())
    val upcoming = events.count {
        val day = LocalDate.ofEpochDay(it.dateEpochDay)
        !day.isBefore(LocalDate.now())
    }
    val activeFilters = preferences.userFilters.count { it.enabled } +
        listOf(preferences.oneHandClosure, preferences.seatedComfort, preferences.lowSensoryLoad, preferences.deviceClearance, preferences.easyFootwear, preferences.avoidWool, preferences.animalFree, preferences.limitedOverheadReach, preferences.gripStrength, preferences.seamPressureSensitivity, preferences.easyRemoval, preferences.mobilityAidCompatibility).count { it }
    val wardrobeCount = SamplePersonProfiles.wardrobeForActiveProfile(profileContext).size
    val fitInsights = observations.size.coerceAtLeast(LookWeaveRepository.observations.size)
    LaunchedEffect(Unit) { runCatching { LookWeaveGateway.observations() }.onSuccess { observations = it }.onFailure { evidenceError = true } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 30.dp)) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to Today") }
                Column(Modifier.weight(1f)) {
                    Text("LIVING, NOT LABELLED", style = MaterialTheme.typography.labelSmall, color = Brass)
                    Text("Your profile", style = MaterialTheme.typography.headlineMedium)
                }
                ProfileAvatar(displayName = nameDraft.ifBlank { displayName }, portraitUri = portraitUri, size = 44.dp)
                Spacer(Modifier.width(10.dp))
            }
        }
        item {
            ProfileSwitcherSection(
                profileEpoch = profileEpoch,
                onActivated = onProfileActivated
            )
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onDisplayName(nameDraft) }) { Text("Save name") }
                    OutlinedButton(onClick = { portraitPicker.launch("image/*") }) { Text(if (portraitUri == null) "Add photo" else "Change photo") }
                }
                val meta = LocalProfileStore.personaMeta(profileContext)
                val activePreferenceLabels = buildList {
                    if (preferences.oneHandClosure) add("One-hand closures")
                    if (preferences.seatedComfort) add("Seated comfort")
                    if (preferences.lowSensoryLoad) add("Low sensory load")
                    if (preferences.deviceClearance) add("Device clearance")
                    if (preferences.easyFootwear) add("Easy footwear")
                    if (preferences.limitedOverheadReach) add("Limited overhead reach")
                    if (preferences.gripStrength) add("Grip-friendly")
                    if (preferences.mobilityAidCompatibility) add("Mobility-aid compatible")
                }
                CollapsibleProfileTable(
                    expanded = showProfileFacts,
                    onToggle = { showProfileFacts = !showProfileFacts },
                    onEdit = { profileEditText = ""; profileEditError = null; showEditProfile = true },
                    rows = listOf(
                        "Identity" to listOfNotNull(
                            LocalProfileStore.genderLabel(profileContext),
                            meta.age?.let { "Age $it" },
                            meta.occupation.takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifBlank { "Not specified" },
                        "Location" to LocalProfileStore.postalCode(profileContext).ifBlank { "Private · ZIP not added" },
                        "Style" to LocalProfileStore.preferredStyles(profileContext).joinToString().ifBlank { "Still learning" },
                        "Colours" to LocalProfileStore.preferredColors(profileContext).joinToString().ifBlank { "Still learning" },
                        "Wearability" to activePreferenceLabels.joinToString().ifBlank { "No active requirements" },
                        "Interests" to LocalProfileStore.interests(profileContext).joinToString().ifBlank { "Not added" },
                        "Profile note" to meta.summary.ifBlank { "Add details through your preferences or Loomie." }
                    )
                )
                PreferenceHubSection(
                    context = profileContext,
                    profileEpoch = profileEpoch,
                    preferences = preferences,
                    onPreferences = onPreferences
                )
                Text("Dashboard", style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        "$activeFilters" to "Active filters",
                        "$${remaining.toInt()}" to "Budget left",
                        "$upcoming" to "Upcoming events",
                        "$fitInsights" to "Fit insights",
                        "$wardrobeCount" to "Wardrobe"
                    ).forEach { (value, label) ->
                        Column(
                            Modifier.width(150.dp).background(Paper, RoundedCornerShape(14.dp)).padding(14.dp)
                        ) {
                            Text(value, style = MaterialTheme.typography.headlineSmall, color = Olive)
                            Text(label, color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(22.dp))
            Row(
                Modifier.fillMaxWidth().clickable { showEvidence = !showEvidence }.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.History, null, tint = Olive)
                Spacer(Modifier.width(10.dp))
                Text("Recent evidence", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Text(if (showEvidence) "Hide" else "Show ${observations.size}", color = Olive, style = MaterialTheme.typography.labelLarge)
            }
            AnimatedVisibility(showEvidence) {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    if (observations.isEmpty()) Text(if (evidenceError) "Evidence could not be loaded. Reopen your profile to retry." else "Your first Fit Lab observation will appear here.", color = SoftInk)
                    observations.takeLast(8).reversed().forEach { item ->
                        val name = LookWeaveRepository.allGarments.firstOrNull { it.id == item.optString("garmentId") }?.name ?: "Garment"
                        val seconds = item.optInt("seconds")
                        val result = listOfNotNull(
                            seconds.takeIf { it > 0 }?.let { "$it sec" },
                            item.optString("difficulty").takeIf { it.isNotBlank() }
                        ).joinToString(" · ").ifBlank { "Saved comfort note" }
                        HistoryRow(name, item.optString("task"), result, item.optString("recordedAt").take(10))
                    }
                }
            }
        }
        item { CustomRulesSection(preferences, onPreferences) }
        item {
            Spacer(Modifier.height(18.dp))
            Column(Modifier.padding(horizontal = 20.dp)) {
                WardrobeActionCard(
                    eyebrow = "YOUR LOOK",
                    title = "Customize colours / face",
                    subtitle = "Face proportions, colour story, and accessory preferences — same Style Studio analysis.",
                    onClick = onStyle
                )
            }
        }
        item {
            Spacer(Modifier.height(22.dp)); SectionTitle("Account", icon = Icons.Rounded.PersonOutline)
            Column(Modifier.padding(horizontal = 20.dp)) {
                ProfileAction(Icons.Rounded.Settings, "App settings", "Photos, microphone, and device permissions") {
                    profileContext.startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${profileContext.packageName}")))
                }
                ProfileAction(Icons.Rounded.DeleteOutline, "Clear profile", "Remove preferences and history on this device") { showReset = true }
            }
        }
    }
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Clear your profile?") },
            text = { Text("This removes your synced preferences, garment observations, saved places and locally saved preview, then returns to the welcome screen.") },
            confirmButton = { TextButton(onClick = { showReset = false; onReset() }) { Text("Clear profile", color = Clay) } },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel") } },
            containerColor = Paper,
            shape = RoundedCornerShape(4.dp)
        )
    }
    if (showEditProfile) {
        AlertDialog(
            onDismissRequest = { if (!profileEditBusy) showEditProfile = false },
            title = { Text("Edit profile at a glance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Describe any changes naturally. Loomie will organize them into profile fields before saving.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
                    TextField(
                        value = profileEditText,
                        onValueChange = { profileEditText = it; profileEditError = null },
                        placeholder = { Text("I live in 60614, prefer sage and navy, like relaxed tailoring, and need easy-grip closures") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    profileEditError?.let { Text(it, color = Clay, style = MaterialTheme.typography.bodySmall) }
                }
            },
            confirmButton = {
                TextButton(enabled = profileEditText.isNotBlank() && !profileEditBusy, onClick = {
                    profileEditBusy = true
                    profileEditScope.launch {
                        runCatching {
                            LookWeaveGateway.respond(
                                AgentRequest(
                                    message = "Organize this profile update into structured fields and propose update_profile_prefs. Save only details explicitly stated by the user: $profileEditText",
                                    garmentEvidence = emptyList(),
                                    hardConstraintIds = emptySet()
                                )
                            )
                        }.onSuccess { response ->
                            val draft = response.proposedToolCalls.firstOrNull { it.name == "update_profile_prefs" }?.let { DayToolDraft.from(it) }
                            if (draft == null) profileEditError = "I could not find a clear profile field to update. Try mentioning location, colours, styles, interests, or gender."
                            else runCatching { confirmDayDraft(profileContext, draft) }
                                .onSuccess { showEditProfile = false; onProfileActivated() }
                                .onFailure { profileEditError = it.message ?: "Could not save this profile update." }
                        }.onFailure { profileEditError = it.message ?: "Could not organize that update." }
                        profileEditBusy = false
                    }
                }) { Text(if (profileEditBusy) "Organizing…" else "Organize & save") }
            },
            dismissButton = { TextButton(enabled = !profileEditBusy, onClick = { showEditProfile = false }) { Text("Cancel") } },
            containerColor = Paper,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun CollapsibleProfileTable(
    expanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    rows: List<Pair<String, String>>
) {
    Column(
        Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(16.dp)).border(1.dp, Hairline, RoundedCornerShape(16.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Profile at a glance", style = MaterialTheme.typography.titleMedium)
                Text("Identity, style and practical preferences", color = SoftInk, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onEdit) { Text("Edit", color = Olive) }
            Text(if (expanded) "Collapse" else "Expand", color = Olive, style = MaterialTheme.typography.labelLarge)
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp)) {
                rows.forEachIndexed { index, row ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.Top) {
                        Text(row.first, color = SoftInk, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(92.dp))
                        Text(row.second, color = Ink, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    if (index != rows.lastIndex) HorizontalDivider(color = Hairline)
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun CustomRulesSection(preferences: WearabilityPreferences, onPreferences: (WearabilityPreferences) -> Unit) {
    val scope = rememberCoroutineScope()
    var text by rememberSaveable { mutableStateOf("") }
    var draft by remember { mutableStateOf<WearabilityRule?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    Column(Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(22.dp))
        SectionTitle("Custom rules", "Ask in your own words", Icons.Rounded.Edit)
        Text("Describe a preference in your own words, then confirm before it applies.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(10.dp))
        TextField(value = text, onValueChange = { text = it; draft = null }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("For example: avoid clothing that is hard to remove") }, minLines = 2)
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            busy = true; error = null
            scope.launch { runCatching { LookWeaveGateway.draftRule(text) }.onSuccess { draft = it }.onFailure { error = it.message ?: "Could not understand that rule." }; busy = false }
        }, enabled = text.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Checking rule…" else "Review rule") }
        draft?.let { candidate ->
            Spacer(Modifier.height(10.dp))
            Column(Modifier.fillMaxWidth().background(OliveLight).padding(14.dp)) {
                Text(candidate.label, style = MaterialTheme.typography.titleMedium)
                Text("${candidate.kind.name.lowercase().replaceFirstChar { it.uppercase() }} rule · confirmation required", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    busy = true
                    scope.launch { runCatching { LookWeaveGateway.createRule(candidate) }.onSuccess { saved -> onPreferences(preferences.copy(customRules = preferences.customRules + saved)); draft = null; text = "" }.onFailure { error = it.message ?: "Could not save this rule." }; busy = false }
                }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Text("Confirm and save") }
            }
        }
        error?.let { Text(it, color = Clay, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)) }
        preferences.customRules.filter { it.enabled && it.confirmed }.forEach { rule ->
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(rule.label, style = MaterialTheme.typography.titleMedium); Text(rule.kind.name.lowercase(), color = SoftInk, style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = {
                    scope.launch { runCatching { LookWeaveGateway.deleteRule(rule.id) }.onSuccess { remaining -> onPreferences(preferences.copy(customRules = remaining)) } }
                }) { Icon(Icons.Rounded.DeleteOutline, "Delete ${rule.label}", tint = Clay) }
            }
            HorizontalDivider(color = Hairline)
        }
    }
}

@Composable
private fun HistoryRow(title: String, task: String, result: String, date: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(OliveLight, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Check, null, tint = Olive) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text("$task · $result", color = SoftInk, style = MaterialTheme.typography.bodyMedium) }
        Text(date, style = MaterialTheme.typography.labelSmall, color = Brass)
    }
    HorizontalDivider(color = Hairline)
}

@Composable
private fun ProfileSwitcherSection(
    profileEpoch: Int,
    onActivated: () -> Unit
) {
    val context = LocalContext.current
    var profiles by remember(profileEpoch) { mutableStateOf(SamplePersonProfiles.allProfiles(context)) }
    var activeId by remember(profileEpoch) { mutableStateOf(SamplePersonProfiles.activeId(context)) }
    var pendingBodyUri by remember { mutableStateOf<Uri?>(null) }
    var addName by rememberSaveable { mutableStateOf("") }
    var showAddName by remember { mutableStateOf(false) }

    val bodyPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        pendingBodyUri = uri
        showAddName = true
    }
    val facePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { face ->
        val body = pendingBodyUri ?: return@rememberLauncherForActivityResult
        val name = addName.ifBlank { "Custom profile" }
        SamplePersonProfiles.addCustomProfile(context, name, body, face)
        pendingBodyUri = null
        addName = ""
        showAddName = false
        profiles = SamplePersonProfiles.allProfiles(context)
        activeId = SamplePersonProfiles.activeId(context)
        onActivated()
    }

    Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Person profiles", style = MaterialTheme.typography.titleLarge)
        Text(
            "Six personas (sensory, brace, prosthesis, office, modest, budget). Switching loads ZIP, prefs, capsule, budget, and day seeds.",
            color = SoftInk,
            style = MaterialTheme.typography.bodyMedium
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(profiles, key = { it.id }) { profile ->
                val selected = profile.id == activeId
                Column(
                    Modifier
                        .width(88.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) OliveLight else Paper)
                        .border(1.dp, if (selected) Olive else Hairline, RoundedCornerShape(14.dp))
                        .clickable {
                            if (SamplePersonProfiles.activate(context, profile.id)) {
                                activeId = profile.id
                                onActivated()
                            }
                        }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileThumb(profile = profile, selected = selected)
                    Spacer(Modifier.height(6.dp))
                    val switcherName = profile.displayName.substringBefore(" ·")
                    Text(
                        switcherName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) Olive else Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (profile.age != null || profile.occupation.isNotBlank()) {
                        Text(
                            listOfNotNull(profile.age?.toString(), profile.occupation.takeIf { it.isNotBlank() }).joinToString(" "),
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (!profile.builtIn) {
                        TextButton(
                            onClick = {
                                SamplePersonProfiles.deleteCustomProfile(context, profile.id)
                                profiles = SamplePersonProfiles.allProfiles(context)
                                activeId = SamplePersonProfiles.activeId(context)
                                onActivated()
                            },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("Remove", color = Clay, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            item {
                Column(
                    Modifier
                        .width(88.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Paper)
                        .border(1.dp, Hairline, RoundedCornerShape(14.dp))
                        .clickable { bodyPicker.launch("image/*") }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add profile", tint = Olive)
                    Spacer(Modifier.height(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelSmall, color = SoftInk)
                }
            }
        }
        if (showAddName) {
            TextField(
                value = addName,
                onValueChange = { addName = it },
                label = { Text("Custom profile name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val body = pendingBodyUri
                        if (body != null) {
                            SamplePersonProfiles.addCustomProfile(context, addName.ifBlank { "Custom profile" }, body, null)
                            pendingBodyUri = null
                            addName = ""
                            showAddName = false
                            profiles = SamplePersonProfiles.allProfiles(context)
                            activeId = SamplePersonProfiles.activeId(context)
                            onActivated()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save with body only") }
                Button(
                    onClick = { facePicker.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) { Text("Add face photo") }
            }
        }
    }
}

@Composable
private fun ProfileThumb(profile: SamplePersonProfiles.Profile, selected: Boolean) {
    val context = LocalContext.current
    var bitmap by remember(profile.id) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(profile.id) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                when {
                    profile.builtIn -> {
                        // Face → body → thumb, paths from the profile only (identity assets).
                        val thumb = "sample/${profile.id}/thumb.jpg"
                        sequenceOf(profile.faceAssetPath, profile.bodyAssetPath, thumb)
                            .filterNotNull()
                            .firstNotNullOfOrNull { path ->
                                runCatching {
                                    context.assets.open(path).use { BitmapFactory.decodeStream(it) }
                                }.getOrNull()
                            }
                    }
                    else -> {
                        sequenceOf(profile.faceFileName, profile.bodyFileName)
                            .filterNotNull()
                            .firstNotNullOfOrNull { name ->
                                val file = File(context.filesDir, "profiles/$name")
                                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                            }
                    }
                }
            }.getOrNull()
        }
    }
    Box(
        Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (selected) Olive else OliveLight)
            .border(2.dp, if (selected) Olive else Hairline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap!!.asImageBitmap(),
                contentDescription = profile.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                profile.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = if (selected) Paper else Ink,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private data class PrefTag(
    val id: String,
    val label: String,
    val group: String,
    val kind: String = "filter" // filter | classic | color | lifestyle | budget
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MyPreferencesSection(
    preferences: WearabilityPreferences,
    onPreferences: (WearabilityPreferences) -> Unit,
    profileEpoch: Int = 0
) {
    val context = LocalContext.current
    val classicTags = listOf(
        PrefTag("classic_one_hand", "Easy one-hand closure", "Mobility", "classic"),
        PrefTag("classic_seated", "Comfort while seated", "Mobility", "classic"),
        PrefTag("classic_sensory", "Low sensory load", "Sensory", "classic"),
        PrefTag("classic_device", "Device clearance", "Mobility", "classic"),
        PrefTag("classic_footwear", "Easy footwear", "Mobility", "classic"),
        PrefTag("classic_overhead", "Limited overhead reach", "Dressing", "classic"),
        PrefTag("classic_grip", "Grip-friendly", "Dressing", "classic"),
        PrefTag("classic_pressure", "Low seam pressure", "Sensory", "classic"),
        PrefTag("classic_removal", "Easy removal", "Dressing", "classic"),
        PrefTag("classic_mobility", "Mobility-aid friendly", "Mobility", "classic"),
        PrefTag("classic_wool", "Avoid wool", "Materials", "classic"),
        PrefTag("classic_animal", "Animal-free", "Materials", "classic"),
        PrefTag("classic_heat", "Heat sensitive", "Lifestyle", "classic"),
        PrefTag("classic_energy", "Conserve energy", "Lifestyle", "classic"),
        PrefTag("classic_stepfree", "Step-free shopping", "Lifestyle", "classic")
    )
    val filterTags = listOf(
        "sn_tagless", "sn_soft_handfeel", "sn_no_stiff_collar", "mt_stretch", "se_soft_waist",
        "cl_magnetic", "rc_sleeve_ease", "clm_light_layers", "clm_uv_cover", "mt_breathable",
        "sz_price_aware", "oc_work", "oc_casual", "oc_gallery"
    ).mapNotNull { id ->
        FilterCatalog.byId(id)?.let { PrefTag(it.id, it.label, when (it.category) {
            FilterCategory.SENSORY -> "Sensory"
            FilterCategory.MOBILITY, FilterCategory.SEATED, FilterCategory.FOOTWEAR -> "Mobility"
            FilterCategory.REACH, FilterCategory.GRIP, FilterCategory.PRESSURE, FilterCategory.CLOSURES -> "Dressing"
            FilterCategory.MATERIALS -> "Materials"
            FilterCategory.CLIMATE, FilterCategory.OCCASION -> "Lifestyle"
            FilterCategory.SIZING -> "Budget"
        }, "filter") }
    }
    var colorLove by remember(profileEpoch) { mutableStateOf(LocalProfileStore.preferredColors(context)) }
    var colorAvoid by remember(profileEpoch) { mutableStateOf(LocalProfileStore.avoidColors(context)) }
    var lifestyleSelected by remember(profileEpoch) {
        mutableStateOf(
            (LocalProfileStore.preferredStyles(context) + LocalProfileStore.interests(context)).distinct()
        )
    }
    var budgetLimit by remember(profileEpoch) { mutableStateOf(LocalProfileStore.loadBudget(context).monthlyLimit) }
    val colorPool = (colorLove + colorAvoid + PersonaTagCatalog.colors + PersonaTagCatalog.avoidColors).distinct()
    val lifestylePool = (lifestyleSelected + PersonaTagCatalog.styles + PersonaTagCatalog.interests).distinct()
    val budgetTags = listOf(
        PrefTag("budget_50", "\$50 / mo", "Budget", "budget"),
        PrefTag("budget_100", "\$100 / mo", "Budget", "budget"),
        PrefTag("budget_150", "\$150 / mo", "Budget", "budget")
    )
    val catalog = classicTags + filterTags + colorPool.map { PrefTag("color::$it", it, "Colors", "color") } +
        lifestylePool.map { PrefTag("life::$it", it, "Lifestyle", "lifestyle") } + budgetTags

    fun classicOn(id: String): Boolean = when (id) {
        "classic_one_hand" -> preferences.oneHandClosure
        "classic_seated" -> preferences.seatedComfort
        "classic_sensory" -> preferences.lowSensoryLoad
        "classic_device" -> preferences.deviceClearance
        "classic_footwear" -> preferences.easyFootwear
        "classic_overhead" -> preferences.limitedOverheadReach
        "classic_grip" -> preferences.gripStrength
        "classic_pressure" -> preferences.seamPressureSensitivity
        "classic_removal" -> preferences.easyRemoval
        "classic_mobility" -> preferences.mobilityAidCompatibility
        "classic_wool" -> preferences.avoidWool
        "classic_animal" -> preferences.animalFree
        "classic_heat" -> preferences.heatSensitive
        "classic_energy" -> preferences.conserveEnergy
        "classic_stepfree" -> preferences.stepFreeAccess
        else -> false
    }

    fun isSelected(tag: PrefTag): Boolean = when (tag.kind) {
        "classic" -> classicOn(tag.id)
        "filter" -> preferences.userFilters.any { it.filterId == tag.id && it.enabled }
        "color" -> tag.label in colorLove || tag.label in colorAvoid
        "lifestyle" -> tag.label in lifestyleSelected
        "budget" -> when (tag.id) {
            "budget_50" -> budgetLimit <= 55
            "budget_100" -> budgetLimit in 56.0..110.0
            "budget_150" -> budgetLimit in 111.0..175.0
            else -> false
        }
        else -> false
    }

    fun toggle(tag: PrefTag) {
        when (tag.kind) {
            "classic" -> {
                val next = when (tag.id) {
                    "classic_one_hand" -> preferences.copy(oneHandClosure = !preferences.oneHandClosure)
                    "classic_seated" -> preferences.copy(seatedComfort = !preferences.seatedComfort)
                    "classic_sensory" -> preferences.copy(lowSensoryLoad = !preferences.lowSensoryLoad)
                    "classic_device" -> preferences.copy(deviceClearance = !preferences.deviceClearance)
                    "classic_footwear" -> preferences.copy(easyFootwear = !preferences.easyFootwear)
                    "classic_overhead" -> preferences.copy(limitedOverheadReach = !preferences.limitedOverheadReach)
                    "classic_grip" -> preferences.copy(gripStrength = !preferences.gripStrength)
                    "classic_pressure" -> preferences.copy(seamPressureSensitivity = !preferences.seamPressureSensitivity)
                    "classic_removal" -> preferences.copy(easyRemoval = !preferences.easyRemoval)
                    "classic_mobility" -> preferences.copy(mobilityAidCompatibility = !preferences.mobilityAidCompatibility)
                    "classic_wool" -> preferences.copy(avoidWool = !preferences.avoidWool)
                    "classic_animal" -> preferences.copy(animalFree = !preferences.animalFree)
                    "classic_heat" -> preferences.copy(heatSensitive = !preferences.heatSensitive)
                    "classic_energy" -> preferences.copy(conserveEnergy = !preferences.conserveEnergy)
                    "classic_stepfree" -> preferences.copy(stepFreeAccess = !preferences.stepFreeAccess)
                    else -> preferences
                }
                LocalProfileStore.saveWearabilitySeed(context, next)
                onPreferences(next)
            }
            "filter" -> {
                val existing = preferences.userFilters.firstOrNull { it.filterId == tag.id && it.enabled }
                val nextFilters = when {
                    existing == null -> preferences.userFilters + UserFilter(tag.id, FilterMode.MUST_AVOID, enabled = true)
                    existing.mode == FilterMode.MUST_AVOID ->
                        preferences.userFilters.filterNot { it.filterId == tag.id } +
                            UserFilter(tag.id, FilterMode.PREFER, enabled = true)
                    else -> preferences.userFilters.filterNot { it.filterId == tag.id }
                }
                val next = FilterCatalog.syncClassicPreferences(preferences.copy(userFilters = nextFilters))
                LocalProfileStore.saveWearabilitySeed(context, next)
                onPreferences(next)
            }
            "color" -> {
                val love = colorLove.toMutableList()
                val avoid = colorAvoid.toMutableList()
                when {
                    tag.label in love -> {
                        love.remove(tag.label)
                        avoid += tag.label
                    }
                    tag.label in avoid -> avoid.remove(tag.label)
                    else -> love += tag.label
                }
                colorLove = love
                colorAvoid = avoid
                LocalProfileStore.savePreferredColors(context, love)
                LocalProfileStore.saveAvoidColors(context, avoid)
            }
            "lifestyle" -> {
                val styles = lifestyleSelected.toMutableList()
                if (tag.label in styles) styles.remove(tag.label) else styles += tag.label
                lifestyleSelected = styles
                LocalProfileStore.savePreferredStyles(context, styles)
            }
            "budget" -> {
                val amount = when (tag.id) {
                    "budget_50" -> 50.0
                    "budget_100" -> 100.0
                    else -> 150.0
                }
                budgetLimit = amount
                val budget = LocalProfileStore.loadBudget(context)
                LocalProfileStore.saveBudget(context, budget.copy(monthlyLimit = amount))
                LocalProfileStore.savePriceLimit(context, amount)
            }
        }
    }

    val selected = catalog.filter { isSelected(it) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Tap tags to toggle. Selected prefs appear below by group and sync to Fit, Plan, Discover, and Loomie.",
            color = SoftInk,
            style = MaterialTheme.typography.bodySmall
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            catalog.forEach { tag ->
                val on = isSelected(tag)
                val mode = preferences.userFilters.firstOrNull { it.filterId == tag.id }?.mode
                FilterChip(
                    selected = on,
                    onClick = { toggle(tag) },
                    label = {
                        Text(
                            buildString {
                                append(tag.label)
                                if (tag.kind == "filter" && on) {
                                    append(" · ")
                                    append(if (mode == FilterMode.PREFER) "Prefer" else "Must")
                                }
                                if (tag.kind == "color" && tag.label in colorAvoid) append(" · Avoid")
                                if (tag.kind == "color" && tag.label in colorLove) append(" · Love")
                            }
                        )
                    }
                )
            }
        }
        if (selected.isNotEmpty()) {
            Text("Selected", style = MaterialTheme.typography.titleMedium)
            selected.groupBy { it.group }.forEach { (group, tags) ->
                Text(group, color = Olive, style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    tags.forEach { tag ->
                        Text(
                            tag.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink,
                            modifier = Modifier
                                .background(OliveLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun PreferenceHubSection(
    context: android.content.Context,
    profileEpoch: Int = 0,
    preferences: WearabilityPreferences = WearabilityPreferences(),
    onPreferences: (WearabilityPreferences) -> Unit = {}
) {
    var zip by remember(profileEpoch) { mutableStateOf(LocalProfileStore.postalCode(context)) }
    var colorLove by remember(profileEpoch) { mutableStateOf(LocalProfileStore.preferredColors(context)) }
    var colorAvoid by remember(profileEpoch) { mutableStateOf(LocalProfileStore.avoidColors(context)) }
    var styles by remember(profileEpoch) {
        mutableStateOf(
            (LocalProfileStore.preferredStyles(context) + LocalProfileStore.interests(context)).distinct()
        )
    }
    val meta = remember(profileEpoch) { LocalProfileStore.personaMeta(context) }
    Column(
        Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("My preference", style = MaterialTheme.typography.titleMedium)
        Text(
            buildString {
                if (meta.age != null) append("${meta.age} · ")
                if (meta.occupation.isNotBlank()) append("${meta.occupation} · ")
                append("Gender, ZIP, colors and styles — clean dropdowns, no tag walls.")
            },
            color = SoftInk,
            style = MaterialTheme.typography.bodySmall
        )
        GenderSection(context, profileEpoch)
        TextField(
            value = zip,
            onValueChange = {
                zip = it.filter(Char::isDigit).take(5)
                if (zip.length == 5) LocalProfileStore.savePostalCode(context, zip)
            },
            label = { Text("ZIP / postal code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        SimpleDropdown(
            label = "Add preferred color",
            options = listOf("Choose…") + PersonaTagCatalog.colors.filter { it !in colorLove },
            selected = "Choose…",
            onSelect = { chosen ->
                if (chosen != "Choose…" && chosen !in colorLove) {
                    colorLove = colorLove + chosen
                    colorAvoid = colorAvoid.filterNot { it == chosen }
                    LocalProfileStore.savePreferredColors(context, colorLove)
                    LocalProfileStore.saveAvoidColors(context, colorAvoid)
                }
            }
        )
        if (colorLove.isNotEmpty()) {
            SimpleDropdown(
                label = "Remove preferred color",
                options = listOf("Keep all") + colorLove,
                selected = "Keep all",
                onSelect = { chosen ->
                    if (chosen != "Keep all") {
                        colorLove = colorLove.filterNot { it == chosen }
                        LocalProfileStore.savePreferredColors(context, colorLove)
                    }
                }
            )
            Text("Preferred colors: ${colorLove.joinToString()}", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        }
        SimpleDropdown(
            label = "Add avoid color",
            options = listOf("Choose…") + (PersonaTagCatalog.avoidColors + PersonaTagCatalog.colors)
                .distinct()
                .filter { it !in colorAvoid },
            selected = "Choose…",
            onSelect = { chosen ->
                if (chosen != "Choose…" && chosen !in colorAvoid) {
                    colorAvoid = colorAvoid + chosen
                    colorLove = colorLove.filterNot { it == chosen }
                    LocalProfileStore.saveAvoidColors(context, colorAvoid)
                    LocalProfileStore.savePreferredColors(context, colorLove)
                }
            }
        )
        if (colorAvoid.isNotEmpty()) {
            Text("Avoid colors: ${colorAvoid.joinToString()}", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        }
        SimpleDropdown(
            label = "Add style / interest",
            options = listOf("Choose…") + (PersonaTagCatalog.styles + PersonaTagCatalog.interests)
                .distinct()
                .filter { it !in styles },
            selected = "Choose…",
            onSelect = { chosen ->
                if (chosen != "Choose…" && chosen !in styles) {
                    styles = styles + chosen
                    LocalProfileStore.savePreferredStyles(context, styles)
                }
            }
        )
        if (styles.isNotEmpty()) {
            SimpleDropdown(
                label = "Remove style / interest",
                options = listOf("Keep all") + styles,
                selected = "Keep all",
                onSelect = { chosen ->
                    if (chosen != "Keep all") {
                        styles = styles.filterNot { it == chosen }
                        LocalProfileStore.savePreferredStyles(context, styles)
                    }
                }
            )
            Text("Styles: ${styles.joinToString()}", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        }
        SizeLogSection(context)
    }
}

@Composable
internal fun GenderSection(context: android.content.Context, profileEpoch: Int = 0) {
    var preset by remember(profileEpoch) {
        mutableStateOf(LocalProfileStore.genderPreset(context).ifBlank { GenderOptions.PREFER_NOT })
    }
    var custom by remember(profileEpoch) { mutableStateOf(LocalProfileStore.genderCustom(context)) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Gender", style = MaterialTheme.typography.titleMedium)
        Text("Used so discovery and Loomie suggestions match you — not mixed-gender noise.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        SimpleDropdown(
            label = "Gender",
            options = GenderOptions.presets,
            selected = preset.ifBlank { GenderOptions.PREFER_NOT },
            onSelect = {
                preset = it
                LocalProfileStore.saveGender(context, it, custom)
            }
        )
        if (preset == GenderOptions.CUSTOM) {
            TextField(
                value = custom,
                onValueChange = {
                    custom = it
                    LocalProfileStore.saveGender(context, GenderOptions.CUSTOM, it)
                },
                label = { Text("Custom gender") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
internal fun SizeLogSection(context: android.content.Context, showGender: Boolean = false) {
    var sizes by remember { mutableStateOf(LocalProfileStore.loadSizes(context)) }
    var editing by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(14.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showGender) {
            GenderSection(context, 0)
            Spacer(Modifier.height(4.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Size log", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = {
                if (editing) LocalProfileStore.saveSizes(context, sizes)
                editing = !editing
            }) { Text(if (editing) "Save sizes" else "Edit") }
        }
        Text("Tops, pants and shoes feed Loomie and discovery when useful.", color = SoftInk, style = MaterialTheme.typography.bodySmall)
        if (editing) {
            listOf(
                "Tops / blouses" to sizes.tops,
                "T-shirt" to sizes.tShirt,
                "Pants" to sizes.pants,
                "Shoes" to sizes.shoes,
                "Dress" to sizes.dress,
                "Outerwear" to sizes.outerwear
            ).forEach { (label, value) ->
                TextField(
                    value = value,
                    onValueChange = { next ->
                        sizes = when (label) {
                            "Tops / blouses" -> sizes.copy(tops = next)
                            "T-shirt" -> sizes.copy(tShirt = next)
                            "Pants" -> sizes.copy(pants = next)
                            "Shoes" -> sizes.copy(shoes = next)
                            "Dress" -> sizes.copy(dress = next)
                            else -> sizes.copy(outerwear = next)
                        }
                    },
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            TextField(
                value = sizes.notes,
                onValueChange = { sizes = sizes.copy(notes = it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val summary = sizes.summaryLine()
            if (summary.isBlank()) Text("No sizes saved yet.", color = SoftInk, style = MaterialTheme.typography.bodyMedium)
            else Text(summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ProfileAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Olive); Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(detail, color = SoftInk, style = MaterialTheme.typography.bodyMedium) }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = SoftInk)
    }
    HorizontalDivider(color = Hairline)
}
