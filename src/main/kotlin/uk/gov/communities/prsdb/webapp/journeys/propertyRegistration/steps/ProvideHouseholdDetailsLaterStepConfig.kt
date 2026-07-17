package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideHouseholdDetailsLaterStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, HouseholdsAndTenantsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: HouseholdsAndTenantsState) =
        mapOf(
            "heading" to "forms.provideTenancyDetailsLater.heading",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: HouseholdsAndTenantsState) = "forms/provideHouseholdDetailsLaterForm"

    override fun mode(state: HouseholdsAndTenantsState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideHouseholdDetailsLaterStep(
    stepConfig: ProvideHouseholdDetailsLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, HouseholdsAndTenantsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-tenancy-details-later"
    }
}
