package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@JourneyFrameworkComponent
class OrgGovBodyMemberListStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, LandlordRegistrationOrgLandlordState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LandlordRegistrationOrgLandlordState) =
        mapOf(
            "addAnotherTitle" to "forms.orgGovBodyMemberList.heading",
            "optionalAddAnotherTitleParam" to (state.governingBodyMembersMap?.size ?: 0),
            "summaryText" to null,
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.continue",
            "addAnotherButtonText" to "forms.orgGovBodyMemberList.buttons.addAnother",
            "summaryListData" to getMemberRows(state),
            // TODO: PDJB-1290 - Replace with real "add another" URL once implemented
            "addAnotherUrl" to "#",
        )

    private fun getMemberRows(state: LandlordRegistrationOrgLandlordState): List<SummaryListRowViewModel> {
        val membersMap = state.governingBodyMembersMap ?: emptyMap()
        return membersMap
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (_, member) ->
                SummaryListRowViewModel(
                    fieldHeading = "forms.orgGovBodyMemberList.memberName",
                    fieldValue = member.name,
                    optionalFieldHeadingParam = displayIndex + 1,
                    actions =
                        listOf(
                            // TODO: PDJB-1290 - Replace with real change URL
                            SummaryListRowActionsViewModel(text = "forms.links.change", url = "#"),
                            // TODO: PDJB-1290 - Replace with real remove URL
                            SummaryListRowActionsViewModel(text = "forms.links.remove", url = "#"),
                        ),
                )
            }
    }

    override fun chooseTemplate(state: LandlordRegistrationOrgLandlordState): String = "forms/addAnotherForm"

    override fun mode(state: LandlordRegistrationOrgLandlordState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyMemberListStep(
    stepConfig: OrgGovBodyMemberListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, LandlordRegistrationOrgLandlordState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-member-list"
    }
}
