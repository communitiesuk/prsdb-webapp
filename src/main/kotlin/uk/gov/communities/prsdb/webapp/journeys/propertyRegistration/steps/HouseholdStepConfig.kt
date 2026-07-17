package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@JourneyFrameworkComponent
class HouseholdStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<HouseholdStepMode, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>() {
    override val formModelClass = NumberOfHouseholdsFormModel::class

    override fun getStepSpecificContent(state: HouseholdsAndTenantsState) =
        buildMap {
            put("fieldSetHeading", "forms.numberOfHouseholdsRestructureAndSkipping.heading")
            put("label", "forms.numberOfHouseholdsRestructureAndSkipping.label")
            if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
                put("submitButtonText", "forms.buttons.saveAndContinue")
                put("submitButtonAction", CONTINUE_BUTTON_ACTION_NAME)
                put("secondarySubmitButtonText", "forms.buttons.provideThisLater")
                put("secondarySubmitButtonAction", PROVIDE_THIS_LATER_BUTTON_ACTION_NAME)
                put("showSecondarySubmitButton", true)
            }
        }

    override fun chooseTemplate(state: HouseholdsAndTenantsState): String =
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            "forms/numberOfHouseholdsFormRestructureAndSkipping"
        } else {
            "forms/numberOfHouseholdsFormOld"
        }

    override fun mode(state: HouseholdsAndTenantsState) =
        getFormModelFromStateOrNull(state)?.let { formModel ->
            when (formModel.action) {
                PROVIDE_THIS_LATER_BUTTON_ACTION_NAME -> HouseholdStepMode.PROVIDE_THIS_LATER
                else -> formModel.numberOfHouseholds.takeIf { it.isNotBlank() }?.let { HouseholdStepMode.COMPLETE }
            }
        }
}

@JourneyFrameworkComponent
final class HouseholdStep(
    stepConfig: HouseholdStepConfig,
) : RequestableStep<HouseholdStepMode, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "number-of-households"
    }
}

enum class HouseholdStepMode {
    COMPLETE,
    PROVIDE_THIS_LATER,
}
