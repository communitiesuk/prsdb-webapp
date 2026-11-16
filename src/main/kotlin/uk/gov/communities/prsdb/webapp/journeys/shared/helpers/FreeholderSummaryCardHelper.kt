package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
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
                    SummaryCardViewModel(
                        title = freeholder.name ?: throw PrsdbWebException("Freeholder $freeholderId has known details but no name"),
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
        ).ifEmpty { throw PrsdbWebException("Freeholder has known details but no email or postal address") }
}
