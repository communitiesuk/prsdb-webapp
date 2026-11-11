package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholdTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class FreeholdTypeStepConfig : AbstractRequestableStepConfig<FreeholdType, FreeholdTypeFormModel, JourneyState>() {
    override val formModelClass = FreeholdTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.freeholdType.fieldSetHeading",
            "fieldSetHint" to "registerProperty.freeholdType.fieldSetHint",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = FreeholdType.SOLE_OWNERSHIP,
                        labelMsgKey = "registerProperty.freeholdType.radios.option.soleOwnership.label",
                        hintMsgKey = "registerProperty.freeholdType.radios.option.soleOwnership.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FreeholdType.JOINT_TENANTS,
                        labelMsgKey = "registerProperty.freeholdType.radios.option.jointTenants.label",
                        hintMsgKey = "registerProperty.freeholdType.radios.option.jointTenants.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FreeholdType.TENANTS_IN_COMMON,
                        labelMsgKey = "registerProperty.freeholdType.radios.option.tenantsInCommon.label",
                        hintMsgKey = "registerProperty.freeholdType.radios.option.tenantsInCommon.hint",
                    ),
                    RadiosButtonViewModel(
                        value = FreeholdType.COMMONHOLD,
                        labelMsgKey = "registerProperty.freeholdType.radios.option.commonhold.label",
                        hintMsgKey = "registerProperty.freeholdType.radios.option.commonhold.hint",
                    ),
                ),
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/freeholdTypeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.freeholdType
}

@JourneyFrameworkComponent
final class FreeholdTypeStep(
    stepConfig: FreeholdTypeStepConfig,
) : RequestableStep<FreeholdType, FreeholdTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freehold-type"
    }
}

enum class FreeholdType {
    SOLE_OWNERSHIP,
    JOINT_TENANTS,
    TENANTS_IN_COMMON,
    COMMONHOLD,
}
