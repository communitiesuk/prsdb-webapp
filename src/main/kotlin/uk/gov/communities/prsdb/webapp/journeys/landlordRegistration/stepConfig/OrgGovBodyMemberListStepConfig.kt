package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsInputWithDestination
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class OrgGovBodyMemberListStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, OrgGovBodyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: OrgGovBodyState) =
        mapOf(
            "addAnotherTitle" to "forms.orgGovBodyMemberList.heading",
            "optionalAddAnotherTitleParam" to (state.governingBodyMembersMap?.size ?: 0),
            "summaryText" to null,
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.continue",
            "addAnotherButtonText" to "forms.orgGovBodyMemberList.buttons.addAnother",
            "summaryListData" to getMemberRows(state),
            "addAnotherUrl" to Destination(state.orgGovBodyWhoToProvideStep).toUrlStringOrNull(),
        )

    override fun afterStepIsReached(state: OrgGovBodyState) {
        // ensure that if you ever get to this page we reset any state that is used by one of the buttons.
        // this means we can be certain all the buttons will always work even if you use the browser back buttons.
        state.editingGovBodyMemberId = null
    }

    private fun getMemberRows(state: OrgGovBodyState): List<SummaryListRowViewModel> {
        val membersMap = state.governingBodyMembersMap ?: emptyMap()
        return membersMap
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (internalIndex, member) ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    fieldHeading = "forms.orgGovBodyMemberList.memberName",
                    fieldValue = member.name,
                    actions =
                        listOf(
                            SummaryListRowActionsInputWithDestination(
                                text = "forms.links.change",
                                destination =
                                    Destination(state.setStateForGovBodyMemberEditStep)
                                        .withUrlParameter(urlParameterService.createParameterPair(internalIndex)),
                            ),
                            SummaryListRowActionsInputWithDestination(
                                text = "forms.links.remove",
                                destination =
                                    Destination(state.removeGovBodyMemberStep)
                                        .withUrlParameter(urlParameterService.createParameterPair(internalIndex)),
                            ),
                        ),
                    optionalFieldHeadingParam = displayIndex + 1,
                )
            }
    }

    override fun chooseTemplate(state: OrgGovBodyState): String = "forms/addAnotherForm"

    override fun mode(state: OrgGovBodyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyMemberListStep(
    stepConfig: OrgGovBodyMemberListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, OrgGovBodyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-member-list"
    }
}
