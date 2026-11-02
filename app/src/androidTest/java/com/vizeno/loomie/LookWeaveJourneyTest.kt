package com.vizeno.loomie

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test

class LookWeaveJourneyTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun styleStudio_savesChoicesAndUpdatesAccessoryRecommendations() {
        enterIfNeeded()
        compose.onNodeWithContentDescription("Open profile").performClick()
        compose.onNodeWithText("Colours, face proportions & accessories").performClick()
        compose.onNodeWithText("Analyse selected image").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("style-studio-list").performScrollToNode(hasText("Cool"))
        compose.onNodeWithText("Cool").performScrollTo().performClick()
        compose.onNodeWithText("Silver").performScrollTo().performClick()
        compose.onNodeWithText("Square").performScrollTo().performClick()
        compose.onNodeWithText("Save style preferences").performScrollTo().performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodes(hasText("Style preferences saved. Finding real accessory matches…", substring = true)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("Style preferences saved. Your recommendations are updated.")).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("Find similar glasses", substring = true)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("Finding live matches", substring = true)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("Accessory subcategory")).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("Real eyeglasses & sunglasses")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithTag("style-studio-list").performScrollToNode(hasText("Accessory subcategory", substring = true))
        compose.onNodeWithText("Accessory subcategory", substring = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Glasses").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Watches").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun wearAndGo_usesExternalToolsAndShowsDecisionTrace() {
        enterIfNeeded()
        compose.onNodeWithText("Agent").performClick()
        compose.onNodeWithText("US ZIP code").performScrollTo().performTextInput("11217")
        compose.onNode(hasText("Plan my outing") and hasClickAction()).performScrollTo().performClick()
        compose.waitUntil(180_000) {
            compose.onAllNodes(hasText("How this was decided")).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("How this was decided").performScrollTo().assertIsDisplayed()
    }

    private fun enterIfNeeded() {
        val entry = compose.onAllNodes(hasText("Enter LookWeave")).fetchSemanticsNodes()
        if (entry.isNotEmpty()) compose.onNodeWithText("Enter LookWeave").performClick()
        compose.waitForIdle()
    }

    @Test
    fun winningJourney_replansAndOpensVirtualStudio() {
        enterIfNeeded()
        compose.onNodeWithText("Plan").performClick()
        compose.onNodeWithText("Whole-look solver", ignoreCase = true).assertIsDisplayed()
        compose.onNodeWithText("Weave with 3 priorities").performClick()
        compose.onNodeWithText("All priorities met · 5 pieces").assertIsDisplayed()
        compose.onNodeWithText("Preview\non you").performClick()
        compose.onNodeWithText("Your photo").assertIsDisplayed()
        compose.onNodeWithText("Garment ready").assertIsDisplayed()
        compose.onNodeWithText("Generate preview").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun wearabilityLab_recordsComfortObservation() {
        enterIfNeeded()
        compose.onNodeWithText("Lab").performClick()
        compose.onNodeWithText("Difficult").performScrollTo().performClick()
        compose.onNodeWithText("Add to my profile").performScrollTo().performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodes(hasContentDescription("Observation saved")).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun wardrobe_savesNearbyPlaceAndOpensInteractiveMap() {
        enterIfNeeded()
        compose.onNodeWithText("Wardrobe").performClick()
        compose.onNodeWithText("Adaptive store directory").assertIsDisplayed().performClick()
        compose.onNodeWithText("WEARABILITY MATCHES").assertIsDisplayed()
        compose.waitUntil(90_000) {
            compose.onAllNodes(hasContentDescription("Save place")).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodes(hasText("See your places together")).fetchSemanticsNodes().isNotEmpty()
        }
        if (compose.onAllNodes(hasContentDescription("Save place")).fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithContentDescription("Save place").assertIsDisplayed().performClick()
            compose.waitForIdle()
            compose.onNode(hasText("Map") and hasClickAction()).performClick()
        } else {
            compose.onNodeWithText("Open map").performClick()
        }
        compose.onNodeWithText("ACCESS MAP").assertIsDisplayed()
        compose.onNodeWithText("Directions").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun primaryDestinations_renderWithAccessibleAgentControl() {
        enterIfNeeded()
        compose.onNodeWithText("Wardrobe").performClick()
        compose.onNodeWithText("Your wardrobe").assertIsDisplayed()
        compose.onNodeWithText("Plan").performClick()
        compose.onNodeWithText("Plan a look").assertIsDisplayed()
        compose.onNodeWithText("Lab").performClick()
        compose.onNodeWithText("Wearability Lab").assertIsDisplayed()
        compose.onNodeWithText("Agent").performClick()
        compose.onNodeWithText("Ask LookWeave").assertIsDisplayed()
        compose.onNodeWithContentDescription("Start voice conversation").performScrollTo().assertIsDisplayed()
    }
}
