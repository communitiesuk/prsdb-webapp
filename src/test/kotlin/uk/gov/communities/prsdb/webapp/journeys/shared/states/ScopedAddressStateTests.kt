package uk.gov.communities.prsdb.webapp.journeys.shared.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService

// Demonstrates that two instances of the address task - identified only by their route
// ("lead-trustee-address" vs "org-address") - read and write completely separate state, even though they share
// the same journey and the same underlying storage. This is what lets the same routed task be added more than once.
class ScopedAddressStateTests {
    private val variableStore = mutableMapOf<String, Any?>()
    private val stepDataStore = mutableMapOf<String, FormData>()
    private lateinit var journeyStateService: JourneyStateService
    private lateinit var delegate: JourneyState

    @BeforeEach
    fun setUp() {
        // Back the delegate's step-data storage and the service's variable storage with real maps so we can
        // observe exactly which keys each scoped instance reads and writes.
        journeyStateService = mock()
        whenever(journeyStateService.getValue(any())).thenAnswer { variableStore[it.getArgument<String>(0)] }
        doAnswer { variableStore[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(journeyStateService)
            .setValue(any(), anyOrNull())

        delegate = mock()
        whenever(delegate.getStepData(any())).thenAnswer { stepDataStore[it.getArgument<String>(0)] }
        doAnswer { stepDataStore[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(delegate)
            .addStepData(any(), any())
    }

    private fun scopedStateFor(route: String) = ScopedAddressState(route, journeyStateService, delegate, mock(), mock(), mock(), mock())

    @Test
    fun `two instances of the address task keep their variables separate`() {
        val leadTrusteeAddress = scopedStateFor("lead-trustee-address")
        val orgAddress = scopedStateFor("org-address")

        leadTrusteeAddress.cachedSelectedAddress = "10 Downing Street"

        assertEquals("10 Downing Street", leadTrusteeAddress.cachedSelectedAddress)
        assertNull(orgAddress.cachedSelectedAddress)
    }

    @Test
    fun `two instances of the address task keep their step data separate`() {
        val leadTrusteeAddress = scopedStateFor("lead-trustee-address")
        val orgAddress = scopedStateFor("org-address")

        leadTrusteeAddress.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

        assertEquals("SW1A 2AA", leadTrusteeAddress.getStepData("lookup-address")?.get("postcode"))
        assertNull(orgAddress.getStepData("lookup-address"))
    }

    @Test
    fun `address keys are prefixed with the instance route`() {
        val leadTrusteeAddress = scopedStateFor("lead-trustee-address")

        leadTrusteeAddress.cachedSelectedAddress = "10 Downing Street"
        leadTrusteeAddress.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

        assertTrue(variableStore.containsKey("lead-trustee-address/cachedSelectedAddress"))
        assertTrue(stepDataStore.containsKey("lead-trustee-address/lookup-address"))
    }
}
