package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyMemberDobFormModel

@JourneyFrameworkComponent
class OrgGovBodyMemberDobStepConfig :
    AbstractRequestableStepConfig<Complete, OrgGovBodyMemberDobFormModel, OrgGovBodyMembersState>() {
    override val formModelClass = OrgGovBodyMemberDobFormModel::class

    override fun getStepSpecificContent(state: OrgGovBodyMembersState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgGovBodyMemberDob.fieldSetHeading",
            "fieldSetHint" to "registerAsALandlord.orgGovBodyMemberDob.fieldSetHint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun resolvePageContent(
        state: OrgGovBodyMembersState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? OrgGovBodyMemberDobFormModel
        if (!formModel?.day.isNullOrBlank()) return defaultContent

        val editingMember = getEditingMemberOrNull(state) ?: return defaultContent
        val prepopulatedFormModel = formModel ?: OrgGovBodyMemberDobFormModel()
        prepopulatedFormModel.day = editingMember.dateOfBirth.dayOfMonth.toString()
        prepopulatedFormModel.month = editingMember.dateOfBirth.monthNumber.toString()
        prepopulatedFormModel.year = editingMember.dateOfBirth.year.toString()
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: OrgGovBodyMembersState) = "forms/dateForm"

    override fun mode(state: OrgGovBodyMembersState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getEditingMemberOrNull(state: OrgGovBodyMembersState) =
        state.editingGovBodyMemberId?.let { state.governingBodyMembersMap?.get(it) }
}

@JourneyFrameworkComponent
final class OrgGovBodyMemberDobStep(
    stepConfig: OrgGovBodyMemberDobStepConfig,
) : RequestableStep<Complete, OrgGovBodyMemberDobFormModel, OrgGovBodyMembersState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-member-dob"
    }
}
