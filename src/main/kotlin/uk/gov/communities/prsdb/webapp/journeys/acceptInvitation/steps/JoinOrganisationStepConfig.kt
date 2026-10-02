package uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptInvitation.AcceptInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@JourneyFrameworkComponent("acceptInvitationJoinOrganisationStepConfig")
class JoinOrganisationStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AcceptInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "organisationName" to
                (
                    invitationService.getOrganisationNameForJourneyIdOrNull(state.journeyId)
                        ?: throw PrsdbWebException("Organisation invitation name not found for journey ${state.journeyId}")
                ),
        )

    override fun chooseTemplate(state: AcceptInvitationJourneyState) = "forms/joinOrganisationStart"

    override fun mode(state: AcceptInvitationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("acceptInvitationJoinOrganisationStep")
final class JoinOrganisationStep(
    stepConfig: JoinOrganisationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "join-your-organisation"
    }
}
