package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.database.entity.Landlord

class PropertyDetailsLandlordViewModel(
    private val landlord: Landlord,
    private val withChangeLinks: Boolean = true,
    private val landlordDetailsUrl: String = "/landlord-details",
) {
    private val isUkResident = landlord.internationalAddress == null

    val landlordsDetails: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    "$UPDATE_ROUTE/name",
                    landlordDetailsUrl,
                )
                // TODO PRSD-747 to pass Id verification status (see Figma for design)
                addRow(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                    "$UPDATE_ROUTE/date-of-birth",
                )
                addRow(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                    "$UPDATE_ROUTE/email",
                )
                addRow(
                    "propertyDetails.landlordDetails.contactNumber",
                    landlord.phoneNumber,
                    "$UPDATE_ROUTE/telephone",
                )
                if (isUkResident) {
                    addRow(
                        "landlordDetails.personalDetails.contactAddress",
                        landlord.address.singleLineAddress,
                        "$UPDATE_ROUTE/address",
                    )
                } else {
                    addRow(
                        "propertyDetails.landlordDetails.addressOutsideEnglandOrWales",
                        landlord.internationalAddress,
                        "$UPDATE_ROUTE/address",
                    )
                    addRow(
                        "propertyDetails.landlordDetails.contactAddressInEnglandOrWales",
                        landlord.address.singleLineAddress,
                        "$UPDATE_ROUTE/contact-address",
                    )
                }
            }.toList()

    private fun MutableList<SummaryListRowViewModel>.addRow(
        key: String,
        value: Any?,
        changeLink: String? = null,
        valueUrl: String? = null,
    ) {
        val changeLinkOrNull = if (withChangeLinks) changeLink else null
        add(SummaryListRowViewModel(key, value, changeLinkOrNull, valueUrl))
    }
}
