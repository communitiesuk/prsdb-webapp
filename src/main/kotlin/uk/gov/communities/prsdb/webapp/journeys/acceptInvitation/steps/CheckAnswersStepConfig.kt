package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptInvitationCheckAnswersStepConfig")
class CheckAnswersStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    // TODO PDJB-1774: This should inherit from AbstractCheckYourAnswersStepConfig rather than the generic requestable base class.
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1774: Invitees can check their answers
    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Check your answers (TODO PDJB-1774)")

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/todo"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("acceptInvitationCheckAnswersStep")
final class CheckAnswersStep(
    stepConfig: CheckAnswersStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = AbstractCheckYourAnswersStep.ROUTE_SEGMENT
    }
}
