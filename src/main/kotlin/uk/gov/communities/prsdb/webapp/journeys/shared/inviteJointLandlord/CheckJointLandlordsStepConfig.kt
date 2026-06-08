package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.SharedInviteJointLandlordState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsInputWithDestination
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class CheckJointLandlordsStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, SharedInviteJointLandlordState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: SharedInviteJointLandlordState) =
        mapOf(
            "addAnotherTitle" to "jointLandlords.checkJointLandlords.heading",
            "optionalAddAnotherTitleParam" to getJointLandlordsCount(state),
            "summaryText" to "jointLandlords.checkJointLandlords.paragraph",
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "jointLandlords.checkJointLandlords.buttons.addAnother",
            "summaryListData" to getEmailRows(state),
            "addAnotherUrl" to Destination(state.inviteAnotherJointLandlordStep).toUrlStringOrNull(),
        )

    private fun getEmailRows(state: SharedInviteJointLandlordState): List<SummaryListRowViewModel> {
        val invitedEmails = state.invitedJointLandlordEmailsMap ?: emptyMap()
        return invitedEmails
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (internalIndex, email) ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "jointLandlords.checkJointLandlords.invitedEmailAddress",
                    email,
                    actions =
                        listOf(
                            SummaryListRowActionsInputWithDestination(
                                text = "forms.links.change",
                                destination =
                                    Destination(
                                        state.inviteAnotherJointLandlordStep,
                                    ).withUrlParameter(urlParameterService.createParameterPair(internalIndex)),
                            ),
                            SummaryListRowActionsInputWithDestination(
                                text = "forms.links.remove",
                                destination =
                                    Destination(
                                        state.removeJointLandlordAreYouSureStep,
                                    ).withUrlParameter(urlParameterService.createParameterPair(internalIndex)),
                            ),
                        ),
                    optionalFieldHeadingParam = displayIndex + 1,
                )
            }
    }

    override fun chooseTemplate(state: SharedInviteJointLandlordState): String = "forms/addAnotherForm"

    override fun mode(state: SharedInviteJointLandlordState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getJointLandlordsCount(state: SharedInviteJointLandlordState): Int = getEmailRows(state).size
}

@JourneyFrameworkComponent
final class CheckJointLandlordsStep(
    stepConfig: CheckJointLandlordsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, SharedInviteJointLandlordState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-joint-landlords"
    }
}
