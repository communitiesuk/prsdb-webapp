package uk.gov.communities.prsdb.webapp.journeys.updateLicensing

import kotlinx.serialization.Serializable
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.LANDLORD_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState.Companion.checkable
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class UpdateLicensingJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateLicensingJourney>,
    private val ownershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (state.propertyId != propertyId) {
            throw NoSuchJourneyException("Journey is not for property $propertyId")
        }

        return journey(state) {
            unreachableStepUrl { "/" }
            task(journey.licensingTask) {
                initialStep()
                nextStep { journey.cyaStep }
                checkable()
            }
            step(journey.cyaStep) {
                routeSegment("check-licensing-answers")
                parents { journey.licensingTask.isComplete() }
                nextUrl { LANDLORD_PROPERTY_DETAILS_ROUTE }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(propertyId: Long): String =
        stateFactory.getObject().initializeOrRestoreState(propertyId) {
            setValue("propertyId", "$propertyId")
            setValue("hasOriginalLicense", "${ownershipService.getPropertyOwnership(propertyId).license != null}")
        }
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
    override val cyaStep: UpdateLicensingCheckAnswersStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    UpdateLicensingJourneyState {
    override var cyaChildJourneyId: String? by delegateProvider.mutableDelegate("checkYourAnswersChildJourneyId")
    override val hasOriginalLicense: Boolean by delegateProvider.requiredDelegate("hasOriginalLicense")

    override val propertyId: Long by delegateProvider.requiredDelegate("propertyId")

    override fun generateJourneyId(seed: Any?): String {
        val propertyId = seed as? Long

        return super<AbstractJourneyState>.generateJourneyId(propertyId?.let { generateSeedForProperty(it) })
    }

    companion object {
        fun generateSeedForProperty(propertyId: Long): String = "Update licence for property $propertyId"
    }
}

interface UpdateLicensingJourneyState :
    LicensingState,
    CheckYourAnswersJourneyState {
    val licensingTask: LicensingTask
    override val cyaStep: UpdateLicensingCheckAnswersStep
    val hasOriginalLicense: Boolean
    val propertyId: Long
}

@Serializable
data class LicenseData(
    val licenseType: LicensingType,
    val licenseNumber: String?,
)
