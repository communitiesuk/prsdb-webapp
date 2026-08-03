package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyMemberDobFormModel

@JourneyFrameworkComponent
class OrgGovBodyMemberDobStepConfig :
    AbstractRequestableStepConfig<Complete, OrgGovBodyMemberDobFormModel, LandlordRegistrationOrgLandlordState>() {
    override val formModelClass = OrgGovBodyMemberDobFormModel::class

    override fun getStepSpecificContent(state: LandlordRegistrationOrgLandlordState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgGovBodyMemberDob.fieldSetHeading",
            "fieldSetHint" to "registerAsALandlord.orgGovBodyMemberDob.fieldSetHint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun resolvePageContent(
        state: LandlordRegistrationOrgLandlordState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> {
        val formModel = defaultContent[FORM_MODEL_ATTR_NAME] as? OrgGovBodyMemberDobFormModel
        if (!formModel?.day.isNullOrBlank()) return defaultContent

        val editingMember = state.editingGovBodyMember ?: return defaultContent
        val prepopulatedFormModel = formModel ?: OrgGovBodyMemberDobFormModel()
        prepopulatedFormModel.day = editingMember.dateOfBirth.dayOfMonth.toString()
        prepopulatedFormModel.month = editingMember.dateOfBirth.monthNumber.toString()
        prepopulatedFormModel.year = editingMember.dateOfBirth.year.toString()
        return defaultContent + (FORM_MODEL_ATTR_NAME to prepopulatedFormModel)
    }

    override fun chooseTemplate(state: LandlordRegistrationOrgLandlordState) = "forms/dateForm"

    override fun mode(state: LandlordRegistrationOrgLandlordState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyMemberDobStep(
    stepConfig: OrgGovBodyMemberDobStepConfig,
) : RequestableStep<Complete, OrgGovBodyMemberDobFormModel, LandlordRegistrationOrgLandlordState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-member-dob"
    }
}
