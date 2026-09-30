package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.CheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.ConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.EmailAddressStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.FullNameStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps.JoinOrganisationStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete

@PrsdbWebService
class AcceptInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<AcceptInvitationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.joinOrganisationStep }
            configure {
                withAdditionalContentProperty { "title" to "acceptInvitation.title" }
            }
            step(journey.joinOrganisationStep) {
                routeSegment(JoinOrganisationStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { journey.fullNameStep }
            }
            step(journey.fullNameStep) {
                routeSegment(FullNameStep.ROUTE_SEGMENT)
                parents { journey.joinOrganisationStep.isComplete() }
                nextStep { journey.emailAddressStep }
            }
            step(journey.emailAddressStep) {
                routeSegment(EmailAddressStep.ROUTE_SEGMENT)
                parents { journey.fullNameStep.isComplete() }
                nextStep { journey.checkAnswersStep }
            }
            step(journey.checkAnswersStep) {
                routeSegment(CheckAnswersStep.ROUTE_SEGMENT)
                parents { journey.emailAddressStep.isComplete() }
                nextStep { journey.confirmationStep }
            }
            step(journey.confirmationStep) {
                routeSegment(ConfirmationStep.ROUTE_SEGMENT)
                parents { journey.checkAnswersStep.isComplete() }
                backDestination { Destination.Nowhere() }
                nextDestination { Destination.Nowhere() }
            }
        }
    }

    fun initializeJourneyState(): String {
        val state = stateFactory.getObject()
        return state.initializeState(null)
    }
}

@JourneyFrameworkComponent("acceptInvitationJourney")
class AcceptInvitationJourney(
    override val joinOrganisationStep: JoinOrganisationStep,
    override val fullNameStep: FullNameStep,
    override val emailAddressStep: EmailAddressStep,
    override val checkAnswersStep: CheckAnswersStep,
    override val confirmationStep: ConfirmationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    AcceptInvitationJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        return super<AbstractJourneyState>.generateJourneyId(
            "Accept invitation journey at time ${System.currentTimeMillis()}",
        )
    }
}

interface AcceptInvitationJourneyState : JourneyState {
    val joinOrganisationStep: JoinOrganisationStep
    val fullNameStep: FullNameStep
    val emailAddressStep: EmailAddressStep
    val checkAnswersStep: CheckAnswersStep
    val confirmationStep: ConfirmationStep
}
