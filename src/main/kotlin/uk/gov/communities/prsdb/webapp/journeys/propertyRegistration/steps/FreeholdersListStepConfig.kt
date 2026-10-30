package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersMembersState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

@JourneyFrameworkComponent
class FreeholdersListStepConfig(
    private val urlParameterService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, FreeholdersMembersState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FreeholdersMembersState) =
        mapOf(
            "addAnotherTitle" to "registerProperty.freeholders.list.heading.${if (state.isJointFreeholder) "joint" else "single"}",
            "optionalAddAnotherTitleParam" to temporaryFreeholders.size,
            "insetText" to "registerProperty.freeholders.list.insetText",
            "summaryCards" to getFreeholderCards(state),
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "registerProperty.freeholders.list.buttons.addAnother",
            "addAnotherUrl" to Destination(state.freeholderProvideDetailsStep).toUrlStringOrNull(),
        )

    private fun getFreeholderCards(state: FreeholdersMembersState): List<SummaryCardViewModel> {
        // TODO PDJB-1969: use state.freeholdersMap once freeholders are saved by the journey
        val freeholdersMap = temporaryFreeholders
        val showRemove = freeholdersMap.size > 1 || state.allowRemovingLastMember
        return freeholdersMap
            .toSortedMap()
            .map { (freeholderId, freeholder) ->
                val actions = getCardActions(state, freeholderId, showRemove)
                if (freeholder.detailsKnown) {
                    SummaryCardViewModel(
                        title = freeholder.name.orEmpty(),
                        summaryList = getContactRows(freeholder),
                        actions = actions,
                    )
                } else {
                    SummaryCardViewModel(
                        title = "registerProperty.freeholders.list.unknownFreeholder.title",
                        summaryList = emptyList(),
                        actions = actions,
                        bodyTextKey = "registerProperty.freeholders.list.unknownFreeholder.body",
                    )
                }
            }
    }

    private fun getCardActions(
        state: FreeholdersMembersState,
        freeholderId: Int,
        showRemove: Boolean,
    ): List<SummaryCardActionViewModel> {
        val urlParameter = urlParameterService.createParameterPair(freeholderId)
        val changeUrl = Destination(state.setStateForFreeholderEditStep).withUrlParameter(urlParameter).toUrlStringOrNull()
        val removeUrl =
            if (showRemove) Destination(state.removeFreeholderStep).withUrlParameter(urlParameter).toUrlStringOrNull() else null
        return listOfNotNull(
            changeUrl?.let { SummaryCardActionViewModel(text = "forms.links.change", url = it) },
            removeUrl?.let { SummaryCardActionViewModel(text = "forms.links.remove", url = it) },
        )
    }

    private fun getContactRows(freeholder: FreeholderDataModel): List<SummaryListRowViewModel> =
        listOfNotNull(
            freeholder.email?.let {
                SummaryListRowViewModel(fieldHeading = "registerProperty.freeholders.list.emailAddress", fieldValue = it)
            },
            freeholder.address?.let {
                SummaryListRowViewModel(
                    fieldHeading = "registerProperty.freeholders.list.postalAddress",
                    fieldValue = it.toMultiLineAddress().split("\n"),
                )
            },
        )

    override fun chooseTemplate(state: FreeholdersMembersState) = "forms/addAnotherCardsForm"

    override fun mode(state: FreeholdersMembersState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    companion object {
        // TODO PDJB-1969: remove these temporary freeholders once freeholders are saved by the journey
        private val temporaryFreeholders: Map<Int, FreeholderDataModel> =
            mapOf(
                1 to
                    FreeholderDataModel(
                        detailsKnown = true,
                        name = "James Hassan",
                        address =
                            AddressDataModel.fromManualAddressData(
                                addressLineOne = "36 Sunnyside Lane",
                                townOrCity = "Biggleswade",
                                postcode = "SG18 9EJ",
                            ),
                    ),
                2 to FreeholderDataModel(detailsKnown = true, name = "Billie Smith", email = "email@example.com"),
                3 to FreeholderDataModel(detailsKnown = false),
            )
    }
}

@JourneyFrameworkComponent
final class FreeholdersListStep(
    stepConfig: FreeholdersListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, FreeholdersMembersState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholders-list"
    }
}
