package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationInvalidLinkStepConfig")
class InvalidLinkStepConfig :
    AbstractRequestableStepConfig<Nothing, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1821: Invalid invitation link page
    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Invalid link (TODO PDJB-1821)")

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/todoNoButton"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = null
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationInvalidLinkStep")
final class InvalidLinkStep(
    stepConfig: InvalidLinkStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = INVALID_LINK_PAGE_PATH_SEGMENT
    }
}
