package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.CheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.ConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.EmailAddressStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.FullNameStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.InvalidLinkStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.JoinOrganisationStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.TokenValidity
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete

@PrsdbWebService
class AcceptOrganisationalLandlordUserInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<AcceptInvitationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.validateTokenStep }
            configure {
                withAdditionalContentProperty { "title" to "acceptOrganisationInvitation.title" }
            }
            step(journey.validateTokenStep) {
                routeSegment(ValidateTokenStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { outcome ->
                    when (outcome) {
                        TokenValidity.VALID -> journey.joinOrganisationStep
                        TokenValidity.INVALID -> journey.invalidLinkStep
                    }
                }
            }
            step(journey.invalidLinkStep) {
                routeSegment(InvalidLinkStep.ROUTE_SEGMENT)
                parents { journey.validateTokenStep.hasOutcome(TokenValidity.INVALID) }
                backDestination { Destination.Nowhere() }
                nextDestination { Destination.Nowhere() }
            }
            step(journey.joinOrganisationStep) {
                routeSegment(JoinOrganisationStep.ROUTE_SEGMENT)
                parents { journey.validateTokenStep.hasOutcome(TokenValidity.VALID) }
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

    fun initializeJourneyState(token: String): String = stateFactory.getObject().initializeState(token)
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationJourney")
class AcceptInvitationJourney(
    override val validateTokenStep: ValidateTokenStep,
    override val invalidLinkStep: InvalidLinkStep,
    override val joinOrganisationStep: JoinOrganisationStep,
    override val fullNameStep: FullNameStep,
    override val emailAddressStep: EmailAddressStep,
    override val checkAnswersStep: CheckAnswersStep,
    override val confirmationStep: ConfirmationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    AcceptOrganisationalLandlordUserInvitationJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? String
        val tokenDescription = token?.let { " for token $it" }.orEmpty()
        return super<AbstractJourneyState>.generateJourneyId(
            "Accept organisational landlord user invitation journey$tokenDescription at time ${System.currentTimeMillis()}",
        )
    }
}

interface AcceptOrganisationalLandlordUserInvitationJourneyState : JourneyState {
    val validateTokenStep: ValidateTokenStep
    val invalidLinkStep: InvalidLinkStep
    val joinOrganisationStep: JoinOrganisationStep
    val fullNameStep: FullNameStep
    val emailAddressStep: EmailAddressStep
    val checkAnswersStep: CheckAnswersStep
    val confirmationStep: ConfirmationStep
}
