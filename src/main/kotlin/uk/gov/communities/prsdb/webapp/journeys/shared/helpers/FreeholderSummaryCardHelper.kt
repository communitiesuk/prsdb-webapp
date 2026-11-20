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
                val actions = getActions(freeholderId)
                if (freeholder.detailsKnown) {
                    val contactRows = getContactRows(freeholder)
                    SummaryCardViewModel(
                        title = freeholder.name ?: throw PrsdbWebException("Freeholder $freeholderId has known details but no name"),
                        summaryList = contactRows,
                        actions = actions,
                        bodyTextKey = if (freeholder.hasUnstoredPostalAddress()) POSTAL_ADDRESS_NOTE_KEY else null,
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

    fun getSummaryCards(freeholders: Collection<Freeholder>): List<SummaryCardViewModel> =
        getSummaryCards(
            freeholders
                .sortedBy { it.id }
                .withIndex()
                .associate { (index, freeholder) -> index to FreeholderDataModel.fromFreeholder(freeholder) },
        )

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
