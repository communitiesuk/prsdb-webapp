package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class LandlordViewModel(
    private val landlord: Landlord,
    private val withChangeLinks: Boolean = true,
) {
    private val isEnglandOrWalesResident = landlord.isEnglandOrWalesResident()

    private val changeLinkMessageKey = "forms.links.change"

    val name: String = landlord.name

    // TODO PRSD-746 - add user consent information to this page once it is captured (this will need to be passed into the constructor since this is a view model)
    val consentInformation: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlordDetails.personalDetails.optionalChoices.legalChanges",
                    "TODO PRSD-746",
                    null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.optionalChoices.research",
                    "TODO PRSD-746",
                    null,
                    withActionLink = withChangeLinks,
                )
            }

    val personalDetails: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlordDetails.personalDetails.registrationDate",
                    DateTimeHelper.getDateInUK(landlord.createdDate.toKotlinInstant()),
                    null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.lrn",
                    RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber),
                    null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    changeLinkMessageKey,
                    if (!landlord.isVerified) "$UPDATE_ROUTE/${LandlordDetailsUpdateStepId.UpdateName.urlPathSegment}" else null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                    changeLinkMessageKey,
                    if (!landlord.isVerified) "$UPDATE_ROUTE/${LandlordDetailsUpdateStepId.UpdateDateOfBirth.urlPathSegment}" else null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.oneLoginVerified",
                    MessageKeyConverter.convert(landlord.isVerified),
                    null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                    changeLinkMessageKey,
                    "$UPDATE_ROUTE/${LandlordDetailsUpdateStepId.UpdateEmail.urlPathSegment}",
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.telephoneNumber",
                    landlord.phoneNumber,
                    changeLinkMessageKey,
                    "$UPDATE_ROUTE/${LandlordDetailsUpdateStepId.UpdatePhoneNumber.urlPathSegment}",
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.englandOrWalesResident",
                    isEnglandOrWalesResident,
                    changeLinkMessageKey,
                    // TODO: PRSD-688 "$UPDATE_ROUTE/country-of-residence",
                    null,
                    withActionLink = withChangeLinks,
                )
                if (isEnglandOrWalesResident) {
                    addRow(
                        "landlordDetails.personalDetails.contactAddress",
                        landlord.address.singleLineAddress,
                        changeLinkMessageKey,
                        "$UPDATE_ROUTE/${LandlordDetailsUpdateStepId.LookupEnglandAndWalesAddress.urlPathSegment}",
                        // TODO PRSD-355: Set to withActionLinks
                        withActionLink = false,
                    )
                } else {
                    addRow(
                        "landlordDetails.personalDetails.country",
                        landlord.countryOfResidence,
                        changeLinkMessageKey,
                        // TODO: PRSD-688 "$UPDATE_ROUTE/country-of-residence",
                        null,
                        withActionLink = withChangeLinks,
                    )
                    addRow(
                        "landlordDetails.personalDetails.nonEnglandOrWalesAddress",
                        landlord.nonEnglandOrWalesAddress,
                        changeLinkMessageKey,
                        // TODO: PRSD-688 "$UPDATE_ROUTE/address",
                        null,
                        withActionLink = withChangeLinks,
                    )
                    addRow(
                        "landlordDetails.personalDetails.englandOrWalesAddress",
                        landlord.address.singleLineAddress,
                        changeLinkMessageKey,
                        // TODO: PRSD-688 "$UPDATE_ROUTE/contact-address",
                        null,
                        withActionLink = withChangeLinks,
                    )
                }
            }.toList()
}
