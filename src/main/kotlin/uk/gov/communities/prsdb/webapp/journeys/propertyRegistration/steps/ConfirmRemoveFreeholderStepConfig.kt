package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RemoveFreeholderFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class ConfirmRemoveFreeholderStepConfig(
    private val collectionKeyParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<AnyMembers, RemoveFreeholderFormModel, FreeholdersDetailsState>() {
    override val formModelClass = RemoveFreeholderFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState): Map<String, Any?> {
        val freeholderToRemove =
            collectionKeyParameterService
                .getParameterOrNull()
                ?.let { state.freeholdersMap?.get(it) }
        val freeholderName = freeholderToRemove?.takeIf { it.detailsKnown }?.name

        return mapOf(
            "fieldSetHeading" to
                if (freeholderName != null) {
                    "registerProperty.freeholders.remove.fieldSetHeading"
                } else {
                    "registerProperty.freeholders.remove.unknownFreeholder.fieldSetHeading"
                },
            "optionalFieldSetHeadingParam" to freeholderName.orEmpty(),
            "fieldSetHint" to "registerProperty.freeholders.remove.fieldSetHint",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "submitButtonTextKey" to "forms.buttons.saveAndContinue",
            "showSectionHeader" to true,
        )
    }

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/areYouSureForm"

    override fun beforeAttemptingToReachStep(state: FreeholdersDetailsState): Boolean {
        val keyToRemove = collectionKeyParameterService.getParameterOrNull()
        val currentMap = state.freeholdersMap ?: emptyMap()
        return keyToRemove != null && keyToRemove in currentMap.keys && (currentMap.size > 1 || state.allowRemovingLastMember)
    }

    override fun afterStepDataIsAdded(state: FreeholdersDetailsState) {
        val wantsToProceed = getFormModelFromStateOrNull(state)?.wantsToProceed
        state.confirmRemoveFreeholderStep.clearFormData()
        if (wantsToProceed != true) return

        val keyToRemove = collectionKeyParameterService.getParameterOrNull() ?: return
        val currentMap = state.freeholdersMap?.toMutableMap() ?: return
        currentMap.remove(keyToRemove)
        state.freeholdersMap = currentMap
    }

    override fun mode(state: FreeholdersDetailsState): AnyMembers =
        if (state.freeholdersMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
final class ConfirmRemoveFreeholderStep(
    stepConfig: ConfirmRemoveFreeholderStepConfig,
) : RequestableStep<AnyMembers, RemoveFreeholderFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-freeholder"
    }
}
