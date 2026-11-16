package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class SuperiorLandlordSummaryCardHelper {
    fun getSummaryCards(
        superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>,
        getActions: (superiorLandlordId: Int) -> List<SummaryCardActionViewModel> = { emptyList() },
    ): List<SummaryCardViewModel> =
        superiorLandlordsMap
            .toSortedMap()
            .map { (superiorLandlordId, superiorLandlord) ->
                val actions = getActions(superiorLandlordId)
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
}
