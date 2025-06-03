package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow

class PropertyDetailsLandlordViewModel(
    private val landlord: Landlord,
    landlordDetailsUrl: String = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
) {
    private val isEnglandOrWalesResident = landlord.isEnglandOrWalesResident()

    val landlordsDetails: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    landlordDetailsUrl,
                )
                addRow(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                )
                addRow(
                    "landlordDetails.personalDetails.oneLoginVerified",
                    MessageKeyConverter.convert(landlord.isVerified),
                )
                addRow(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                )
                addRow(
                    "propertyDetails.landlordDetails.contactNumber",
                    landlord.phoneNumber,
                )
                if (isEnglandOrWalesResident) {
                    addRow(
                        "landlordDetails.personalDetails.contactAddress",
                        landlord.address.singleLineAddress,
                    )
                } else {
                    addRow(
                        "propertyDetails.landlordDetails.addressNonEnglandOrWales",
                        landlord.nonEnglandOrWalesAddress,
                    )
                    addRow(
                        "propertyDetails.landlordDetails.contactAddressInEnglandOrWales",
                        landlord.address.singleLineAddress,
                    )
                }
            }.toList()
}
