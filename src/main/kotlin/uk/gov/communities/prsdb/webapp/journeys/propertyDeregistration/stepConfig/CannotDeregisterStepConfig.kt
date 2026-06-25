package uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.PropertyDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CannotDeregisterStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyDeregistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyDeregistrationJourneyState): Map<String, Any?> {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyOwnershipId)
        return mapOf(
            "addressLines" to propertyOwnership.address.toMultiLineAddress().split("\n"),
            "leavePropertyUrl" to absoluteUrlProvider.buildLeavePropertyUri(state.propertyOwnershipId),
        )
    }

    override fun chooseTemplate(state: PropertyDeregistrationJourneyState) = "cannotDeregisterPropertyJointLandlords"

    override fun mode(state: PropertyDeregistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class CannotDeregisterStep(
    stepConfig: CannotDeregisterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyDeregistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "cannot-deregister"
    }
}
