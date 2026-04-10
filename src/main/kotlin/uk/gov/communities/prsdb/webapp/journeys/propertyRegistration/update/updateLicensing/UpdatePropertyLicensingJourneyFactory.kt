package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.LANDLORD_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateLicensingJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateLicensingJourney>,
    private val ownershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.hasOriginalLicense = ownershipService.getPropertyOwnership(propertyId).license != null
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state)
        }
    }

    private fun checkYourAnswersJourneyMap(state: UpdateLicensingJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configure {
                withAdditionalContentProperty { "title" to "propertyDetails.update.title" }
            }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            checkAnswerTask(journey.licensingTask)
            configureStep(journey.licensingTypeStep) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.licensingType.fieldSetHeading"
                }
            }
            step(journey.finishCyaStep) {
                parents { journey.licensingTask.isComplete() }
                nextDestination { Destination.Nowhere() }
            }
        }

    private fun mainJourneyMap(state: UpdateLicensingJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { "/" }
            task(journey.licensingTask) {
                initialStep()
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateLicensingCyaStep.ROUTE_SEGMENT)
                parents { journey.licensingTask.isComplete() }
                nextUrl { LANDLORD_PROPERTY_DETAILS_ROUTE }
            }
            configureStep(journey.licensingTypeStep) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.licensingType.fieldSetHeading"
                }
            }
        }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateLicensingJourney(
    // Licensing task
    override val licensingTask: LicensingTask,
    override val licensingTypeStep: LicensingTypeStep,
    override val selectiveLicenceStep: SelectiveLicenceStep,
    override val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep,
    override val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep,
    // Check your answers step
    override val cyaStep: UpdateLicensingCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateLicensingJourneyState>,
    journeyStateService: JourneyStateService,
    journeyName: String = "licence",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateLicensingJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var hasOriginalLicense: Boolean by delegateProvider.requiredDelegate("hasOriginalLicense")
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
}

interface UpdateLicensingJourneyState :
    LicensingState,
    CheckYourAnswersJourneyState {
    val licensingTask: LicensingTask
    override val finishCyaStep: FinishCyaJourneyStep
    override val cyaStep: UpdateLicensingCyaStep
    val hasOriginalLicense: Boolean
    val propertyId: Long
}
