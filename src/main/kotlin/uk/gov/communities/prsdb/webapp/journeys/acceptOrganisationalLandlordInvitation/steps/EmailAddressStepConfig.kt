package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.EMAIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrganisationInvitationEmailFormModel
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationEmailAddressStepConfig")
class EmailAddressStepConfig(
    private val invitationService: OrganisationalLandlordInvitationService,
) : AbstractRequestableStepConfig<
        Complete,
        AcceptOrganisationInvitationEmailFormModel,
        AcceptOrganisationalLandlordUserInvitationJourneyState,
        >() {
    override val formModelClass = AcceptOrganisationInvitationEmailFormModel::class

    override fun getStepSpecificContent(state: AcceptOrganisationalLandlordUserInvitationJourneyState): Map<String, Any?> {
        val invitation =
            invitationService.getPendingInvitationForJourneyIdOrNull(state.journeyId)
                ?: throw PrsdbWebException("Could not find a pending invitation for invitation journey ${state.journeyId}")
        val roleHintMessageKey =
            if (invitation.role == OrganisationalLandlordUserRole.ADMIN) {
                "acceptOrganisationInvitation.email.hint.admin"
            } else {
                "acceptOrganisationInvitation.email.hint.editor"
            }
        return mapOf(
            "fieldSetHeading" to "forms.email.fieldSetHeading",
            "fieldSetHint" to roleHintMessageKey,
            "fieldSetHintIsHtml" to true,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: AcceptOrganisationalLandlordUserInvitationJourneyState) = "forms/emailForm"

    override fun mode(state: AcceptOrganisationalLandlordUserInvitationJourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            Complete.COMPLETE
        }
}

@JourneyFrameworkComponent("acceptOrganisationalLandlordInvitationEmailAddressStep")
final class EmailAddressStep(
    stepConfig: EmailAddressStepConfig,
) : RequestableStep<Complete, AcceptOrganisationInvitationEmailFormModel, AcceptOrganisationalLandlordUserInvitationJourneyState>(
        stepConfig,
    ) {
    companion object {
        const val ROUTE_SEGMENT = EMAIL_PATH_SEGMENT
    }
}
