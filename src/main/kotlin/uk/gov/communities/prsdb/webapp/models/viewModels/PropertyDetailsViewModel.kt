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
    private val withChangeLinks: Boolean = true,
    private val hideNullUprn: Boolean = true,
    private val landlordDetailsUrl: String = "/landlord-details",
) {
    val address: String = propertyOwnership.property.address.singleLineAddress

    val landlordTypeKey: String =
        when (propertyOwnership.landlordType) {
            LandlordType.SOLE -> "propertyDetails.keyDetails.landlordType.sole"
            LandlordType.JOINT -> "propertyDetails.keyDetails.landlordType.joint"
            LandlordType.COMPANY -> "propertyDetails.keyDetails.landlordType.company"
        }

    val isTenantedKey: String = MessageKeyConverter.convert(propertyOwnership.currentNumTenants > 0)

    val keyDetails: List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "propertyDetails.keyDetails.landlordType",
                landlordTypeKey,
                null,
            ),
            SummaryListRowViewModel(
                "propertyDetails.keyDetails.registeredLandlord",
                propertyOwnership.primaryLandlord.name,
                landlordDetailsUrl,
            ),
            SummaryListRowViewModel(
                "propertyDetails.keyDetails.isTenanted",
                isTenantedKey,
                null,
            ),
        )

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
        ) +
            getAddressAndUprn(propertyOwnership, hideNullUprn) +
            listOf(
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
                    // TODO PRSD-689: Add update link
                    addChangeLink("#"),
                ),
                getLicensingDetails(propertyOwnership),
            ) +
            getTenancyDetails(propertyOwnership) +
            listOf(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.landlordType",
                    landlordTypeKey,
                    // TODO PRSD-xxx Add update link
                    addChangeLink("#"),
                ),
            )

    private fun getTenancyDetails(propertyOwnership: PropertyOwnership): List<SummaryListRowViewModel> =
        if (propertyOwnership.currentNumTenants > 0) {
            listOf(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.occupied",
                    isTenantedKey,
                    // TODO PRSD-799: Add update link
                    addChangeLink("#"),
                ),
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.numberOfHouseholds",
                    propertyOwnership.currentNumHouseholds,
                    // TODO PRSD-800: Add update link
                    addChangeLink("#"),
                ),
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.numberOfPeople",
                    propertyOwnership.currentNumTenants,
                    // TODO PRSD-801: Add update link
                    addChangeLink("#"),
                ),
            )
        } else {
            listOf(
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.occupied",
                    isTenantedKey,
                    // TODO PRSD-799: Add update link
                    addChangeLink("#"),
                ),
            )
        }

    private fun getLicensingDetails(propertyOwnership: PropertyOwnership): SummaryListRowViewModel {
        val license = propertyOwnership.license
        return if (license == null || license.licenseType == LicensingType.NO_LICENSING) {
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.licensingType",
                MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                // TODO PRSD-798: Add update link
                addChangeLink("#"),
            )
        } else {
            SummaryListRowViewModel(
                "propertyDetails.propertyRecord.licensingType",
                listOf(
                    MessageKeyConverter.convert(license.licenseType),
                    license.licenseNumber,
                ),
                // TODO PRSD-798: Add update link
                addChangeLink("#"),
            )
        }
    }

    private fun getAddressAndUprn(
        propertyOwnership: PropertyOwnership,
        hideNullUprn: Boolean,
    ): List<SummaryListRowViewModel> {
        if (propertyOwnership.property.address.uprn != null) {
            return listOf(
                getAddress(),
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.uprn",
                    propertyOwnership.property.address.uprn
                        .toString(),
                    null,
                ),
            )
        } else if (hideNullUprn) {
            return listOf(getAddress())
        } else {
            return listOf(
                getAddress(),
                SummaryListRowViewModel(
                    "propertyDetails.propertyRecord.uprn",
                    "propertyDetails.propertyRecord.uprn.unavailable",
                    null,
                ),
            )
        }
    }

    private fun getAddress() =
        SummaryListRowViewModel(
            "propertyDetails.propertyRecord.address",
            address,
            null,
        )

    private fun addChangeLink(link: String?): String? =
        if (withChangeLinks) {
            link
        } else {
            null
        }
}
