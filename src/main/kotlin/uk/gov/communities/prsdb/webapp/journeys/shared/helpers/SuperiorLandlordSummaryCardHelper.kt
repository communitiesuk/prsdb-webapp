package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardActionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryCardViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class SuperiorLandlordSummaryCardHelper {
    fun getSummaryCards(
        superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>,
        unknownBodyTextKey: String = UNKNOWN_DETAILS_BODY_KEY,
        getActions: (superiorLandlordId: Int) -> List<SummaryCardActionViewModel> = { emptyList() },
    ): List<SummaryCardViewModel> =
        superiorLandlordsMap
            .toSortedMap()
            .map { (superiorLandlordId, superiorLandlord) ->
                val actions = getActions(superiorLandlordId)
                if (superiorLandlord.detailsKnown) {
                    val contactRows = getContactRows(superiorLandlord)
                    SummaryCardViewModel(
                        title =
                            superiorLandlord.name
                                ?: throw PrsdbWebException("Superior landlord $superiorLandlordId has known details but no name"),
                        summaryList = contactRows,
                        actions = actions,
                        bodyTextKey = if (superiorLandlord.hasUnstoredPostalAddress()) POSTAL_ADDRESS_NOTE_KEY else null,
                    )
                } else {
                    SummaryCardViewModel(
                        title = "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.title",
                        summaryList = emptyList(),
                        actions = actions,
                        bodyTextKey = unknownBodyTextKey,
                    )
                }
            }

    fun getSummaryCards(
        superiorLandlords: Collection<SuperiorLandlord>,
        unknownBodyTextKey: String,
    ): List<SummaryCardViewModel> =
        getSummaryCards(
            superiorLandlords
                .sortedBy { it.id }
                .withIndex()
                .associate { (index, superiorLandlord) -> index to SuperiorLandlordDataModel.fromSuperiorLandlord(superiorLandlord) },
            unknownBodyTextKey = unknownBodyTextKey,
        )

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

    companion object {
        const val UNKNOWN_DETAILS_BODY_KEY = "registerProperty.superiorLandlords.list.unknownSuperiorLandlord.body"
        const val COUNCIL_UNKNOWN_DETAILS_BODY_KEY = "propertyDetails.peopleResponsible.superiorLandlord.councilUnknownDetails"
        const val POSTAL_ADDRESS_NOTE_KEY = "propertyDetails.peopleResponsible.superiorLandlord.postalAddressNote"
    }
}
