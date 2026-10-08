package uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.inviteOrganisationalLandlordUser.InviteOrganisationalLandlordUserJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.InviteOrganisationalLandlordUserFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class InviteOrganisationalLandlordUserStepConfig(
    private val userToLandlordService: UserToLandlordService,
) : AbstractRequestableStepConfig<Complete, InviteOrganisationalLandlordUserFormModel, InviteOrganisationalLandlordUserJourneyState>() {
    override val formModelClass = InviteOrganisationalLandlordUserFormModel::class

    override fun getStepSpecificContent(state: InviteOrganisationalLandlordUserJourneyState): Map<String, Any?> =
        mapOf(
            "organisationName" to userToLandlordService.getCurrentOrganisationLandlordForUser().name,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = OrganisationalLandlordUserRole.ADMIN,
                        labelMsgKey = "inviteOrganisationalLandlordUser.role.radios.admin.label",
                        hintMsgKey = "inviteOrganisationalLandlordUser.role.radios.admin.hint",
                    ),
                    RadiosButtonViewModel(
                        value = OrganisationalLandlordUserRole.EDITOR,
                        labelMsgKey = "inviteOrganisationalLandlordUser.role.radios.editor.label",
                        hintMsgKey = "inviteOrganisationalLandlordUser.role.radios.editor.hint",
                    ),
                ),
        )

    override fun chooseTemplate(state: InviteOrganisationalLandlordUserJourneyState) = "forms/inviteOrganisationalLandlordUserForm"

    override fun mode(state: InviteOrganisationalLandlordUserJourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            Complete.COMPLETE
        }
}

@JourneyFrameworkComponent
final class InviteOrganisationalLandlordUserStep(
    stepConfig: InviteOrganisationalLandlordUserStepConfig,
) : RequestableStep<Complete, InviteOrganisationalLandlordUserFormModel, InviteOrganisationalLandlordUserJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "invite-team-member"
    }
}
