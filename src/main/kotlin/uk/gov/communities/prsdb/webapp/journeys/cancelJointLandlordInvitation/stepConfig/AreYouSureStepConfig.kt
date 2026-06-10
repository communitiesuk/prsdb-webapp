package uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CancelJointLandlordInvitationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent("cancelJointLandlordInvitationAreYouSureStepConfig")
class AreYouSureStepConfig :
    AbstractRequestableStepConfig<
        AreYouSureMode,
        CancelJointLandlordInvitationAreYouSureFormModel,
        CancelJointLandlordInvitationJourneyState,
        >() {
    override val formModelClass = CancelJointLandlordInvitationAreYouSureFormModel::class

    override fun getStepSpecificContent(state: CancelJointLandlordInvitationJourneyState): Map<String, Any?> =
        mapOf(
            "radioOptions" to
                RadiosViewModel.yesOrNoRadios(
                    yesLabel = "cancelJointLandlordInvitation.areYouSure.radios.yes.label",
                    yesHint = "cancelJointLandlordInvitation.areYouSure.radios.yes.hint",
                    noLabel = "cancelJointLandlordInvitation.areYouSure.radios.no.label",
                ),
            "fieldSetHeading" to "cancelJointLandlordInvitation.areYouSure.fieldSetHeading",
            "optionalFieldSetHeadingParam" to state.invitedEmail,
        )

    override fun chooseTemplate(state: CancelJointLandlordInvitationJourneyState) = "forms/areYouSureForm"

    override fun mode(state: CancelJointLandlordInvitationJourneyState): AreYouSureMode? =
        getFormModelFromStateOrNull(state)?.wantsToProceed?.let {
            if (it) AreYouSureMode.WANTS_TO_PROCEED else AreYouSureMode.DOES_NOT_WANT_TO_PROCEED
        }
}

@JourneyFrameworkComponent("cancelJointLandlordInvitationAreYouSureStep")
final class AreYouSureStep(
    stepConfig: AreYouSureStepConfig,
) : RequestableStep<AreYouSureMode, CancelJointLandlordInvitationAreYouSureFormModel, CancelJointLandlordInvitationJourneyState>(
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
