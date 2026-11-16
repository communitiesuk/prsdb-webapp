package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.FreeholderSummaryCardHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class FreeholdersListStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
    private val freeholderSummaryCardHelper: FreeholderSummaryCardHelper,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, FreeholdersDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FreeholdersDetailsState) =
        mapOf(
            "addAnotherTitle" to "registerProperty.freeholders.list.heading.${if (state.isJointFreeholder) "joint" else "nonJoint"}",
            "optionalAddAnotherTitleParam" to (state.freeholdersMap?.size ?: 0),
            "insetText" to "registerProperty.freeholders.list.insetText",
            "summaryCards" to getFreeholderCards(state),
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "registerProperty.freeholders.list.buttons.addAnother",
            "addAnotherUrl" to Destination(state.freeholderProvideDetailsStep).toUrlStringOrNull(),
        )

    private fun getFreeholderCards(state: FreeholdersDetailsState): List<SummaryCardViewModel> {
        val freeholdersMap = state.freeholdersMap ?: emptyMap()
        val showRemove = freeholdersMap.size > 1 || state.allowRemovingLastMember
        return freeholderSummaryCardHelper.getSummaryCards(freeholdersMap) { freeholderId ->
            getCardActions(state, freeholderId, showRemove)
        }
    }

    private fun getCardActions(
        state: FreeholdersDetailsState,
        freeholderId: Int,
        showRemove: Boolean,
    ): List<SummaryCardActionViewModel> {
        val urlParameter = urlParameterService.createParameterPair(freeholderId)
        val changeUrl = Destination(state.setStateForFreeholderEditStep).withUrlParameter(urlParameter).toUrlStringOrNull()
        val removeUrl =
            if (showRemove) Destination(state.confirmRemoveFreeholderStep).withUrlParameter(urlParameter).toUrlStringOrNull() else null
        return listOfNotNull(
            changeUrl?.let { SummaryCardActionViewModel(text = "forms.links.change", url = it) },
            removeUrl?.let { SummaryCardActionViewModel(text = "forms.links.remove", url = it) },
        )
    }

    override fun chooseTemplate(state: FreeholdersDetailsState) = "forms/addAnotherCardsForm"

    override fun mode(state: FreeholdersDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepIsReached(state: FreeholdersDetailsState) {
        // Ensure that if you ever get to this page we reset any state that is used by one of the buttons.
        // This means we can be certain all the buttons will always work even if you use the browser back buttons.
        state.editingFreeholderId = null
    }
}

@JourneyFrameworkComponent
final class FreeholdersListStep(
    stepConfig: FreeholdersListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, FreeholdersDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholders-list"
    }
}
