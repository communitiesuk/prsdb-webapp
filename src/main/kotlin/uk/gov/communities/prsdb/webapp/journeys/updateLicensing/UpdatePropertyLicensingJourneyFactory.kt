package uk.gov.communities.prsdb.webapp.journeys.updateLicensing

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController.Companion.LANDLORD_PROPERTY_DETAILS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
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
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.CheckYourAnswersJourneyState.Companion.checkable
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateLicensingJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateLicensingJourney>,
    private val ownershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        // TODO PRSD-1550 - properly init the state, make the property ID immutable and handle mismatched property IDs
        if (state.propertyId == null) {
            ownershipService.getPropertyOwnership(propertyId).let {
                state.propertyId = propertyId
                state.hasOriginalLicense = it.license != null
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
                nextUrl { LANDLORD_PROPERTY_DETAILS_ROUTE }
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
    override val cyaStep: UpdateLicensingCheckAnswersStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    UpdateLicensingJourneyState {
    override var cyaChildJourneyId: String? by delegateProvider.mutableDelegate("checkYourAnswersChildJourneyId")

    override var hasOriginalLicense: Boolean? by delegateProvider.mutableDelegate("hasOriginalLicense")
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
    override val cyaStep: UpdateLicensingCheckAnswersStep
    val hasOriginalLicense: Boolean?
    val propertyId: Long?
}
