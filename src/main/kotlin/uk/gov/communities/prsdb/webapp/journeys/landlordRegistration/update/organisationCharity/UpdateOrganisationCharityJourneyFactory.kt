package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity

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
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCharityTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import java.security.Principal

@PrsdbWebService
class UpdateOrganisationCharityJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOrganisationCharityJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun mainJourneyMap(state: UpdateOrganisationCharityJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            task(journey.charityTask) {
                initialStep()
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "landlordDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateOrganisationCharityCyaStep.ROUTE_SEGMENT)
                parents { journey.charityTask.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }

    private fun checkYourAnswersJourneyMap(
        state: UpdateOrganisationCharityJourney,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            when (checkingAnswersFor) {
                OrgIsRegisteredCharityStep.ROUTE_SEGMENT,
                OrgCharityRegisteredWithStep.ROUTE_SEGMENT,
                ->
                    checkAnswerTask(journey.charityTask)

                OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT ->
                    fromTask(journey.charityTask) {
                        checkAnswerStep(task.orgCharityNumberEnglandAndWalesStep, OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT)
                    }

                OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT ->
                    fromTask(journey.charityTask) {
                        checkAnswerStep(task.orgCharityNumberNorthernIrelandStep, OrgCharityNumberNorthernIrelandStep.ROUTE_SEGMENT)
                    }

                OrgCharityNumberScotlandStep.ROUTE_SEGMENT ->
                    fromTask(journey.charityTask) {
                        checkAnswerStep(task.orgCharityNumberScotlandStep, OrgCharityNumberScotlandStep.ROUTE_SEGMENT)
                    }

                else -> throw IllegalStateException("Unknown step being checked: $checkingAnswersFor")
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
            configure {
                withAdditionalContentProperty {
                    "title" to "landlordDetails.update.title"
                }
            }
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateOrganisationCharityJourney(
    override val charityTask: OrgCharityTask,
    override val cyaStep: UpdateOrganisationCharityCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateOrganisationCharityJourneyState>,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "organisation-charity",
) : AbstractJourneyState(journeyStateService),
    UpdateOrganisationCharityJourneyState {
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateOrganisationCharityJourneyState : CheckYourAnswersJourneyState {
    val charityTask: OrgCharityTask
    override val cyaStep: UpdateOrganisationCharityCyaStep
}
