package uk.gov.communities.prsdb.webapp.journeyMap.server

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeyMap.introspection.IntrospectedDestination
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import kotlin.test.assertEquals

class JourneyIntrospectorTests {
    private class TestStepConfig : AbstractInternalStepConfig<TestEnum, JourneyState>() {
        override fun mode(state: JourneyState) = TestEnum.ENUM_VALUE
    }

    private class RoutingStep : JourneyStep.InternalStep<TestEnum, JourneyState>(TestStepConfig())

    private class SavingStep : JourneyStep.InternalStep<TestEnum, JourneyState>(TestStepConfig())

    @Test
    fun `steps that are only reached through the destination of another step are included`() {
        val saving = SavingStep().also { initialise(it) { Destination.ExternalUrl("confirmation") } }
        val routing =
            RoutingStep().also { step ->
                initialise(step) { mode ->
                    if (mode == TestEnum.ALTERNATIVE_VALUE) Destination(saving) else Destination.ExternalUrl("elsewhere")
                }
            }

        val structure = JourneyIntrospector(null).introspect("routing", routingMap(routing), null)

        assertEquals(listOf("internal:RoutingStep", "internal:SavingStep"), structure.steps.map { it.id })
    }

    @Test
    fun `where each outcome of a step leads is recorded`() {
        val saving = SavingStep().also { initialise(it) { Destination.ExternalUrl("/confirmation") } }
        val routing =
            RoutingStep().also { step ->
                initialise(step) { mode ->
                    if (mode == TestEnum.ALTERNATIVE_VALUE) Destination(saving) else Destination.ExternalUrl("/elsewhere?journeyId=1")
                }
            }

        val structure = JourneyIntrospector(null).introspect("routing", routingMap(routing), null)

        assertEquals(
            listOf(
                IntrospectedDestination("ENUM_VALUE", url = "/elsewhere"),
                IntrospectedDestination("ALTERNATIVE_VALUE", step = "internal:SavingStep"),
            ),
            structure.stepById("internal:RoutingStep")?.destinations,
        )
    }

    @Test
    fun `journey state that routing code reads besides the outcome is recorded with the destination`() {
        val reads = mutableListOf<String>()
        val introspector =
            JourneyIntrospector(null) { block ->
                val start = reads.size
                block()
                reads.drop(start).toSet()
            }
        val routing =
            RoutingStep().also { step ->
                initialise(step) { mode ->
                    if (mode == TestEnum.ALTERNATIVE_VALUE) reads += "step:occupied"
                    Destination.ExternalUrl("/elsewhere")
                }
            }

        val structure = introspector.introspect("routing", routingMap(routing), null)

        assertEquals(
            listOf(emptyList(), listOf("step:occupied")),
            structure.stepById("internal:RoutingStep")?.destinations?.map { it.reads },
        )
    }

    private fun routingMap(step: JourneyStep<*, *, *>) = mapOf("routing" to RedirectingStepLifecycleOrchestrator(step))

    private fun initialise(
        step: JourneyStep.InternalStep<TestEnum, JourneyState>,
        destination: (TestEnum) -> Destination,
    ) = step.initialize(null, mock(), null, destination, NoParents(), { Destination.ExternalUrl("unreachable") }, false)
}
