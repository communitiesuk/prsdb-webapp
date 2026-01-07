package uk.gov.communities.prsdb.webapp.journeys.updateLicensing

import kotlinx.serialization.Serializable
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
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
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateLicensingJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateLicensingJourney>,
    private val ownershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        // QQ  - properly init the state eventually
        if (state.propertyId == null) {
            ownershipService.getPropertyOwnership(propertyId).let {
                state.propertyId = propertyId
                state.originalLicenseData =
                    LicenseData(
                        licenseType = it.license?.licenseType ?: LicensingType.NO_LICENSING,
                        licenseNumber = it.license?.licenseNumber,
                    )
            }
        } else if (state.propertyId != propertyId) {
            throw IllegalStateException("Journey state property ID ${state.propertyId} does not match provided property ID $propertyId")
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
                nextUrl { "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT" }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeState(user)
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
    override val cyaStep: RequestableStep<Complete, CheckAnswersFormModel, UpdateLicensingJourneyState>,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    UpdateLicensingJourneyState {
    override var cyaChildJourneyId: String? by delegateProvider.mutableDelegate("checkYourAnswersChildJourneyId")

    override var originalLicenseData: LicenseData? by delegateProvider.mutableDelegate("originalLicenseData")
    override var propertyId: Long? by delegateProvider.mutableDelegate("propertyId")

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal

        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(it) })
    }

    companion object {
        fun generateSeedForUser(user: Principal): String = "Update licence for user ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface UpdateLicensingJourneyState :
    LicensingState,
    CheckYourAnswersJourneyState {
    val licensingTask: LicensingTask
    override val cyaStep: RequestableStep<Complete, CheckAnswersFormModel, UpdateLicensingJourneyState>
    val originalLicenseData: LicenseData?
    val propertyId: Long?
}

@Serializable
data class LicenseData(
    val licenseType: LicensingType,
    val licenseNumber: String?,
)
