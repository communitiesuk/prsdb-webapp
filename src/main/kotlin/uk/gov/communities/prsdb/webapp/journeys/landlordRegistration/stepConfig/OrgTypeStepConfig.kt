package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxDividerViewModel

@JourneyFrameworkComponent
class OrgTypeStepConfig : AbstractRequestableStepConfig<Complete, OrgTypeFormModel, JourneyState>() {
    override val formModelClass = OrgTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgType.fieldSetHeading",
            "fieldSetHint" to "registerAsALandlord.orgType.fieldSetHint",
            "checkboxOptions" to
                listOf(
                    CheckboxButtonViewModel(
                        value = OrgType.COMPANY,
                        labelMsgKey = "registerAsALandlord.orgType.checkbox.company",
                    ),
                    CheckboxButtonViewModel(
                        value = OrgType.CHARITY,
                        labelMsgKey = "registerAsALandlord.orgType.checkbox.charity",
                    ),
                    CheckboxButtonViewModel(
                        value = OrgType.TRUST,
                        labelMsgKey = "registerAsALandlord.orgType.checkbox.trust",
                    ),
                    CheckboxDividerViewModel(labelMsgKey = "registerAsALandlord.orgType.divider"),
                    CheckboxButtonViewModel(
                        value = OrgType.NONE,
                        labelMsgKey = "registerAsALandlord.orgType.checkbox.none",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgTypeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgTypeStep(
    stepConfig: OrgTypeStepConfig,
) : RequestableStep<Complete, OrgTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-type"
    }
}
