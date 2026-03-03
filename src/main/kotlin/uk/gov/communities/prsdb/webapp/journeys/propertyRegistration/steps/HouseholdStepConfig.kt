package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@JourneyFrameworkComponent
class HouseholdStepConfig : AbstractRequestableStepConfig<Complete, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>() {
    override val formModelClass = NumberOfHouseholdsFormModel::class

    override fun getStepSpecificContent(state: HouseholdsAndTenantsState) =
        mapOf(
            "fieldSetHeading" to "forms.numberOfHouseholds.heading",
            "label" to "forms.numberOfHouseholds.label",
        )

    override fun chooseTemplate(state: HouseholdsAndTenantsState): String = "forms/numberOfHouseholdsForm"

    override fun mode(state: HouseholdsAndTenantsState) = getFormModelFromStateOrNull(state)?.numberOfHouseholds?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class HouseholdStep(
    stepConfig: HouseholdStepConfig,
) : RequestableStep<Complete, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "number-of-households"
    }
}
