package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.CurrentUserService
import uk.gov.communities.prsdb.webapp.services.OrganisationPermissionsProvider
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationConfirmationStepConfig")
class ConfirmationStepConfig(
    private val userToLandlordService: UserToLandlordService,
    private val currentUserService: CurrentUserService,
    private val organisationPermissionsProvider: OrganisationPermissionsProvider,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "organisationName" to userToLandlordService.getCurrentOrganisationLandlordForUser().name,
            "emailAddress" to currentUserService.getCurrentEmail(),
            "isAdmin" to organisationPermissionsProvider.canCurrentUserPerformOrgAdminActions(),
            "dashboardUrl" to LANDLORD_DASHBOARD_URL,
        )

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) =
        "acceptOrganisationInvitationConfirmation"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Complete = Complete.COMPLETE
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationConfirmationStep")
final class ConfirmationStep(
    stepConfig: ConfirmationStepConfig,
) : RequestableStep<Complete, NoInputFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = CONFIRMATION_PATH_SEGMENT
    }
}
