package com.vizeno.loomie

import com.vizeno.loomie.data.LookWeaveRepository
import com.vizeno.loomie.domain.ConstraintSolver
import com.vizeno.loomie.model.WearabilityPreferences
import com.vizeno.loomie.model.WearabilityRule
import com.vizeno.loomie.model.WearabilityRuleKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstraintSolverTest {
    @Test fun everyConflictIsResolvedWhenMaterialAndClosureNeedsCombine() {
        val preferences = WearabilityPreferences(animalFree = true)
        val plan = ConstraintSolver.plan("Dinner", preferences)
        assertTrue(plan.resolved.all { ConstraintSolver.isFeasible(it, preferences) })
        assertFalse(plan.resolved.any { it.fabric.value.contains("leather", true) })
        assertEquals(5, plan.resolved.size)
        assertEquals(3, plan.preservedCount)
    }

    @Test fun missingAlternativeIsReportedWithoutKeepingBlockedGarment() {
        val wardrobe = LookWeaveRepository.garments.filter { it.id != "g10" }
        val plan = ConstraintSolver.plan("Dinner", WearabilityPreferences(animalFree = true), wardrobe)
        assertTrue(plan.unresolved.isNotEmpty())
        assertEquals(0, plan.score)
        assertFalse(plan.resolved.any { it.id == "g4" })
    }
    @Test fun hardClosureConstraintNeverLeaksIntoResult() {
        val plan = ConstraintSolver.plan("Gallery opening", WearabilityPreferences(oneHandClosure = true))
        assertFalse(plan.resolved.any { !it.oneHandFriendly })
        assertEquals("g6", plan.replaced?.id)
        assertEquals("g2", plan.replacement?.id)
    }

    @Test fun minimalChangePreservesAllUnaffectedPieces() {
        val plan = ConstraintSolver.plan("Gallery opening", WearabilityPreferences(oneHandClosure = true))
        assertEquals(4, plan.preservedCount)
        assertEquals(5, plan.resolved.size)
    }

    @Test fun disablingClosureConstraintKeepsOriginalLook() {
        val preferences = WearabilityPreferences(oneHandClosure = false, seatedComfort = false, easyFootwear = false)
        val plan = ConstraintSolver.plan("Dinner", preferences)
        assertTrue(plan.replaced == null)
        assertEquals(plan.original.map { it.id }, plan.resolved.map { it.id })
    }

    @Test fun unavailableGarmentsAreNeverFeasible() {
        val unavailable = LookWeaveRepository.byId("g2").copy(available = false)
        assertFalse(ConstraintSolver.isFeasible(unavailable, WearabilityPreferences()))
    }

        @Test fun expandedNeedsExplainTheChangedLayerAndUnknownEvidence() {
            val plan = ConstraintSolver.plan("Dinner", WearabilityPreferences(limitedOverheadReach = true, gripStrength = true))
            assertEquals("g6", plan.replaced?.id)
            assertTrue(plan.triggeredRules.any { it.contains("Overhead") || it.contains("Grip") })
            assertTrue(plan.unverifiable.isNotEmpty())
        }

        @Test fun unconfirmedCustomRuleDoesNotAffectTheSolver() {
            val draft = WearabilityRule("rule-1", "Easy removal", WearabilityRuleKey.EASY_REMOVAL, confirmed = false)
            val draftPlan = ConstraintSolver.plan("Dinner", WearabilityPreferences(oneHandClosure = false, seatedComfort = false, easyFootwear = false, customRules = listOf(draft)))
            assertTrue(draftPlan.replaced == null)
            val confirmedPlan = ConstraintSolver.plan("Dinner", WearabilityPreferences(oneHandClosure = false, seatedComfort = false, easyFootwear = false, customRules = listOf(draft.copy(confirmed = true))))
            assertTrue(confirmedPlan.triggeredRules.contains("Easy removal"))
        }

    @Test fun unknownFilterEvidenceNeverBlocksOrRankBoosts() {
        val prefs = WearabilityPreferences(
            oneHandClosure = false,
            seatedComfort = false,
            easyFootwear = false,
            userFilters = listOf(
                com.vizeno.loomie.model.UserFilter("sn_no_stiff_collar", com.vizeno.loomie.model.FilterMode.MUST_AVOID),
                com.vizeno.loomie.model.UserFilter("clm_heat", com.vizeno.loomie.model.FilterMode.PREFER)
            )
        )
        val garment = LookWeaveRepository.byId("g1")
        val feasibility = ConstraintSolver.feasibility(garment, prefs, "Dinner")
        assertTrue(feasibility.allowed)
        assertTrue(feasibility.unverifiable.any { it.contains("Unknown", true) || it.contains("evidence", true) })
        assertFalse(feasibility.reasons.any { it.contains("stiff collar", true) || it.contains("Heat", true) })
    }

    @Test fun suggestDailyLooksReturnsFiveDistinctCombinations() {
        val looks = ConstraintSolver.suggestDailyLooks(
            preferences = WearabilityPreferences(oneHandClosure = true, seatedComfort = true, easyFootwear = true),
            dayNotes = listOf("High: 62°F"),
            eventTitles = listOf("Gallery opening"),
            gender = "Male",
            count = 5
        )
        assertEquals(5, looks.size)
        assertEquals(5, looks.map { it.plan.resolved.map { g -> g.id }.sorted() }.toSet().size)
        assertTrue(looks.all { it.reasons.isNotEmpty() })
        assertTrue(looks.any { look -> look.reasons.any { it.contains("Gallery", true) || it.contains("62", true) || it.contains("Male", true) } })
        looks.forEach { look ->
            assertTrue(look.plan.resolved.any { it.category == com.vizeno.loomie.model.GarmentCategory.TOP })
            assertTrue(look.plan.resolved.any { it.category == com.vizeno.loomie.model.GarmentCategory.BOTTOM })
            assertTrue(look.plan.resolved.any { it.category == com.vizeno.loomie.model.GarmentCategory.SHOES })
        }
    }

    @Test fun nonBinaryIdentityDoesNotCollapseWardrobeToUnisexOnly() {
        val plan = ConstraintSolver.plan(
            occasion = "Dinner",
            preferences = WearabilityPreferences(oneHandClosure = false, seatedComfort = false, easyFootwear = false),
            gender = "Non-binary"
        )
        assertEquals(5, plan.resolved.size)
        assertTrue(plan.resolved.any { it.audience != com.vizeno.loomie.model.GarmentAudience.UNISEX })
    }
}
