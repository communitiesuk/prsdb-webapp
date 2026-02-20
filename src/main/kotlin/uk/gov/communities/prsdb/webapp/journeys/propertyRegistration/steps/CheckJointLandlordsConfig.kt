package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.ArrayIndexParameterService

@JourneyFrameworkComponent
class CheckJointLandlordsConfig(
    private val urlParameterService: ArrayIndexParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, JointLandlordsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JointLandlordsState) =
        mapOf(
            "addAnotherTitle" to "jointLandlords.checkJointLandlords.heading",
            "summaryName" to "jointLandlords.checkJointLandlords.summary",
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.continue",
            "summaryListData" to getEmailRows(state),
            "addAnotherUrl" to Destination(state.inviteAnotherJointLandlordStep).toUrlStringOrNull(),
        )

    private fun getEmailRows(state: JointLandlordsState): List<SummaryListRowViewModel> {
        val invitedEmails = state.invitedJointLandlords
        return invitedEmails.mapIndexed { index, email ->
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "jointLandlords.checkJointLandlords.invitedEmailAddress",
                email,
                Destination(state.removeJointLandlordStep).withUrlParameter(urlParameterService.createParameterPair(index)),
                actionValue = "forms.links.remove",
            )
        }
    }

    override fun chooseTemplate(state: JointLandlordsState): String = "forms/addAnotherForm"

    override fun mode(state: JointLandlordsState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class CheckJointLandlordsStep(
    stepConfig: CheckJointLandlordsConfig,
) : RequestableStep<Complete, NoInputFormModel, JointLandlordsState>(stepConfig)
