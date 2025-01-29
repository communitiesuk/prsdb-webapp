package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

class PropertyDetailsLandlordViewModel(
    private val landlord: Landlord,
    private val withChangeLinks: Boolean = true,
) {
    val nameRow: SummaryListRowViewModel = formatNameRow()
    val landlordsDetails: List<SummaryListRowViewModel> = formatLandlordDetails()

    private fun formatNameRow(): SummaryListRowViewModel =
        SummaryListRowViewModel(
            "landlordDetails.personalDetails.name",
            landlord.name,
            toggleChangeLink("$UPDATE_ROUTE/name"),
        )

    private fun formatLandlordDetails(): List<SummaryListRowViewModel> {
        val isUkResident = landlord.internationalAddress == null

        val residencyIndependentPersonalDetails =
            listOf(
                // TODO PRSD-747 to pass Id verification status (see Figma for design)
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                    toggleChangeLink("$UPDATE_ROUTE/date-of-birth"),
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                    toggleChangeLink("$UPDATE_ROUTE/email"),
                ),
                SummaryListRowViewModel(
                    "propertyDetails.landlordDetails.contactNumber",
                    landlord.phoneNumber,
                    toggleChangeLink("$UPDATE_ROUTE/telephone"),
                ),
            )

        val residencyPersonalDetails =
            if (isUkResident) {
                formatUkAddressDetails(landlord)
            } else {
                formatNonUkAddressDetails(landlord)
            }

        return residencyIndependentPersonalDetails + residencyPersonalDetails
    }

    private fun formatNonUkAddressDetails(landlord: Landlord) =
        listOf(
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.nonUkAddress",
                landlord.internationalAddress,
                toggleChangeLink("$UPDATE_ROUTE/address"),
            ),
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.ukAddress",
                landlord.address.singleLineAddress,
                toggleChangeLink("$UPDATE_ROUTE/contact-address"),
            ),
        )

    private fun formatUkAddressDetails(landlord: Landlord) =
        listOf(
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.contactAddress",
                landlord.address.singleLineAddress,
                toggleChangeLink("$UPDATE_ROUTE/address"),
            ),
        )

    private fun toggleChangeLink(link: String?): String? =
        if (withChangeLinks) {
            link
        } else {
            null
        }
}
