package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder

class DuplicableTaskWithDependenciesTests {
    private val session = mutableMapOf<String, Any?>()
    private lateinit var journeyStateService: JourneyStateService

    // The typed contract a task reaches the enclosing state through.
    private interface PocDependencies {
        var sharedValue: String?
    }

    // Stands in for the enclosing journey/sibling state that satisfies the contract.
    private class EnclosingStateHolder : PocDependencies {
        override var sharedValue: String? = null
    }

    // A duplicable task that declares a dependency contract. makeSubJourney is never exercised here.
    private class PocTask(
        journeyStateService: JourneyStateService,
    ) : DuplicableTaskWithDependencies<JourneyState, PocDependencies>(journeyStateService) {
        override fun makeSubJourney(state: JourneyState): SubJourneyBuilder<*> = throw NotImplementedError("not needed for these tests")

        override val taskState: JourneyState
            get() = this
    }

    @BeforeEach
    fun setUp() {
        journeyStateService = mock()
        whenever(journeyStateService.getValue(any())).thenAnswer { session[it.getArgument<String>(0)] }
        doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(journeyStateService)
            .setValue(any(), anyOrNull())
    }

    @Test
    fun `reading a bound dependency reflects later mutations to the enclosing state`() {
        val enclosing = EnclosingStateHolder()
        val task = PocTask(journeyStateService).apply { bindDependencies(enclosing) }

        enclosing.sharedValue = "mutated-after-binding"

        assertEquals("mutated-after-binding", task.dependencies.sharedValue)
    }

    @Test
    fun `writing through a bound dependency updates the enclosing state`() {
        val enclosing = EnclosingStateHolder()
        val task = PocTask(journeyStateService).apply { bindDependencies(enclosing) }

        task.dependencies.sharedValue = "written-through-task"

        assertEquals("written-through-task", enclosing.sharedValue)
    }

    @Test
    fun `writing through a dependency does not create a task-route-scoped session key`() {
        val enclosing = EnclosingStateHolder()
        val task =
            PocTask(journeyStateService).apply {
                bindRoute("poc-route")
                bindDependencies(enclosing)
            }

        task.dependencies.sharedValue = "no-session-write"

        assertFalse(session.keys.any { it.contains("sharedValue") })
    }

    @Test
    fun `binding dependencies twice throws`() {
        val task = PocTask(journeyStateService).apply { bindDependencies(EnclosingStateHolder()) }

        assertThrows<JourneyInitialisationException> { task.bindDependencies(EnclosingStateHolder()) }
    }

    @Test
    fun `accessing dependencies before binding throws`() {
        val task = PocTask(journeyStateService)

        assertThrows<UninitializedPropertyAccessException> { task.dependencies }
    }

    @Test
    fun `a with-dependencies task requires binding`() {
        assertTrue(PocTask(journeyStateService).requiresDependencies)
    }

    @Test
    fun `a plain DuplicableTask does not require dependencies`() {
        val plain =
            object : DuplicableTask<JourneyState>(journeyStateService) {
                override fun makeSubJourney(state: JourneyState): SubJourneyBuilder<*> = throw NotImplementedError()

                override val taskState: JourneyState
                    get() = this
            }

        assertFalse(plain.requiresDependencies)
    }
}
