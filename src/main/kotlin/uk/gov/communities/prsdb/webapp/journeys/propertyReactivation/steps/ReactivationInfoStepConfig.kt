package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.PropertyReactivationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class ReactivationInfoStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyReactivationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyReactivationJourneyState) =
        mapOf(
            "addressLines" to
                propertyOwnershipService
                    .getInactivePropertyOwnershipForCurrentLandlord(state.propertyOwnershipId)
                    .address
                    .toMultiLineAddress()
                    .split("\n"),
        )

    override fun chooseTemplate(state: PropertyReactivationJourneyState) = "forms/reactivatePropertyInfoForm"

    override fun mode(state: PropertyReactivationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ReactivationInfoStep(
    stepConfig: ReactivationInfoStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyReactivationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "info"
    }
}
