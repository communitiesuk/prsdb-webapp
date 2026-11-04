package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
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
            "optionalAddAnotherTitleParam" to temporarySuperiorLandlords.size,
            "insetText" to "registerProperty.superiorLandlords.list.insetText",
            "summaryCards" to getSuperiorLandlordCards(state),
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "registerProperty.superiorLandlords.list.buttons.addAnother",
            "addAnotherUrl" to Destination(state.superiorLandlordProvideDetailsStep).toUrlStringOrNull(),
        )

    private fun getSuperiorLandlordCards(state: SuperiorLandlordsDetailsState): List<SummaryCardViewModel> {
        // TODO PDJB-1978: use state.superiorLandlordsMap once superior landlords are saved by the journey
        val superiorLandlordsMap = temporarySuperiorLandlords
        return superiorLandlordsMap
            .toSortedMap()
            .map { (superiorLandlordId, superiorLandlord) ->
                val actions = getCardActions(state, superiorLandlordId)
                if (superiorLandlord.detailsKnown) {
                    SummaryCardViewModel(
                        title = superiorLandlord.name.orEmpty(),
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
        )

    override fun chooseTemplate(state: SuperiorLandlordsDetailsState) = "forms/addAnotherCardsForm"

    override fun mode(state: SuperiorLandlordsDetailsState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    companion object {
        // TODO PDJB-1978: remove these temporary superior landlords once superior landlords are saved by the journey
        private val temporarySuperiorLandlords: Map<Int, SuperiorLandlordDataModel> =
            mapOf(
                1 to
                    SuperiorLandlordDataModel(
                        detailsKnown = true,
                        name = "James Hassan",
                        address =
                            AddressDataModel.fromManualAddressData(
                                addressLineOne = "36 Sunnyside Lane",
                                townOrCity = "Biggleswade",
                                postcode = "SG18 9EJ",
                            ),
                    ),
                2 to SuperiorLandlordDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com"),
                3 to SuperiorLandlordDataModel(detailsKnown = false),
            )
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
