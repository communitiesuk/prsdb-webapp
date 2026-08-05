package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordAddressController.Companion.UPDATE_ADDRESS_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordDateOfBirthController.Companion.UPDATE_DATE_OF_BIRTH_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordEmailController.Companion.UPDATE_EMAIL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordNameController.Companion.UPDATE_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordPhoneNumberController.Companion.UPDATE_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class LandlordViewModel(
    private val landlord: IndividualLandlord,
    private val withChangeLinks: Boolean = true,
    // TODO: PDJB-1492: Delete this (landlord type row will always be visible)
    private val withLandlordTypeRow: Boolean = false,
) {
    private val isEnglandOrWalesResident = landlord.isEnglandOrWalesResident()

    private val changeLinkMessageKey = "forms.links.change"

    val name: String = landlord.name

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
                if (withLandlordTypeRow) {
                    addRow(
                        "landlordDetails.personalDetails.landlordType",
                        "landlordDetails.personalDetails.landlordTypeValue",
                    )
                }
                addRow(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    changeLinkMessageKey,
                    if (!landlord.isVerified) "$UPDATE_NAME_ROUTE/${NameStep.ROUTE_SEGMENT}" else null,
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                    changeLinkMessageKey,
                    if (!landlord.isVerified) "$UPDATE_DATE_OF_BIRTH_ROUTE/${DateOfBirthStep.ROUTE_SEGMENT}" else null,
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
                    "$UPDATE_EMAIL_ROUTE/${EmailStep.ROUTE_SEGMENT}",
                    withActionLink = withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.telephoneNumber",
                    landlord.phoneNumber,
                    changeLinkMessageKey,
                    "$UPDATE_PHONE_NUMBER_ROUTE/${PhoneNumberStep.ROUTE_SEGMENT}",
                    withActionLink = withChangeLinks,
                )
                if (isEnglandOrWalesResident) {
                    addRow(
                        "landlordDetails.personalDetails.contactAddress",
                        landlord.address.singleLineAddress,
                        changeLinkMessageKey,
                        "$UPDATE_ADDRESS_ROUTE/${LookupAddressStep.ROUTE_SEGMENT}",
                        withActionLink = withChangeLinks,
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
