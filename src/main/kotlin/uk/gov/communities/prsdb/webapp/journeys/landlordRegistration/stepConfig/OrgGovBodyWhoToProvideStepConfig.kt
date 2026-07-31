package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyWhoToProvideFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class OrgGovBodyWhoToProvideStepConfig :
    AbstractRequestableStepConfig<Complete, OrgGovBodyWhoToProvideFormModel, OrgGovBodyMembersState>() {
    override val formModelClass = OrgGovBodyWhoToProvideFormModel::class

    override fun getStepSpecificContent(state: OrgGovBodyMembersState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgGovBodyWhoToProvide.fieldSetHeading",
            "fieldName" to "whoToProvide",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.DIRECTOR,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.director",
                    ),
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.TRUSTEE,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.trustee",
                    ),
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.PARTNER,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.partner",
                    ),
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.OTHER,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.otherMember",
                    ),
                ),
        )

    override fun resolvePageContent(
        state: OrgGovBodyMembersState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? OrgGovBodyWhoToProvideFormModel
        if (formModel?.whoToProvide != null) return defaultContent

        val editingMember = getEditingMemberOrNull(state) ?: return defaultContent
        val prepopulatedFormModel = formModel ?: OrgGovBodyWhoToProvideFormModel()
        prepopulatedFormModel.whoToProvide = editingMember.type
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: OrgGovBodyMembersState) = "forms/orgGovBodyWhoToProvideForm"

    override fun mode(state: OrgGovBodyMembersState) =
        getFormModelFromStateOrNull(
            state,
        )?.whoToProvide?.let { Complete.COMPLETE }

    private fun getEditingMemberOrNull(state: OrgGovBodyMembersState) =
        state.editingGovBodyMemberId?.let { state.governingBodyMembersMap?.get(it) }
}

@JourneyFrameworkComponent
final class OrgGovBodyWhoToProvideStep(
    stepConfig: OrgGovBodyWhoToProvideStepConfig,
) : RequestableStep<Complete, OrgGovBodyWhoToProvideFormModel, OrgGovBodyMembersState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-who-to-provide"
    }
}
