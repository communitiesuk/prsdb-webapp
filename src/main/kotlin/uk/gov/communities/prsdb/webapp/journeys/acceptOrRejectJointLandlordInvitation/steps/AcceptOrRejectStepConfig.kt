package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GOV_LEGAL_ADVICE_URL
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AcceptOrRejectFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import java.util.UUID

@JourneyFrameworkComponent
class AcceptOrRejectStepConfig(
    private val jointLandlordInvitationRepository: JointLandlordInvitationRepository,
) : AbstractRequestableStepConfig<YesOrNo, AcceptOrRejectFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override val formModelClass = AcceptOrRejectFormModel::class

    override fun getStepSpecificContent(state: AcceptOrRejectJointLandlordInvitationJourneyState): Map<String, Any?> {
        val invitation =
            jointLandlordInvitationRepository.findByToken(UUID.fromString(state.invitationToken))
                ?: throw PrsdbWebException("Invitation not found for token ${state.invitationToken}")

        // TODO PDJB-260 - update where the landlord name comes from
        val inviterName = "HARDCODED - INVITING LANDLORD NAME"

        return mapOf(
            "heading" to "acceptOrRejectJointLandlordInvitation.acceptOrReject.heading",
            "inviterName" to inviterName,
            "propertyAddress" to invitation.registeredOwnership.address.toMultiLineAddress().split("\n"),
            "fieldSetHeading" to "acceptOrRejectJointLandlordInvitation.acceptOrReject.radios.fieldSetHeading",
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(yesLabel = "acceptOrRejectJointLandlordInvitation.acceptOrReject.radios.yes.label"),
            "findLegalAdviceUrl" to GOV_LEGAL_ADVICE_URL,
        )
    }

    override fun chooseTemplate(state: AcceptOrRejectJointLandlordInvitationJourneyState) =
        "forms/acceptOrRejectJointLandlordInvitationForm"

    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): YesOrNo? =
        getFormModelFromStateOrNull(state)?.isLandlordForThisProperty?.let {
            when (it) {
                true -> YesOrNo.YES
                false -> YesOrNo.NO
            }
        }
}

@JourneyFrameworkComponent
final class AcceptOrRejectStep(
    stepConfig: AcceptOrRejectStepConfig,
) : RequestableStep<YesOrNo, AcceptOrRejectFormModel, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "accept-or-reject"
    }
}
