package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel

@JourneyFrameworkComponent
class LicensingTypeStepConfig(
    private val featureFlagManager: FeatureFlagManager,
) : AbstractRequestableStepConfig<LicensingTypeMode, LicensingTypeFormModel, LicensingState>() {
    override val formModelClass = LicensingTypeFormModel::class

    override fun getStepSpecificContent(state: LicensingState): Map<String, Any?> {
        val showProvideThisLater =
            state.allowProvideLicensingLaterRoute &&
                featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        return mapOf(
            "fieldSetHeading" to "forms.licensingType.fieldSetHeading",
            "fieldSetHint" to "forms.licensingType.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "showSecondarySubmitButton" to showProvideThisLater,
            "submitButtonAction" to CONTINUE_BUTTON_ACTION_NAME,
            "secondarySubmitButtonText" to "forms.buttons.provideThisLater",
            "secondarySubmitButtonAction" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = LicensingType.SELECTIVE_LICENCE,
                        labelMsgKey = "forms.licensingType.radios.option.selectiveLicence.label",
                        hintMsgKey = "forms.licensingType.radios.option.selectiveLicence.hint",
                    ),
                    RadiosButtonViewModel(
                        value = LicensingType.HMO_MANDATORY_LICENCE,
                        labelMsgKey = "forms.licensingType.radios.option.hmoMandatory.label",
                        hintMsgKey = "forms.licensingType.radios.option.hmoMandatory.hint",
                    ),
                    RadiosButtonViewModel(
                        value = LicensingType.HMO_ADDITIONAL_LICENCE,
                        labelMsgKey = "forms.licensingType.radios.option.hmoAdditional.label",
                        hintMsgKey = "forms.licensingType.radios.option.hmoAdditional.hint",
                    ),
                    RadiosDividerViewModel("forms.radios.dividerText"),
                    RadiosButtonViewModel(
                        value = LicensingType.NO_LICENSING,
                        labelMsgKey = "forms.licensingType.radios.option.noLicensing.label",
                    ),
                ),
        )
    }

    override fun chooseTemplate(state: LicensingState): String = "forms/licensingTypeForm"

    override fun mode(state: LicensingState) =
        getFormModelFromStateOrNull(state)?.let { formModel ->
            if (formModel.action == PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) {
                if (state.allowProvideLicensingLaterRoute &&
                    featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
                ) {
                    LicensingTypeMode.PROVIDE_LATER
                } else {
                    throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "The 'Provide this later' route is not available for this journey",
                    )
                }
            } else {
                formModel.licensingType?.let { licensingType ->
                    when (licensingType) {
                        LicensingType.SELECTIVE_LICENCE -> LicensingTypeMode.SELECTIVE_LICENCE
                        LicensingType.HMO_MANDATORY_LICENCE -> LicensingTypeMode.HMO_MANDATORY_LICENCE
                        LicensingType.HMO_ADDITIONAL_LICENCE -> LicensingTypeMode.HMO_ADDITIONAL_LICENCE
                        LicensingType.NO_LICENSING -> LicensingTypeMode.NO_LICENSING
                        LicensingType.PROVIDE_LATER -> LicensingTypeMode.PROVIDE_LATER
                    }
                }
            }
        }
}

@JourneyFrameworkComponent
final class LicensingTypeStep(
    stepConfig: LicensingTypeStepConfig,
) : RequestableStep<LicensingTypeMode, LicensingTypeFormModel, LicensingState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "licensing-type"
    }
}

enum class LicensingTypeMode {
    SELECTIVE_LICENCE,
    HMO_MANDATORY_LICENCE,
    HMO_ADDITIONAL_LICENCE,
    NO_LICENSING,
    PROVIDE_LATER,
}
