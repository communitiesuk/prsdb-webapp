package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.Freeholder
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class FreeholderSummaryCardHelper {
    fun getSummaryCards(
        freeholdersMap: Map<Int, FreeholderDataModel>,
        getActions: (freeholderId: Int) -> List<SummaryCardActionViewModel> = { emptyList() },
    ): List<SummaryCardViewModel> =
        freeholdersMap
            .toSortedMap()
            .map { (freeholderId, freeholder) ->
                buildSummaryCard(
                    freeholderId.toString(),
                    freeholder,
                    getActions(freeholderId),
                )
            }

    fun getSummaryCards(freeholders: Collection<Freeholder>): List<SummaryCardViewModel> =
        freeholders
            .sortedBy { it.id }
            .map { freeholder ->
                buildSummaryCard(
                    freeholder.id.toString(),
                    FreeholderDataModel.fromFreeholder(freeholder),
                    emptyList(),
                )
            }

    private fun buildSummaryCard(
        freeholderId: String,
        freeholder: FreeholderDataModel,
        actions: List<SummaryCardActionViewModel>,
    ): SummaryCardViewModel {
        if (!freeholder.detailsKnown) {
            return SummaryCardViewModel(
                title = "registerProperty.freeholders.list.unknownFreeholder.title",
                summaryList = emptyList(),
                actions = actions,
                bodyTextKey = "registerProperty.freeholders.list.unknownFreeholder.body",
            )
        }

        val name = freeholder.name ?: throw PrsdbWebException("Freeholder $freeholderId has known details but no name")
        if (freeholder.email != null && freeholder.address != null) {
            throw PrsdbWebException("Freeholder $freeholderId has both an email and postal address")
        }

        // Postal addresses aren't stored, so a saved freeholder contacted by post has neither an email nor an address
        val showNote = freeholder.email == null && freeholder.address == null
        return SummaryCardViewModel(
            title = name,
            summaryList = getContactRows(freeholder),
            actions = actions,
            bodyTextKey = if (showNote) POSTAL_ADDRESS_NOTE_KEY else null,
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

    companion object {
        const val POSTAL_ADDRESS_NOTE_KEY = "propertyDetails.peopleResponsible.freeholder.postalAddressNote"
    }
}
