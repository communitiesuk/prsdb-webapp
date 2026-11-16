package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ConfirmRemoveSuperiorLandlordFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class ConfirmRemoveSuperiorLandlordStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<AnyMembers, ConfirmRemoveSuperiorLandlordFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = ConfirmRemoveSuperiorLandlordFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState): Map<String, Any?> {
        val superiorLandlordName = getSuperiorLandlordToRemove(state)?.takeIf { it.detailsKnown }?.name
        return mapOf(
            "fieldSetHeading" to
                if (superiorLandlordName == null) {
                    "registerProperty.superiorLandlords.remove.unknownSuperiorLandlord.fieldSetHeading"
                } else {
                    "registerProperty.superiorLandlords.remove.fieldSetHeading"
                },
            "optionalFieldSetHeadingParam" to superiorLandlordName,
            "fieldSetHint" to "registerProperty.superiorLandlords.remove.fieldSetHint",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "submitButtonTextKey" to "forms.buttons.saveAndContinue",
        )
    }

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/areYouSureForm"

    override fun beforeAttemptingToReachStep(state: SuperiorLandlordsDetailsState): Boolean {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.superiorLandlordsMap ?: emptyMap()
        return keyToRemove != null && keyToRemove in currentMap.keys
    }

    override fun afterStepDataIsAdded(state: SuperiorLandlordsDetailsState) {
        val wantsToProceed = getFormModelFromStateOrNull(state)?.wantsToProceed
        // Clear to stop it being pre-selected on later visits
        state.confirmRemoveSuperiorLandlordStep.clearFormData()
        if (wantsToProceed != true) return
        val keyToRemove = collectionKeyParameterService.getParameterOrNull() ?: return
        val currentMap = state.superiorLandlordsMap?.toMutableMap() ?: return
        currentMap.remove(keyToRemove)
        state.superiorLandlordsMap = currentMap
    }

    override fun mode(state: SuperiorLandlordsDetailsState): AnyMembers =
        if (state.superiorLandlordsMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS

    private fun getSuperiorLandlordToRemove(state: SuperiorLandlordsDetailsState): SuperiorLandlordDataModel? =
        state.superiorLandlordsMap?.get(collectionKeyParameterService.getParameterOrNull())
}

@JourneyFrameworkComponent
final class ConfirmRemoveSuperiorLandlordStep(
    stepConfig: ConfirmRemoveSuperiorLandlordStepConfig,
) : RequestableStep<AnyMembers, ConfirmRemoveSuperiorLandlordFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-superior-landlord"
    }
}
