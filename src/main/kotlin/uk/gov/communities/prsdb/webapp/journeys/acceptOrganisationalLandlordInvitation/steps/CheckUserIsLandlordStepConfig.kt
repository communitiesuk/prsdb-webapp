package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckUserIsLandlordStepConfig")
class CheckUserIsLandlordStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1829: Check whether the invited user is already a landlord user and branch accordingly
    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Check if the user is already a landlord user (TODO PDJB-1829)")

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/todo"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            Complete.COMPLETE
        }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationCheckUserIsLandlordStep")
final class CheckUserIsLandlordStep(
    stepConfig: CheckUserIsLandlordStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-if-user-is-landlord"
    }
}
