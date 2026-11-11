package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.SuperiorLandlordSummaryCardHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class SuperiorLandlordsListStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
    private val superiorLandlordSummaryCardHelper: SuperiorLandlordSummaryCardHelper,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, SuperiorLandlordsDetailsState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: SuperiorLandlordsDetailsState) =
        mapOf(
            "addAnotherTitle" to "registerProperty.superiorLandlords.list.heading",
            "optionalAddAnotherTitleParam" to (state.superiorLandlordsMap?.size ?: 0),
            "insetText" to "registerProperty.superiorLandlords.list.insetText",
            "summaryCards" to getSuperiorLandlordCards(state),
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "registerProperty.superiorLandlords.list.buttons.addAnother",
            "addAnotherUrl" to Destination(state.superiorLandlordProvideDetailsStep).toUrlStringOrNull(),
        )

    private fun getSuperiorLandlordCards(state: SuperiorLandlordsDetailsState): List<SummaryCardViewModel> =
        superiorLandlordSummaryCardHelper.getSummaryCards(state.superiorLandlordsMap ?: emptyMap()) { superiorLandlordId ->
            getCardActions(state, superiorLandlordId)
        }

    private fun getCardActions(
        state: SuperiorLandlordsDetailsState,
        superiorLandlordId: Int,
    ): List<SummaryCardActionViewModel> {
        val urlParameter = urlParameterService.createParameterPair(superiorLandlordId)
        val changeUrl = Destination(state.setStateForSuperiorLandlordEditStep).withUrlParameter(urlParameter).toUrlStringOrNull()
        val removeUrl = Destination(state.confirmRemoveSuperiorLandlordStep).withUrlParameter(urlParameter).toUrlStringOrNull()
        return listOfNotNull(
            changeUrl?.let { SummaryCardActionViewModel(text = "forms.links.change", url = it) },
            removeUrl?.let { SummaryCardActionViewModel(text = "forms.links.remove", url = it) },
        )
    }

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/addAnotherCardsForm"

    override fun mode(state: SuperiorLandlordsDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepIsReached(state: SuperiorLandlordsDetailsState) {
        // Ensure that if you ever get to this page we reset any state that is used by one of the buttons.
        // This means we can be certain all the buttons will always work even if you use the browser back buttons.
        state.editingSuperiorLandlordId = null
    }
}

@JourneyFrameworkComponent
final class SuperiorLandlordsListStep(
    stepConfig: SuperiorLandlordsListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, SuperiorLandlordsDetailsState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlords-list"
    }
}
