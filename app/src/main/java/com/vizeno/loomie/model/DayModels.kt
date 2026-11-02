package com.vizeno.loomie.model

data class ClothingPurchase(
    val id: String,
    val name: String,
    val amount: Double,
    val dateEpochDay: Long,
    val garmentId: String? = null
)

data class ClothingBudget(
    val monthlyLimit: Double = 200.0,
    val currency: String = "USD",
    val purchases: List<ClothingPurchase> = emptyList()
) {
    fun spentInMonth(yearMonthEpochDayStart: Long, yearMonthEpochDayEndExclusive: Long): Double =
        purchases.filter { it.dateEpochDay in yearMonthEpochDayStart until yearMonthEpochDayEndExclusive }.sumOf { it.amount }

    fun remaining(yearMonthEpochDayStart: Long, yearMonthEpochDayEndExclusive: Long): Double =
        (monthlyLimit - spentInMonth(yearMonthEpochDayStart, yearMonthEpochDayEndExclusive)).coerceAtLeast(0.0)
}

enum class RecurrenceRule { NONE, WEEKLY, MONTHLY }

data class CalendarEvent(
    val id: String,
    val title: String,
    val dateEpochDay: Long,
    val recurrence: RecurrenceRule = RecurrenceRule.NONE,
    val notes: String = "",
    val garmentIds: List<String> = emptyList(),
    val outfitOccasion: String? = null
)

data class PlannedOutfit(
    val eventId: String? = null,
    val dateEpochDay: Long,
    val occasion: String,
    val garmentIds: List<String> = emptyList(),
    val note: String = ""
)
