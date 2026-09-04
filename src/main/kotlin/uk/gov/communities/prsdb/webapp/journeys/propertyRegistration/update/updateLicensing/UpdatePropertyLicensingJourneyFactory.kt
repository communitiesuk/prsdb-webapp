package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class UpdateLicensingJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateLicensingJourney>,
    private val ownershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(
        propertyId: Long,
        returnUrl: String,
        sendsUpdateEmails: Boolean,
    ): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.sendsUpdateEmails = sendsUpdateEmails
            val propertyOwnership = ownershipService.getPropertyOwnership(propertyId)
            state.hasOriginalLicense = propertyOwnership.license != null
            state.lastModifiedDate = propertyOwnership.getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state, returnUrl)
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
            checkAnswerTask(journey.licensingTask, { journey })
            configureStep(journey.licensingTask.licensingTypeStep) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.licensingType.fieldSetHeading"
                }
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            configureStep(journey.licensingTask.selectiveLicenceStep) {
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            configureStep(journey.licensingTask.hmoMandatoryLicenceStep) {
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            configureStep(journey.licensingTask.hmoAdditionalLicenceStep) {
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            step(journey.finishCyaStep) {
                parents { journey.licensingTask.isComplete() }
                nextDestination { Destination.Nowhere() }
            }
        }

    private fun mainJourneyMap(
        state: UpdateLicensingJourney,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { returnUrl }
            task(journey.licensingTask) {
                withDependencies { journey }
                initialStep()
                backUrl { returnUrl }
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateLicensingCyaStep.ROUTE_SEGMENT)
                parents { journey.licensingTask.isComplete() }
                nextUrl { returnUrl }
            }
            configureStep(journey.licensingTask.licensingTypeStep) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.licensingType.fieldSetHeading"
                }
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            configureStep(journey.licensingTask.selectiveLicenceStep) {
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            configureStep(journey.licensingTask.hmoMandatoryLicenceStep) {
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
            configureStep(journey.licensingTask.hmoAdditionalLicenceStep) {
                withAdditionalContentProperty { "submitButtonText" to "forms.buttons.continue" }
            }
        }

    fun initializeJourneyState(seed: Any): String = stateFactory.getObject().initializeOrRestoreState(seed)
}

@JourneyFrameworkComponent
class UpdateLicensingJourney(
    // Licensing task
    override val licensingTask: LicensingTask,
    // Check your answers step
    override val cyaStep: UpdateLicensingCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateLicensingJourneyState>,
    journeyStateService: JourneyStateService,
    journeyName: String = "licence",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateLicensingJourneyState {
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var hasOriginalLicense: Boolean by delegateProvider.requiredDelegate("hasOriginalLicense")
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var sendsUpdateEmails: Boolean by delegateProvider.requiredDelegate("sendsUpdateEmails", true)

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override val allowProvideLicensingLaterRoute: Boolean = false
    override val isOccupied: Boolean? = null
}

interface UpdateLicensingJourneyState :
    LicensingDependencies,
    CheckYourAnswersJourneyState {
    val licensingTask: LicensingTask
    override val finishCyaStep: FinishCyaJourneyStep
    override val cyaStep: UpdateLicensingCyaStep
    val hasOriginalLicense: Boolean
    val propertyId: Long
    val lastModifiedDate: String

    /**
     * Update confirmation emails are addressed to the landlord who made the change, so they cannot be sent when a
     * letting agent updates a property on a landlord's behalf.
     *
     * TODO PDJB-1581: Send letting agent update emails, and remove this.
     */
    var sendsUpdateEmails: Boolean
}
