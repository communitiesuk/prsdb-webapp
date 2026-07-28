package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GoverningBodyMemberNameFormModel

@JourneyFrameworkComponent
class OrgGovBodyMemberNameStepConfig :
    AbstractRequestableStepConfig<Complete, GoverningBodyMemberNameFormModel, LandlordRegistrationOrgLandlordState>() {
    override val formModelClass = GoverningBodyMemberNameFormModel::class

    override fun getStepSpecificContent(state: LandlordRegistrationOrgLandlordState) =
        mapOf(
            "fieldSetHeading" to "forms.orgGovBodyMemberName.fieldSetHeading",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun resolvePageContent(
        state: LandlordRegistrationOrgLandlordState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? GoverningBodyMemberNameFormModel
        if (!formModel?.name.isNullOrBlank()) return defaultContent

        val editingMember = getEditingMemberOrNull(state) ?: return defaultContent
        val prepopulatedFormModel = formModel ?: GoverningBodyMemberNameFormModel()
        prepopulatedFormModel.name = editingMember.name
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: LandlordRegistrationOrgLandlordState) = "forms/nameForm"

    override fun mode(state: LandlordRegistrationOrgLandlordState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getEditingMemberOrNull(state: LandlordRegistrationOrgLandlordState) =
        state.editingGovBodyMemberId?.let { state.governingBodyMembersMap?.get(it) }
}

@JourneyFrameworkComponent
final class OrgGovBodyMemberNameStep(
    stepConfig: OrgGovBodyMemberNameStepConfig,
) : RequestableStep<Complete, GoverningBodyMemberNameFormModel, LandlordRegistrationOrgLandlordState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-member-name"
    }
}
