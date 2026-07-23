package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel

@JourneyFrameworkComponent
class HouseholdStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<HouseholdMode, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>() {
    override val formModelClass = NumberOfHouseholdsFormModel::class

    override fun getStepSpecificContent(state: HouseholdsAndTenantsState) =
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            mapOf(
                "fieldSetHeading" to "forms.numberOfHouseholds.restructureAndSkipping.heading",
                "label" to "forms.numberOfHouseholds.restructureAndSkipping.label",
                "submitButtonText" to "forms.buttons.saveAndContinue",
                "secondarySubmitButtonText" to "forms.buttons.provideThisLater",
                "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
                "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
                "showSecondarySubmitButton" to state.allowProvideTenancyDetailsLaterRoute,
            )
        } else {
            mapOf(
                "fieldSetHeading" to "forms.numberOfHouseholds.heading",
                "label" to "forms.numberOfHouseholds.label",
            )
        }

    override fun chooseTemplate(state: HouseholdsAndTenantsState): String =
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
            "forms/restructureAndSkipping/numberOfHouseholdsForm"
        } else {
            "forms/restructureAndSkipping/numberOfHouseholdsFormLegacy"
        }

    override fun mode(state: HouseholdsAndTenantsState) =
        getFormModelFromStateOrNull(state)?.let {
            if (it.action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) {
                if (state.allowProvideTenancyDetailsLaterRoute) {
                    HouseholdMode.PROVIDE_THIS_LATER
                } else {
                    // This should never happen as the button to trigger this action should not be shown
                    throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "The 'Provide this later' route is not available for this journey",
                    )
                }
            } else {
                HouseholdMode.COMPLETE
            }
        }
}

@JourneyFrameworkComponent
final class HouseholdStep(
    stepConfig: HouseholdStepConfig,
) : RequestableStep<HouseholdMode, NumberOfHouseholdsFormModel, HouseholdsAndTenantsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "number-of-households"
    }
}

enum class HouseholdMode {
    COMPLETE,
    PROVIDE_THIS_LATER,
}
