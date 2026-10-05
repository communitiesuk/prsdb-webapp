package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.INVALID_LINK_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("acceptInvitationInvalidLinkStepConfig")
class InvalidLinkStepConfig :
    AbstractRequestableStepConfig<Nothing, NoInputFormModel, AcceptInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1821: Invalid invitation link page
    override fun getStepSpecificContent(state: AcceptInvitationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Invalid link (TODO PDJB-1821)")

    override fun chooseTemplate(state: AcceptInvitationJourneyState) = "forms/todoNoButton"

    override fun mode(state: AcceptInvitationJourneyState) = null
}

@JourneyFrameworkComponent("acceptInvitationInvalidLinkStep")
final class InvalidLinkStep(
    stepConfig: InvalidLinkStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, AcceptInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = INVALID_LINK_PAGE_PATH_SEGMENT
    }
}
