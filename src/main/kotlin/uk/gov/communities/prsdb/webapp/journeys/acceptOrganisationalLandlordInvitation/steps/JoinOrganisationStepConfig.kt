package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationJoinOrganisationStepConfig")
class JoinOrganisationStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "organisationName" to
                (
                    // TODO PDJB-1822: Cache the validated invitation or organisation name in journey state
                    // to avoid this repeated lookup.
                    invitationService.getInvitationForJourneyIdOrNull(state.journeyId)
                        ?.organisationalLandlord
                        ?.name
                        ?: throw PrsdbWebException("Could not find an organisation name for invitation journey ${state.journeyId}")
                ),
        )

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/joinOrganisationStart"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationJoinOrganisationStep")
final class JoinOrganisationStep(
    stepConfig: JoinOrganisationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "join-your-organisation"
    }
}
