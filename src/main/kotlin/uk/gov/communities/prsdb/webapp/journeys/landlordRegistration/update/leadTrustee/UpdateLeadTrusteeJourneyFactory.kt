package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LeadTrusteeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LeadTrusteeTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask
import java.security.Principal

@PrsdbWebService
class UpdateLeadTrusteeJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateLeadTrusteeJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return if (state.checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state)
        }
    }

    private fun mainJourneyMap(state: UpdateLeadTrusteeJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            task(journey.leadTrusteeTask) {
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "landlordDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateLeadTrusteeCyaStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteeTask.isComplete() }
                nextStep { journey.completeLeadTrusteeUpdateStep }
            }
            step(journey.completeLeadTrusteeUpdateStep) {
                parents { journey.cyaStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE + "#organisation-contacts" }
            }
        }

    private fun checkYourAnswersJourneyMap(state: UpdateLeadTrusteeJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure {
                withAdditionalContentProperty { "title" to "landlordDetails.update.title" }
            }
            checkAnswerStep(journey.leadTrusteeTask.leadTrusteeNameStep, LeadTrusteeNameStep.ROUTE_SEGMENT)
            checkAnswerStep(journey.leadTrusteeTask.leadTrusteeDobStep, LeadTrusteeDobStep.ROUTE_SEGMENT)
            checkAnswerStep(journey.leadTrusteeTask.leadTrusteeEmailStep, LeadTrusteeEmailStep.ROUTE_SEGMENT)
            checkAnswerStep(journey.leadTrusteeTask.leadTrusteePhoneStep, LeadTrusteePhoneStep.ROUTE_SEGMENT)
            checkAnswerTask(journey.leadTrusteeTask.trusteeAddressTask, LookupAddressStep.ROUTE_SEGMENT)
            step(journey.finishCyaStep) {
                parents { journey.leadTrusteeTask.trusteeAddressTask.isComplete() }
                nextDestination { Destination.Nowhere() }
            }
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateLeadTrusteeJourney(
    override val leadTrusteeNameStep: LeadTrusteeNameStep,
    override val leadTrusteeDobStep: LeadTrusteeDobStep,
    override val leadTrusteeEmailStep: LeadTrusteeEmailStep,
    override val leadTrusteePhoneStep: LeadTrusteePhoneStep,
    override val trusteeAddressTask: TrusteeAddressTask,
    override val leadTrusteeTask: LeadTrusteeTask,
    override val cyaStep: UpdateLeadTrusteeCyaStep,
    override val completeLeadTrusteeUpdateStep: CompleteLeadTrusteeUpdateStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateLeadTrusteeJourneyState>,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "lead-trustee",
) : AbstractJourneyState(journeyStateService),
    UpdateLeadTrusteeJourneyState {
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateLeadTrusteeJourneyState :
    LeadTrusteeState,
    CheckYourAnswersJourneyState {
    val leadTrusteeTask: LeadTrusteeTask
    override val cyaStep: UpdateLeadTrusteeCyaStep
    val completeLeadTrusteeUpdateStep: CompleteLeadTrusteeUpdateStep
}
