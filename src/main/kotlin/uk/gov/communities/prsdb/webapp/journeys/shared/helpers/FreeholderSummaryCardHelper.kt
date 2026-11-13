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
                buildSummaryCard(freeholderId.toString(), freeholder, getActions(freeholderId))
            }

    fun getSummaryCards(freeholders: Collection<Freeholder>): List<SummaryCardViewModel> =
        freeholders
            .sortedBy { it.id }
            .map { freeholder ->
                val card =
                    buildSummaryCard(
                        freeholder.id.toString(),
                        FreeholderDataModel.fromFreeholder(freeholder),
                        emptyList(),
                        includePostalAddress = false,
                    )
                if (freeholder.detailsKnown() && freeholder.address != null) {
                    card.copy(bodyTextKey = "propertyDetails.peopleResponsible.freeholder.postalAddressNote")
                } else {
                    card
                }
            }

    private fun buildSummaryCard(
        freeholderId: String,
        freeholder: FreeholderDataModel,
        actions: List<SummaryCardActionViewModel>,
        includePostalAddress: Boolean = true,
    ): SummaryCardViewModel =
        if (freeholder.detailsKnown) {
            SummaryCardViewModel(
                title = freeholder.name ?: throw PrsdbWebException("Freeholder $freeholderId has known details but no name"),
                summaryList = getContactRows(freeholder, includePostalAddress),
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

    private fun getContactRows(
        freeholder: FreeholderDataModel,
        includePostalAddress: Boolean,
    ): List<SummaryListRowViewModel> =
        listOfNotNull(
            freeholder.email?.let {
                SummaryListRowViewModel(fieldHeading = "registerProperty.freeholders.list.emailAddress", fieldValue = it)
            },
            freeholder.address?.takeIf { includePostalAddress }?.let {
                SummaryListRowViewModel(
                    fieldHeading = "registerProperty.freeholders.list.postalAddress",
                    fieldValue = it.toMultiLineAddress().split("\n"),
                )
            },
        ).ifEmpty {
            if (!includePostalAddress && freeholder.address != null) {
                emptyList()
            } else {
                throw PrsdbWebException("Freeholder has known details but no email or postal address")
            }
        }
}
