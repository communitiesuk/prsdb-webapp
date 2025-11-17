package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalAuthorityStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.security.Principal

@PrsdbWebService
class NewPropertyRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<PropertyRegistrationJourneyState>,
) {
    final fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.taskListStep }
            step(TASK_LIST_PATH_SEGMENT, journey.taskListStep) {
                initialStep()
                noNextDestination()
            }
            task(journey.addressTask) {
                parents { NoParents() }
                redirectToStep { journey.propertyTypeStep }
            }
            step("property-type", journey.propertyTypeStep) {
                parents { journey.addressTask.isComplete() }
                nextStep { journey.ownershipTypeStep }
            }
            step("ownership-type", journey.ownershipTypeStep) {
                parents { journey.propertyTypeStep.isComplete() }
                nextStep { journey.licensingTask.firstStep }
            }
            task(journey.licensingTask) {
                parents { journey.ownershipTypeStep.isComplete() }
                redirectToStep { journey.occupationTask.firstStep }
            }
            task(journey.occupationTask) {
                parents { journey.licensingTask.isComplete() }
                redirectToStep { journey.cyaStep }
            }
            step("check-your-answers", journey.cyaStep) {
                parents { journey.occupationTask.isComplete() }
                nextUrl { "/" }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeJourneyState(user)
}

@PrsdbWebComponent
@Scope("prototype")
class PropertyRegistrationJourneyState(
    val taskListStep: PropertyRegistrationTaskListStep,
    override val lookupStep: LookupAddressStep,
    override val selectAddressStep: SelectAddressStep,
    override val alreadyRegisteredStep: AlreadyRegisteredStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val manualAddressStep: ManualAddressStep,
    override val localAuthorityStep: LocalAuthorityStep,
    private val journeyStateService: JourneyStateService,
    val addressTask: AddressTask,
    val propertyTypeStep: PropertyTypeStep,
    val ownershipTypeStep: OwnershipTypeStep,
    override val licensingTypeStep: LicensingTypeStep,
    override val selectiveLicenceStep: SelectiveLicenceStep,
    override val hmoMandatoryLicenceStep: HmoMandatoryLicenceStep,
    override val hmoAdditionalLicenceStep: HmoAdditionalLicenceStep,
    val licensingTask: LicensingTask,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    val occupationTask: OccupationTask,
    val cyaStep: PropertyRegistrationCheckAnswersStep,
) : AbstractJourneyState(journeyStateService),
    AddressState,
    LicensingState,
    OccupiedJourneyState {
    override var cachedAddresses: List<AddressDataModel>? by mutableDelegate("cachedAddresses", serializer())

    final fun initializeJourneyState(user: Principal): String {
        val journeyId = generateJourneyId(user)

        journeyStateService
            .initialiseJourneyWithId(journeyId) {}
        return journeyId
    }

    companion object {
        fun generateJourneyId(user: Principal): String =
            "Prop reg journey for user ${user.name} at time ${System.currentTimeMillis()}"
                .hashCode()
                .toUInt()
                .times(111113111U)
                .and(0x7FFFFFFFu)
                .toString(36)
    }
}
