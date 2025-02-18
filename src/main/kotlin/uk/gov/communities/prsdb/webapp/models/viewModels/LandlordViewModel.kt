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
        val isEnglandOrWalesResident = landlord.isEnglandOrWalesResident()

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
                // TODO PRSD-747 to pass Id verification status (see Figma for design)
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
                    isEnglandOrWalesResident,
                    toggleChangeLink("$UPDATE_ROUTE/country-of-residence"),
                ),
            )

        val residencyPersonalDetails =
            if (isEnglandOrWalesResident) {
                formatEnglandOrWalesResidentAddressDetails(landlord)
            } else {
                formatNonEnglandOrWalesResidentAddressDetails(landlord)
            }

        return residencyIndependentPersonalDetails + residencyPersonalDetails
    }

    private fun formatNonEnglandOrWalesResidentAddressDetails(landlord: Landlord) =
        listOf(
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.country",
                landlord.countryOfResidence,
                toggleChangeLink("$UPDATE_ROUTE/country-of-residence"),
            ),
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.nonUkAddress",
                landlord.nonEnglandOrWalesAddress,
                toggleChangeLink("$UPDATE_ROUTE/address"),
            ),
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.ukAddress",
                landlord.address.singleLineAddress,
                toggleChangeLink("$UPDATE_ROUTE/contact-address"),
            ),
        )

    private fun formatEnglandOrWalesResidentAddressDetails(landlord: Landlord) =
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
