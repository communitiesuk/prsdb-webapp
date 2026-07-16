package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class OrgGovBodyMemberListStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, LandlordRegistrationOrgLandlordState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: LandlordRegistrationOrgLandlordState) =
        mapOf(
            "addAnotherTitle" to "forms.orgGovBodyMemberList.heading",
            "optionalAddAnotherTitleParam" to (state.governingBodyMembersMap?.size ?: 0),
            "summaryText" to null,
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "forms.orgGovBodyMemberList.buttons.addAnother",
            "summaryListData" to getMemberRows(state),
            "addAnotherUrl" to Destination(state.orgGovBodyWhoToProvideStep).toUrlStringOrNull(),
        )

    private fun getMemberRows(state: LandlordRegistrationOrgLandlordState): List<SummaryListRowViewModel> {
        val membersMap = state.governingBodyMembersMap ?: emptyMap()
        return membersMap
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (internalIndex, member) ->
                SummaryListRowViewModel(
                    fieldHeading = "forms.orgGovBodyMemberList.memberName",
                    fieldValue = member.name,
                    optionalFieldHeadingParam = displayIndex + 1,
                    actions =
                        listOf(
                            SummaryListRowActionsViewModel(
                                text = "forms.links.change",
                                url =
                                    Destination(state.setStateForGovBodyMemberEditStep)
                                        .withUrlParameter(urlParameterService.createParameterPair(internalIndex))
                                        .toUrlStringOrNull()
                                        ?: throw PrsdbWebException(
                                            "Unable to generate change URL for governing body member $internalIndex",
                                        ),
                            ),
                            SummaryListRowActionsViewModel(
                                text = "forms.links.remove",
                                url =
                                    Destination(state.removeGovBodyMemberStep)
                                        .withUrlParameter(urlParameterService.createParameterPair(internalIndex))
                                        .toUrlStringOrNull()
                                        ?: throw PrsdbWebException(
                                            "Unable to generate remove URL for governing body member $internalIndex",
                                        ),
                            ),
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
