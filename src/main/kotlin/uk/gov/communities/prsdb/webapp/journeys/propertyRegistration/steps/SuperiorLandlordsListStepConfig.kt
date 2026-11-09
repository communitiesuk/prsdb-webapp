package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class SuperiorLandlordsListStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
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

    private fun getSuperiorLandlordCards(state: SuperiorLandlordsDetailsState): List<SummaryCardViewModel> {
        val superiorLandlordsMap = state.superiorLandlordsMap ?: emptyMap()
        return superiorLandlordsMap
            .toSortedMap()
            .map { (superiorLandlordId, superiorLandlord) ->
                val actions = getCardActions(state, superiorLandlordId)
                if (superiorLandlord.detailsKnown) {
                    SummaryCardViewModel(
                        title =
                            superiorLandlord.name
                                ?: throw PrsdbWebException("Superior landlord $superiorLandlordId has known details but no name"),
                        summaryList = getContactRows(superiorLandlord),
                        actions = actions,
                    )
                } else {
                    SummaryCardViewModel(
                        title = "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title",
                        summaryList = emptyList(),
                        actions = actions,
                        bodyTextKey = "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.body",
                    )
                }
            }
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

    private fun getContactRows(superiorLandlord: SuperiorLandlordDataModel): List<SummaryListRowViewModel> =
        listOfNotNull(
            superiorLandlord.email?.let {
                SummaryListRowViewModel(fieldHeading = "registerProperty.superiorLandlords.list.emailAddress", fieldValue = it)
            },
            superiorLandlord.address?.let {
                SummaryListRowViewModel(
                    fieldHeading = "registerProperty.superiorLandlords.list.postalAddress",
                    fieldValue = it.toMultiLineAddress().split("\n"),
                )
            },
        ).ifEmpty { throw PrsdbWebException("Superior landlord has known details but no email or postal address") }

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
