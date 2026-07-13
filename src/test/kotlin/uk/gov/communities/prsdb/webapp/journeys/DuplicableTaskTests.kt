package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

class DuplicableTaskTests {
    private val session = mutableMapOf<String, Any?>()
    private lateinit var journeyStateService: JourneyStateService

    @BeforeEach
    fun setUp() {
        // Back getValue/setValue with a real map so we can observe exactly which keys the cached-variable delegates
        // read and write.
        journeyStateService = mock()
        whenever(journeyStateService.getValue(any())).thenAnswer { session[it.getArgument<String>(0)] }
        doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(journeyStateService)
            .setValue(any(), anyOrNull())
    }

    private fun taskFor(route: String?): TestTask = TestTask(journeyStateService).apply { bindRoute(route) }

    @Test
    fun `a bound route prefixes the cached variable key`() {
        val task = taskFor("some-route")

        task.cachedThing = "10 Downing Street"

        assertTrue(session.containsKey("some-route/cachedThing"))
    }

    @Test
    fun `a null route leaves the cached variable key bare`() {
        val task = taskFor(null)

        task.cachedThing = "10 Downing Street"

        assertTrue(session.containsKey("cachedThing"))
    }

    @Test
    fun `two instances on different routes keep their cached variables separate`() {
        val leadTrusteeAddress = taskFor("lead-trustee-address")
        val ownAddress = taskFor(null)

        leadTrusteeAddress.cachedThing = "10 Downing Street"

        assertEquals("10 Downing Street", leadTrusteeAddress.cachedThing)
        assertNull(ownAddress.cachedThing)
    }

    @Test
    fun `addStepData stores under a bare key even when a route is bound`() {
        val task = taskFor("some-route")

        task.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

        // The route must NOT scope step data - it is delegated straight to the service under the bare key.
        verify(journeyStateService).addSingleStepData(eq("lookup-address"), any())
    }

    @Test
    fun `getStepData reads a bare key even when a route is bound`() {
        whenever(journeyStateService.getSubmittedStepData())
            .thenReturn(mapOf("lookup-address" to mapOf("postcode" to "SW1A 2AA")))
        val task = taskFor("some-route")

        assertEquals("SW1A 2AA", task.getStepData("lookup-address")?.get("postcode"))
        assertNull(task.getStepData("some-route/lookup-address"))
    }

    @Test
    fun `getSubmittedStepData passes through unscoped`() {
        val stepData = mapOf("lookup-address" to mapOf("postcode" to "SW1A 2AA"))
        whenever(journeyStateService.getSubmittedStepData()).thenReturn(stepData)
        val task = taskFor("some-route")

        assertEquals(stepData, task.getSubmittedStepData())
    }

    @Test
    fun `bindKeyRegistry registers the task's cached variable key in its route-scoped form`() {
        val registry = DelegateKeyRegistry()
        val task = taskFor("some-route")

        task.bindKeyRegistry(registry)

        // The registry now owns the route-scoped key, so re-registering it collides.
        assertThrows<JourneyInitialisationException> { registry.register("some-route/cachedThing") }
    }

    @Test
    fun `bindKeyRegistry registers a bare cached variable key when no route is bound`() {
        val registry = DelegateKeyRegistry()
        val task = taskFor(null)

        task.bindKeyRegistry(registry)

        assertThrows<JourneyInitialisationException> { registry.register("cachedThing") }
    }

    // Minimal concrete SelfStatedRoutableTask exposing a single route-scoped cached variable. makeSubJourney is
    // never exercised by these tests, which target the route-scoping and state-delegation behaviour only.
    private class TestTask(
        journeyStateService: JourneyStateService,
    ) : DuplicableTask<JourneyState>(journeyStateService) {
        var cachedThing: String? by delegateProvider.nullableDelegate("cachedThing")

        override fun makeSubJourney(state: JourneyState): SubJourneyBuilder<*> = throw NotImplementedError("not needed for these tests")

        override val taskState: JourneyState
            get() = this
    }
}
