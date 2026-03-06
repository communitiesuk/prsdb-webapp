package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.LANDLORD_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
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

        return journey(state) {
            unreachableStepUrl { "/" }
            task(journey.licensingTask) {
                initialStep()
                nextStep { journey.cyaStep }
                checkable()
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
            checkYourAnswersJourney()
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
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
    journeyName: String = "licence",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, delegateProvider, journeyName),
    UpdateLicensingJourneyState {
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("checkYourAnswersChildJourneyId")
    override var hasOriginalLicense: Boolean by delegateProvider.requiredDelegate("hasOriginalLicense")
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
}

interface UpdateLicensingJourneyState :
    LicensingState,
    CheckYourAnswersJourneyState {
    val licensingTask: LicensingTask
    override val cyaStep: UpdateLicensingCyaStep
    val hasOriginalLicense: Boolean
    val propertyId: Long
}
