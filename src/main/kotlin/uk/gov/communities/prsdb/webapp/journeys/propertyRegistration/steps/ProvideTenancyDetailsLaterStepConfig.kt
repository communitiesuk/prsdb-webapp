package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideTenancyDetailsLaterStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, HouseholdsAndTenantsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: HouseholdsAndTenantsState) =
        mapOf(
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: HouseholdsAndTenantsState) = "forms/provideTenancyDetailsLaterForm"

    override fun mode(state: HouseholdsAndTenantsState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideTenancyDetailsLaterStep(
    stepConfig: ProvideTenancyDetailsLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, HouseholdsAndTenantsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-tenancy-details-later"
    }
}
