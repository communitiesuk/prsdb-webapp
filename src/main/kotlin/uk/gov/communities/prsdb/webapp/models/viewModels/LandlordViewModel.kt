package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class LandlordViewModel(
    private val landlord: Landlord,
    private val withChangeLinks: Boolean = true,
) {
    val name: String = landlord.name

    val consentInformation: List<SummaryListRowViewModel>
        get() {
            // TODO PRSD-746 - add user consent information to this page once it is captured (this will need to be passed into the constructor since this is a view model)
            return listOf(
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.optionalChoices.legalChanges",
                    "TODO PRSD-746",
                    toggleChangeLink(null),
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.optionalChoices.research",
                    "TODO PRSD-746",
                    toggleChangeLink(null),
                ),
            )
        }

    val personalDetails: List<SummaryListRowViewModel> = formatPersonalDetails()

    private fun formatPersonalDetails(): List<SummaryListRowViewModel> {
        val isUkResident = landlord.internationalAddress == null

        val residencyIndependentPersonalDetails =
            listOf(
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.registrationDate",
                    DateTimeHelper.getDateInUK(landlord.createdDate.toKotlinInstant()),
                    null,
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.lrn",
                    RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                    null,
                ),
                // TODO PRSD-747 to pass Id verification status to model with verified label and message key
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    toggleChangeLink("$UPDATE_ROUTE/name"),
                ),
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
                    "landlordDetails.personalDetails.telephoneNumber",
                    landlord.phoneNumber,
                    toggleChangeLink("$UPDATE_ROUTE/telephone"),
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.ukResident",
                    isUkResident,
                    toggleChangeLink("$UPDATE_ROUTE/country-of-residence"),
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
            // TODO: PRSD-742 to add country as a separate field
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.country",
                "TODO: PRSD-742",
                toggleChangeLink(null),
            ),
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
