package uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.CancelOrganisationalLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CancelOrganisationalLandlordInvitationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent("cancelOrganisationalLandlordInvitationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<
        AreYouSureMode,
        CancelOrganisationalLandlordInvitationAreYouSureFormModel,
        CancelOrganisationalLandlordInvitationJourneyState,
        >() {
    override val formModelClass = CancelOrganisationalLandlordInvitationAreYouSureFormModel::class

    override fun getStepSpecificContent(state: CancelOrganisationalLandlordInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesLabel = "cancelOrganisationalLandlordInvitation.areYouSure.radios.yes.label",
                    noLabel = "cancelOrganisationalLandlordInvitation.areYouSure.radios.no.label",
                ),
            "fieldSetHeading" to "cancelOrganisationalLandlordInvitation.areYouSure.fieldSetHeading",
            "optionalFieldSetHeadingParam" to state.invitedEmail,
        )

    override fun chooseTemplate(state: CancelOrganisationalLandlordInvitationJourneyState) = "forms/areYouSureForm"

    override fun mode(state: CancelOrganisationalLandlordInvitationJourneyState): AreYouSureMode? =
        getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
            if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
        }
}

@JourneyFrameworkComponent("cancelOrganisationalLandlordInvitationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<
        AreYouSureMode,
        CancelOrganisationalLandlordInvitationAreYouSureFormModel,
        CancelOrganisationalLandlordInvitationJourneyState,
        >(
        stepConfig,
    ) {
    companion object {
        const val ROUTE_SEGMENT = "are-you-sure"
    }
}

enum class AreYouSureMode {
    WANTS_TO_PROCEED,
    DOES_NOT_WANT_TO_PROCEED,
}
