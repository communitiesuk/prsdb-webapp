package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel

@JourneyFrameworkComponent
class LicensingTypeStepConfig : AbstractRequestableStepConfig<LicensingTypeMode, LicensingTypeFormModel, JourneyState>() {
    override val formModelClass = LicensingTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.licensingType.fieldSetHeading",
            "fieldSetHint" to "forms.licensingType.fieldSetHint",
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

    override fun chooseTemplate(state: JourneyState): String = "forms/licensingTypeForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.licensingType?.let { licensingType ->
            when (licensingType) {
                LicensingType.SELECTIVE_LICENCE -> LicensingTypeMode.SELECTIVE_LICENCE
                LicensingType.HMO_MANDATORY_LICENCE -> LicensingTypeMode.HMO_MANDATORY_LICENCE
                LicensingType.HMO_ADDITIONAL_LICENCE -> LicensingTypeMode.HMO_ADDITIONAL_LICENCE
                LicensingType.NO_LICENSING -> LicensingTypeMode.NO_LICENSING
            }
        }
}

@JourneyFrameworkComponent
final class LicensingTypeStep(
    stepConfig: LicensingTypeStepConfig,
) : RequestableStep<LicensingTypeMode, LicensingTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "licensing-type"
    }
}

enum class LicensingTypeMode {
    SELECTIVE_LICENCE,
    HMO_MANDATORY_LICENCE,
    HMO_ADDITIONAL_LICENCE,
    NO_LICENSING,
}
