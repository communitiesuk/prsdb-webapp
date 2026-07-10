package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ALLOW_SKIPPING_PROPERTY_REGISTRATION_FIELDS
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@JourneyFrameworkComponent
class HouseholdStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<Complete, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>() {
    override val formModelClass = NumberOfHouseholdsFormModel::class

    override fun getStepSpecificContent(state: HouseholdsAndTenantsState): Map<String, Any?> {
        val skipTenancyFlow = featureFlagManager.checkFeature(ALLOW_SKIPPING_PROPERTY_REGISTRATION_FIELDS)
        return mapOf(
            "fieldSetHeading" to "forms.numberOfHouseholds.heading",
            "label" to "forms.numberOfHouseholds.label",
            "skipTenancyFlow" to skipTenancyFlow,
        )
    }

    override fun chooseTemplate(state: HouseholdsAndTenantsState): String =
        if (featureFlagManager.checkFeature(ALLOW_SKIPPING_PROPERTY_REGISTRATION_FIELDS)) {
            "forms/numberOfHouseholdsForm.skipTenancyFlow"
        } else {
            "forms/numberOfHouseholdsForm"
        }

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
