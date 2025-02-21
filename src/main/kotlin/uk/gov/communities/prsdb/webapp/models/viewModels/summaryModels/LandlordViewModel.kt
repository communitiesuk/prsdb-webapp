package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
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
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    toggleChangeLink("$UPDATE_ROUTE/name"),
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                    // TODO: PRSD-792 toggleChangeLink("$UPDATE_ROUTE/date-of-birth"),
                    null,
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.oneLoginVerified",
                    MessageKeyConverter.convert(landlord.isVerified),
                    null,
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                    toggleChangeLink("$UPDATE_ROUTE/email"),
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.telephoneNumber",
                    landlord.phoneNumber,
                    // TODO: PRSD-797 toggleChangeLink("$UPDATE_ROUTE/telephone"),
                    null,
                ),
                SummaryListRowViewModel(
                    "landlordDetails.personalDetails.englandOrWalesResident",
                    isEnglandOrWalesResident,
                    // TODO: PRSD-688 toggleChangeLink("$UPDATE_ROUTE/country-of-residence"),
                    null,
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
                // TODO: PRSD-688 toggleChangeLink("$UPDATE_ROUTE/country-of-residence"),
                null,
            ),
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.nonEnglandOrWalesAddress",
                landlord.nonEnglandOrWalesAddress,
                // TODO: PRSD-688 toggleChangeLink("$UPDATE_ROUTE/address"),
                null,
            ),
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.englandOrWalesAddress",
                landlord.address.singleLineAddress,
                // TODO: PRSD-688 toggleChangeLink("$UPDATE_ROUTE/contact-address"),
                null,
            ),
        )

    private fun formatEnglandOrWalesResidentAddressDetails(landlord: Landlord) =
        listOf(
            SummaryListRowViewModel(
                "landlordDetails.personalDetails.contactAddress",
                landlord.address.singleLineAddress,
                // TODO: PRSD-796 toggleChangeLink("$UPDATE_ROUTE/address"),
                null,
            ),
        )

    private fun toggleChangeLink(link: String?): String? =
        if (withChangeLinks) {
            link
        } else {
            null
        }
}
