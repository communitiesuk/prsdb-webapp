package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SharedInviteJointLandlordState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RemoveJointLandlordAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class RemoveJointLandlordAreYouSureStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<AnyLandlordsInvited, RemoveJointLandlordAreYouSureFormModel, SharedInviteJointLandlordState>() {
    override val formModelClass = RemoveJointLandlordAreYouSureFormModel::class

    override fun getStepSpecificContent(state: SharedInviteJointLandlordState) =
        mapOf(
            "fieldSetHeading" to "jointLandlords.removeJointLandlord.fieldSetHeading",
            "fieldSetHint" to "jointLandlords.removeJointLandlord.fieldSetHint",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "optionalFieldSetHeadingParam" to getLandlordEmailToRemove(state),
        )

    override fun chooseTemplate(state: SharedInviteJointLandlordState): String = "forms/areYouSureForm"

    override fun mode(state: SharedInviteJointLandlordState) =
        if (state.invitedJointLandlords.isEmpty()) {
            AnyLandlordsInvited.NO_LANDLORDS
        } else {
            AnyLandlordsInvited.SOME_LANDLORDS
        }

    override fun beforeAttemptingToReachStep(state: SharedInviteJointLandlordState): Boolean {
        val keyToRemove = urlParameterService.getParameterOrNull()
        val currentMap = state.invitedJointLandlordEmailsMap ?: emptyMap()

        return keyToRemove != null && keyToRemove in currentMap.keys
    }

    override fun afterStepDataIsAdded(state: SharedInviteJointLandlordState) {
        if (getFormModelFromStateOrNull(state)?.wantsToProceed == false) {
            return
        }
        val currentMap = state.invitedJointLandlordEmailsMap?.toMutableMap() ?: mutableMapOf()

        currentMap.remove(urlParameterService.getParameterOrNull())
        state.invitedJointLandlordEmailsMap = currentMap
    }

    private fun getLandlordEmailToRemove(state: SharedInviteJointLandlordState): String? {
        val keyToRemove = urlParameterService.getParameterOrNull()
        return state.invitedJointLandlordEmailsMap?.get(keyToRemove)
    }
}

@JourneyFrameworkComponent
final class RemoveJointLandlordAreYouSureStep(
    stepConfig: RemoveJointLandlordAreYouSureStepConfig,
) : RequestableStep<AnyLandlordsInvited, RemoveJointLandlordAreYouSureFormModel, SharedInviteJointLandlordState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-joint-landlord"
    }
}
