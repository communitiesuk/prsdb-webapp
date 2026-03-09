package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsInputWithDestination
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class CheckJointLandlordsStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, JointLandlordsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
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

    private fun getEmailRows(state: JointLandlordsState): List<SummaryListRowViewModel> {
        val invitedEmails = state.invitedJointLandlordEmailsMap ?: emptyMap()
        return invitedEmails
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (internalKey, email) ->
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
                                    ).withUrlParameter(urlParameterService.createParameterPair(internalKey)),
                            ),
                            SummaryListRowActionsInputWithDestination(
                                text = "forms.links.remove",
                                destination =
                                    Destination(
                                        state.removeJointLandlordStep,
                                    ).withUrlParameter(urlParameterService.createParameterPair(internalKey)),
                            ),
                        ),
                    optionalFieldHeadingParam = displayIndex + 1,
                )
            }
    }

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/addAnotherForm"

    override fun mode(state: JointLandlordsState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    private fun getJointLandlordsCount(state: JointLandlordsState): Int = getEmailRows(state).size
}

@JourneyFrameworkComponent
final class CheckJointLandlordsStep(
    stepConfig: CheckJointLandlordsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JointLandlordsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-joint-landlords"
    }
}
