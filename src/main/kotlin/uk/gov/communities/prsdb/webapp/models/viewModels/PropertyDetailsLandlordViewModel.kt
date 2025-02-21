package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.extenstions.addRow

class PropertyDetailsLandlordViewModel(
    private val landlord: Landlord,
    private val withChangeLinks: Boolean = true,
    private val landlordDetailsUrl: String = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
) {
    private val isEnglandOrWalesResident = landlord.isEnglandOrWalesResident()

    val landlordsDetails: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    "$UPDATE_ROUTE/name",
                    withChangeLinks,
                    landlordDetailsUrl,
                )
                // TODO PRSD-747 to pass Id verification status (see Figma for design)
                addRow(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                    "$UPDATE_ROUTE/date-of-birth",
                    withChangeLinks,
                )
                addRow(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                    "$UPDATE_ROUTE/email",
                    withChangeLinks,
                )
                addRow(
                    "propertyDetails.landlordDetails.contactNumber",
                    landlord.phoneNumber,
                    "$UPDATE_ROUTE/telephone",
                    withChangeLinks,
                )
                if (isEnglandOrWalesResident) {
                    addRow(
                        "landlordDetails.personalDetails.contactAddress",
                        landlord.address.singleLineAddress,
                        "$UPDATE_ROUTE/address",
                        withChangeLinks,
                    )
                } else {
                    addRow(
                        "propertyDetails.landlordDetails.addressNonEnglandOrWales",
                        landlord.nonEnglandOrWalesAddress,
                        "$UPDATE_ROUTE/address",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.landlordDetails.contactAddressInEnglandOrWales",
                        landlord.address.singleLineAddress,
                        "$UPDATE_ROUTE/contact-address",
                        withChangeLinks,
                    )
                }
            }.toList()
}
