package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
) {
    val address: String = propertyOwnership.property.address.singleLineAddress

    val primaryLandlordName: String = propertyOwnership.primaryLandlord.name
    val landlordDetailsUrl: String = "#"

    val landlordTypeKey: String =
        when (propertyOwnership.landlordType) {
            LandlordType.SOLE -> "propertyDetails.keyDetails.landlordType.sole"
            LandlordType.JOINT -> "propertyDetails.keyDetails.landlordType.joint"
            LandlordType.COMPANY -> "propertyDetails.keyDetails.landlordType.company"
        }

    val isTenantedKey: String = MessageKeyConverter.convert(propertyOwnership.currentNumTenants > 0)

    val propertyRecord: List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.registrationDate",
                DateTimeHelper.getDateInUK(propertyOwnership.createdDate.toKotlinInstant()),
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.registrationNumber",
                RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber),
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.address",
                address,
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.uprn",
                propertyOwnership.property.address.uprn
                    .toString(),
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.localAuthority",
                propertyOwnership.property.address.localAuthority
                    ?.name,
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.propertyType",
                MessageKeyConverter.convert(propertyOwnership.property.propertyBuildType),
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.ownershipType",
                MessageKeyConverter.convert(propertyOwnership.ownershipType),
                "#",
            ),
            getLicensingDetails(propertyOwnership),
        ) +
            getTenancyDetails(propertyOwnership) +
            listOf(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.landlordType",
                    landlordTypeKey,
                    "#",
                ),
            )

    private fun getTenancyDetails(propertyOwnership: PropertyOwnership): List<SummaryListRowViewModel> =
        if (propertyOwnership.currentNumTenants > 0) {
            listOf(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.occupied",
                    isTenantedKey,
                    "#",
                ),
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.numberOfHouseholds",
                    propertyOwnership.currentNumHouseholds,
                    "#",
                ),
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.numberOfPeople",
                    propertyOwnership.currentNumTenants,
                    "#",
                ),
            )
        } else {
            listOf(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.occupied",
                    isTenantedKey,
                    "#",
                ),
            )
        }

    private fun getLicensingDetails(propertyOwnership: PropertyOwnership): SummaryListRowViewModel =
        if (propertyOwnership.license == null) {
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.licensingType",
                MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                "#",
            )
        } else {
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.licensingType",
                listOf(
                    MessageKeyConverter.convert(propertyOwnership.license!!.licenseType),
                    propertyOwnership.license!!.licenseNumber,
                ),
                "#",
            )
        }
}
