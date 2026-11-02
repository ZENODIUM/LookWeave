package com.vizeno.loomie

import com.vizeno.loomie.data.RetailerDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetailerDirectoryTest {
    @Test
    fun directory_hasBroadVerifiedCoverage() {
        assertEquals(15, RetailerDirectory.retailers.size)
        RetailerDirectory.filters.forEach { feature ->
            assertTrue("No retailer covers $feature", RetailerDirectory.retailers.any { feature in it.features })
        }
    }

    @Test
    fun directory_entriesAreCompleteAndUnique() {
        val stores = RetailerDirectory.retailers
        assertEquals(stores.size, stores.map { it.id }.distinct().size)
        assertEquals(stores.size, stores.map { it.shopUrl }.distinct().size)
        assertTrue(stores.all { it.name.isNotBlank() && it.description.isNotBlank() && it.notableFinds.size >= 3 })
        assertTrue(stores.all { it.shopUrl.startsWith("https://") })
    }
}
