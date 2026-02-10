package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

// TODO PDJB-114: Implement CheckJointLandlordsStep
@JourneyFrameworkComponent
class CheckJointLandlordsConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JointLandlordsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "summaryName" to "TODO PDJB-114: Implement check joint landlords page",
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.continue",
            "summaryListData" to getEmailRows(state),
        )

    private fun getEmailRows(state: JointLandlordsState): List<SummaryListRowViewModel> {
        val invitedEmails = state.invitedJointLandlordEmails ?: emptyList()
        return invitedEmails.map { email ->
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "jointLandlords.checkJointLandlords.invitedEmailAddress",
                email,
                null,
            )
        }
    }

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/checkAnswersForm"

    override fun mode(state: JointLandlordsState) = Complete.COMPLETE
}

@JourneyFrameworkComponent
final class CheckJointLandlordsStep(
    stepConfig: CheckJointLandlordsConfig,
) : RequestableStep<Complete, NoInputFormModel, JointLandlordsState>(stepConfig)
