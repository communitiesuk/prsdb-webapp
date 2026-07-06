package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CheckCanDeregisterStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<CanDeregisterMode, NoInputFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState): String = ""

    override fun mode(state: PropertyDeregistrationJourneyState): CanDeregisterMode {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        return if (propertyOwnership.landlords.size > 1) {
            CanDeregisterMode.HAS_JOINT_LANDLORDS
        } else {
            CanDeregisterMode.SINGLE_LANDLORD
        }
    }
}

@JourneyFrameworkComponent
final class CheckCanDeregisterStep(
    stepConfig: CheckCanDeregisterStepConfig,
) : JourneyStep.RequestableStep<CanDeregisterMode, NoInputFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "deregister"
    }
}

enum class CanDeregisterMode {
    SINGLE_LANDLORD,
    HAS_JOINT_LANDLORDS,
}
