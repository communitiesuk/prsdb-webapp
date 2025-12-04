package uk.gov.communities.prsdb.webapp.journeys.shared

import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiserTests
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersPartialJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersPartialJourneyState.Companion.checkable

class CheckYourAnswersJourneyStateTests {
    interface TestableCheckYourAnswersJourneyState :
        CheckYourAnswersPartialJourneyState,
        JourneyState

    @Test
    fun `checkYourAnswersJourney adds correct configuration to checkable steps when isCheckingAnswers is true`() {
        // Arrange
        val mockState =
            mock<TestableCheckYourAnswersJourneyState> {
                on { isCheckingAnswers } doReturn true
                on { cyaStep } doReturn mock()
                on { baseJourneyId } doReturn "baseJourneyId"
            }
        val jb = JourneyBuilder(mockState)
        val step1 = StepInitialiserTests.mockInitialisableStep()
        val step2 = StepInitialiserTests.mockInitialisableStep()

        // Act
        jb.step(step1) {
            routeSegment("step1")
            nextUrl { "nextStep" }
            parents { NoParents() }
        }
        jb.step(step2) {
            routeSegment("step2")
            nextUrl { "nextStep" }
            parents { NoParents() }
            checkable()
        }
        jb.unreachableStepUrl { "unreachable" }
        jb.checkYourAnswersJourney()
        jb.build()

        // Assert
        val destinationCaptor = argumentCaptor<(TestEnum) -> Destination>()
        verify(step1).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            destinationCaptor.capture(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
        verify(step2).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            destinationCaptor.capture(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
        val destinations = destinationCaptor.allValues
        // First step is not checkable, so its next destination should be unchanged (i.e. not the CYA step)
        assert(destinations[0](TestEnum.ENUM_VALUE) is Destination.ExternalUrl)

        // Second step is checkable, so its next destination should be the CYA step
        assert(destinations[1](TestEnum.ENUM_VALUE) is Destination.VisitableStep)
    }
}
