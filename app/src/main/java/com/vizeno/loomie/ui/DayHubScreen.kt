package com.vizeno.loomie.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.data.LocalProfileStore
import com.vizeno.loomie.model.CalendarEvent
import com.vizeno.loomie.model.ClothingBudget
import com.vizeno.loomie.model.ClothingPurchase
import com.vizeno.loomie.model.PlannedOutfit
import com.vizeno.loomie.model.RecurrenceRule
import com.vizeno.loomie.ui.theme.Hairline
import com.vizeno.loomie.ui.theme.Ink
import com.vizeno.loomie.ui.theme.Olive
import com.vizeno.loomie.ui.theme.OliveLight
import com.vizeno.loomie.ui.theme.Paper
import com.vizeno.loomie.ui.theme.Parchment
import com.vizeno.loomie.ui.theme.SoftInk
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import kotlin.math.min

private enum class DayHubPane { Calendar, Budget, Signals }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DayHubScreen(
    onAskLoomie: (String) -> Unit,
    onUseDayContext: (com.vizeno.loomie.integration.DayContext) -> Unit = {}
) {
    val context = LocalContext.current
    var budget by remember { mutableStateOf(LocalProfileStore.loadBudget(context)) }
    var events by remember { mutableStateOf(LocalProfileStore.loadEvents(context)) }
    var planned by remember { mutableStateOf(LocalProfileStore.loadPlannedOutfits(context)) }
    var priceLimitText by rememberSaveable {
        mutableStateOf(LocalProfileStore.priceLimit(context)?.toString().orEmpty())
    }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var pane by rememberSaveable { mutableStateOf(DayHubPane.Calendar.name) }
    var purchaseName by rememberSaveable { mutableStateOf("") }
    var purchaseAmount by rememberSaveable { mutableStateOf("") }
    var eventTitle by rememberSaveable { mutableStateOf("") }
    var eventRecurrence by rememberSaveable { mutableStateOf(RecurrenceRule.NONE.name) }
    var outfitOccasion by rememberSaveable { mutableStateOf("") }

    val monthStart = month.atDay(1).toEpochDay()
    val monthEnd = month.plusMonths(1).atDay(1).toEpochDay()
    val spent = budget.spentInMonth(monthStart, monthEnd)
    val remaining = budget.remaining(monthStart, monthEnd)
    val progress = if (budget.monthlyLimit <= 0) 0f else min(1f, (spent / budget.monthlyLimit).toFloat())
    val activePane = runCatching { DayHubPane.valueOf(pane) }.getOrDefault(DayHubPane.Calendar)

    fun eventsOn(day: LocalDate): List<CalendarEvent> = events.filter { event ->
        val base = LocalDate.ofEpochDay(event.dateEpochDay)
        when (event.recurrence) {
            RecurrenceRule.NONE -> base == day
            RecurrenceRule.WEEKLY -> base.dayOfWeek == day.dayOfWeek && !day.isBefore(base)
            RecurrenceRule.MONTHLY -> base.dayOfMonth == day.dayOfMonth && !day.isBefore(base)
        }
    }
    val dayEvents = eventsOn(selectedDay)
    val dayPlans = planned.filter { it.dateEpochDay == selectedDay.toEpochDay() } +
        dayEvents.filter { !it.outfitOccasion.isNullOrBlank() }.map {
            PlannedOutfit(it.id, selectedDay.toEpochDay(), it.outfitOccasion!!, it.garmentIds, it.notes)
        }

    Column(
        Modifier
            .fillMaxSize()
            .background(Parchment)
            .statusBarsPadding()
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text("DAY HUB", style = MaterialTheme.typography.labelSmall, color = SoftInk)
            Text("Plan the day", style = MaterialTheme.typography.headlineMedium, color = Ink)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DayHubPane.entries.forEach { option ->
                    val selected = activePane == option
                    FilterChip(
                        selected = selected,
                        onClick = { pane = option.name },
                        label = {
                            Text(
                                when (option) {
                                    DayHubPane.Calendar -> "Calendar"
                                    DayHubPane.Budget -> "Budget"
                                    DayHubPane.Signals -> "Day Signals"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Olive,
                            selectedLabelColor = Paper,
                            containerColor = Paper,
                            labelColor = Ink
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = Hairline,
                            selectedBorderColor = Olive
                        )
                    )
                }
            }
        }

        // Fixed stage: tabs swap content in place; page itself does not scroll calendar+budget together.
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 12.dp)
        ) {
            when (activePane) {
                DayHubPane.Calendar -> DayCalendarPane(
                    month = month,
                    selectedDay = selectedDay,
                    events = events,
                    dayEvents = dayEvents,
                    dayPlans = dayPlans,
                    eventTitle = eventTitle,
                    eventRecurrence = eventRecurrence,
                    outfitOccasion = outfitOccasion,
                    remaining = remaining,
                    budget = budget,
                    onMonth = { month = it },
                    onSelectDay = { selectedDay = it },
                    onEventTitle = { eventTitle = it },
                    onEventRecurrence = { eventRecurrence = it },
                    onOutfitOccasion = { outfitOccasion = it },
                    onAddEvent = {
                        if (eventTitle.isBlank()) return@DayCalendarPane
                        val next = events + CalendarEvent(
                            id = UUID.randomUUID().toString(),
                            title = eventTitle.trim(),
                            dateEpochDay = selectedDay.toEpochDay(),
                            recurrence = runCatching { RecurrenceRule.valueOf(eventRecurrence) }.getOrDefault(RecurrenceRule.NONE),
                            outfitOccasion = outfitOccasion.takeIf { it.isNotBlank() }
                        )
                        events = next
                        LocalProfileStore.saveEvents(context, next)
                        eventTitle = ""
                    },
                    onSaveOutfit = {
                        if (outfitOccasion.isBlank()) return@DayCalendarPane
                        val next = planned + PlannedOutfit(
                            eventId = dayEvents.firstOrNull()?.id,
                            dateEpochDay = selectedDay.toEpochDay(),
                            occasion = outfitOccasion.trim()
                        )
                        planned = next
                        LocalProfileStore.savePlannedOutfits(context, next)
                    },
                    onAskLoomie = {
                        val notes = buildString {
                            append("Budget remaining this month: $${String.format("%.0f", remaining)} ${budget.currency}. ")
                            LocalProfileStore.priceLimit(context)?.let { append("Shopping price limit: $$it. ") }
                            append("Selected day: $selectedDay. ")
                            if (dayEvents.isNotEmpty()) append("Events: ${dayEvents.joinToString { it.title }}. ")
                            if (dayPlans.isNotEmpty()) append("Planned outfits: ${dayPlans.joinToString { it.occasion }}.")
                        }
                        onAskLoomie(notes)
                    }
                )

                DayHubPane.Budget -> DayBudgetPane(
                    budget = budget,
                    spent = spent,
                    remaining = remaining,
                    progress = progress,
                    monthStart = monthStart,
                    monthEnd = monthEnd,
                    purchaseName = purchaseName,
                    purchaseAmount = purchaseAmount,
                    priceLimitText = priceLimitText,
                    onBudgetLimit = { limit ->
                        budget = budget.copy(monthlyLimit = limit)
                        LocalProfileStore.saveBudget(context, budget)
                    },
                    onPurchaseName = { purchaseName = it },
                    onPurchaseAmount = { purchaseAmount = it },
                    onAddPurchase = {
                        val amount = purchaseAmount.toDoubleOrNull() ?: return@DayBudgetPane
                        if (purchaseName.isBlank()) return@DayBudgetPane
                        val next = budget.copy(
                            purchases = budget.purchases + ClothingPurchase(
                                id = UUID.randomUUID().toString(),
                                name = purchaseName.trim(),
                                amount = amount,
                                dateEpochDay = selectedDay.toEpochDay()
                            )
                        )
                        budget = next
                        LocalProfileStore.saveBudget(context, next)
                        purchaseName = ""
                        purchaseAmount = ""
                    },
                    onPriceLimit = {
                        priceLimitText = it
                        LocalProfileStore.savePriceLimit(context, it.toDoubleOrNull())
                    }
                )

                DayHubPane.Signals -> DaySignalsScreen(
                    onBack = null,
                    onUse = onUseDayContext
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayCalendarPane(
    month: YearMonth,
    selectedDay: LocalDate,
    events: List<CalendarEvent>,
    dayEvents: List<CalendarEvent>,
    dayPlans: List<PlannedOutfit>,
    eventTitle: String,
    eventRecurrence: String,
    outfitOccasion: String,
    remaining: Double,
    budget: ClothingBudget,
    onMonth: (YearMonth) -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    onEventTitle: (String) -> Unit,
    onEventRecurrence: (String) -> Unit,
    onOutfitOccasion: (String) -> Unit,
    onAddEvent: () -> Unit,
    onSaveOutfit: () -> Unit,
    onAskLoomie: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ${month.year}",
                style = MaterialTheme.typography.titleLarge
            )
            Row {
                Text(
                    modifier = Modifier.clickable { onMonth(month.minusMonths(1)) }.padding(8.dp),
                    text = "Prev",
                    color = Olive
                )
                Text(
                    modifier = Modifier.clickable { onMonth(month.plusMonths(1)) }.padding(8.dp),
                    text = "Next",
                    color = Olive
                )
            }
        }
        MonthGrid(month, selectedDay, events, onSelectDay)
        Spacer(Modifier.height(10.dp))
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                selectedDay.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
                style = MaterialTheme.typography.titleMedium
            )
            if (dayEvents.isEmpty()) Text("No events on this day.", color = SoftInk)
            dayEvents.forEach { event ->
                Column(Modifier.fillMaxWidth().background(OliveLight, RoundedCornerShape(12.dp)).padding(12.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        buildString {
                            append(event.recurrence.name.lowercase().replaceFirstChar { it.uppercase() })
                            if (event.notes.isNotBlank()) append(" · ${event.notes}")
                        },
                        color = SoftInk,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            TextField(
                eventTitle,
                onEventTitle,
                Modifier.fillMaxWidth(),
                label = { Text("Event title") },
                singleLine = true
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecurrenceRule.entries.forEach { rule ->
                    FilterChip(
                        selected = eventRecurrence == rule.name,
                        onClick = { onEventRecurrence(rule.name) },
                        label = { Text(rule.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
            OutlinedButton(onClick = onAddEvent, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add event")
            }
            Text("Planned outfits", style = MaterialTheme.typography.titleMedium)
            TextField(
                outfitOccasion,
                onOutfitOccasion,
                Modifier.fillMaxWidth(),
                label = { Text("Occasion for this day") },
                singleLine = true
            )
            OutlinedButton(onClick = onSaveOutfit, modifier = Modifier.fillMaxWidth()) {
                Text("Save planned outfit")
            }
            if (dayPlans.isEmpty()) Text("No planned outfits for this day.", color = SoftInk)
            dayPlans.distinctBy { it.occasion + it.dateEpochDay }.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Event, null, tint = Olive)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.occasion, style = MaterialTheme.typography.titleMedium)
                        if (item.note.isNotBlank()) Text(item.note, color = SoftInk)
                    }
                }
            }
            Button(onClick = onAskLoomie, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.ChatBubbleOutline, null)
                Spacer(Modifier.width(8.dp))
                Text("Ask Loomie about this day")
            }
            Text(
                "$${String.format("%.0f", remaining)} ${budget.currency} left this month",
                color = SoftInk,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun DayBudgetPane(
    budget: ClothingBudget,
    spent: Double,
    remaining: Double,
    progress: Float,
    monthStart: Long,
    monthEnd: Long,
    purchaseName: String,
    purchaseAmount: String,
    priceLimitText: String,
    onBudgetLimit: (Double) -> Unit,
    onPurchaseName: (String) -> Unit,
    onPurchaseAmount: (String) -> Unit,
    onAddPurchase: () -> Unit,
    onPriceLimit: (String) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "budget-ring"
    )
    val remainingShare = (1f - animatedProgress).coerceIn(0f, 1f)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(OliveLight, CircleShape)
                    .border(1.dp, Hairline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AttachMoney, contentDescription = null, tint = Olive)
            }
            Column {
                Text("Day Budget", style = MaterialTheme.typography.titleLarge, color = Ink)
                Text("Clothing spend this month", color = SoftInk, style = MaterialTheme.typography.bodySmall)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(Paper, RoundedCornerShape(16.dp))
                .border(1.dp, Hairline, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = Stroke(11.dp.toPx(), cap = StrokeCap.Round)
                        drawArc(OliveLight, -90f, 360f, false, style = stroke)
                        // Remaining ring grows as budget is preserved.
                        drawArc(Olive, -90f, 360f * remainingShare, false, style = stroke)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$${String.format("%.0f", remaining)}", style = MaterialTheme.typography.titleMedium, color = Olive)
                        Text("left", style = MaterialTheme.typography.labelSmall, color = SoftInk)
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${(remainingShare * 100).toInt()}% remaining",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink
                    )
                    Text(
                        "$${String.format("%.0f", spent)} of $${String.format("%.0f", budget.monthlyLimit)} ${budget.currency} used",
                        color = SoftInk,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        TextField(
            value = budget.monthlyLimit.toInt().toString(),
            onValueChange = { value ->
                val limit = value.toDoubleOrNull() ?: return@TextField
                onBudgetLimit(limit)
            },
            label = { Text("Monthly limit") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text("Purchases this month", style = MaterialTheme.typography.titleMedium)
        budget.purchases.filter { it.dateEpochDay in monthStart until monthEnd }.takeLast(8).reversed().forEach { purchase ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(purchase.name, modifier = Modifier.weight(1f))
                Text("$${String.format("%.2f", purchase.amount)}", color = Olive)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                purchaseName,
                onPurchaseName,
                Modifier.weight(1f),
                label = { Text("Purchase") },
                singleLine = true
            )
            TextField(
                purchaseAmount,
                onPurchaseAmount,
                Modifier.width(100.dp),
                label = { Text("$") },
                singleLine = true
            )
        }
        OutlinedButton(onClick = onAddPurchase, modifier = Modifier.fillMaxWidth()) {
            Text("Add purchase")
        }
        Text("Shopping price limit", style = MaterialTheme.typography.titleMedium)
        Text(
            "Feeds discovery and Loomie as a soft/hard shopping ceiling.",
            color = SoftInk,
            style = MaterialTheme.typography.bodyMedium
        )
        TextField(
            value = priceLimitText,
            onValueChange = onPriceLimit,
            label = { Text("Max price (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    events: List<CalendarEvent>,
    onSelect: (LocalDate) -> Unit
) {
    val first = month.atDay(1)
    val shift = ((first.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7
    val cells = buildList {
        repeat(shift) { add(null) }
        for (day in 1..month.lengthOfMonth()) add(month.atDay(day))
    }
    Column(Modifier.fillMaxWidth().background(Paper, RoundedCornerShape(14.dp)).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, Modifier.weight(1f), color = SoftInk, style = MaterialTheme.typography.labelSmall)
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                            .then(
                                if (day != null) Modifier
                                    .background(
                                        when {
                                            day == selected -> Olive
                                            events.any { matchesRecurring(it, day) } -> OliveLight
                                            else -> ColorTransparent
                                        },
                                        CircleShape
                                    )
                                    .clickable { onSelect(day) }
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            Text(
                                day.dayOfMonth.toString(),
                                color = if (day == selected) Paper else Ink,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                repeat(7 - week.size) { Box(Modifier.weight(1f).aspectRatio(1f)) }
            }
        }
    }
}

private val ColorTransparent = androidx.compose.ui.graphics.Color.Transparent

private fun matchesRecurring(event: CalendarEvent, day: LocalDate): Boolean {
    val base = LocalDate.ofEpochDay(event.dateEpochDay)
    return when (event.recurrence) {
        RecurrenceRule.NONE -> base == day
        RecurrenceRule.WEEKLY -> base.dayOfWeek == day.dayOfWeek && !day.isBefore(base)
        RecurrenceRule.MONTHLY -> base.dayOfMonth == day.dayOfMonth && !day.isBefore(base)
    }
}
