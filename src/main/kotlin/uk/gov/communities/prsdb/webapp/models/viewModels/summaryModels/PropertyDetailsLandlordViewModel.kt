package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extenstions.addRowWithoutChangeLink

class PropertyDetailsLandlordViewModel(
    private val landlord: Landlord,
    private val landlordDetailsUrl: String = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
) {
    private val isEnglandOrWalesResident = landlord.isEnglandOrWalesResident()

    val landlordsDetails: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRowWithoutChangeLink(
                    "landlordDetails.personalDetails.name",
                    landlord.name,
                    landlordDetailsUrl,
                )
                addRowWithoutChangeLink(
                    "landlordDetails.personalDetails.dateOfBirth",
                    landlord.dateOfBirth,
                )
                addRowWithoutChangeLink(
                    "landlordDetails.personalDetails.oneLoginVerified",
                    MessageKeyConverter.convert(landlord.isVerified),
                )
                addRowWithoutChangeLink(
                    "landlordDetails.personalDetails.emailAddress",
                    landlord.email,
                )
                addRowWithoutChangeLink(
                    "propertyDetails.landlordDetails.contactNumber",
                    landlord.phoneNumber,
                )
                if (isEnglandOrWalesResident) {
                    addRowWithoutChangeLink(
                        "landlordDetails.personalDetails.contactAddress",
                        landlord.address.singleLineAddress,
                    )
                } else {
                    addRowWithoutChangeLink(
                        "propertyDetails.landlordDetails.addressNonEnglandOrWales",
                        landlord.nonEnglandOrWalesAddress,
                    )
                    addRowWithoutChangeLink(
                        "propertyDetails.landlordDetails.contactAddressInEnglandOrWales",
                        landlord.address.singleLineAddress,
                    )
                }
            }.toList()
}
