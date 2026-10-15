package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationUserIsAlreadyLandlordStepConfig")
class UserIsAlreadyLandlordStepConfig :
    AbstractRequestableStepConfig<Nothing, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) =
        "forms/acceptInvitationUserIsAlreadyLandlord"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = null
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationUserIsAlreadyLandlordStep")
final class UserIsAlreadyLandlordStep(
    stepConfig: UserIsAlreadyLandlordStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "already-landlord"
    }
}
