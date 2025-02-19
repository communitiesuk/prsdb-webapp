package uk.gov.communities.prsdb.webapp.models.viewModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extenstions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
    private val withChangeLinks: Boolean = true,
    private val hideNullUprn: Boolean = true,
    private val landlordDetailsUrl: String = "/landlord-details",
) {
    val address: String = propertyOwnership.property.address.singleLineAddress

    val isTenantedKey: String = MessageKeyConverter.convert(propertyOwnership.currentNumTenants > 0)

    val keyDetails: List<SummaryListRowViewModel> =
        listOf(
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
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.registrationDate",
                    DateTimeHelper.getDateInUK(propertyOwnership.createdDate.toKotlinInstant()),
                )
                addRow(
                    "propertyDetails.propertyRecord.registrationNumber",
                    RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber),
                )
                addRow("propertyDetails.propertyRecord.address", address)
                if (propertyOwnership.property.address.uprn != null) {
                    addRow(
                        "propertyDetails.propertyRecord.uprn",
                        propertyOwnership.property.address.uprn
                            .toString(),
                    )
                } else if (!hideNullUprn) {
                    addRow("propertyDetails.propertyRecord.uprn", "propertyDetails.propertyRecord.uprn.unavailable")
                }
                addRow(
                    "propertyDetails.propertyRecord.localAuthority",
                    propertyOwnership.property.address.localAuthority
                        ?.name,
                )
                addRow(
                    "propertyDetails.propertyRecord.propertyType",
                    MessageKeyConverter.convert(propertyOwnership.property.propertyBuildType),
                )
                addRow(
                    "propertyDetails.propertyRecord.ownershipType",
                    MessageKeyConverter.convert(propertyOwnership.ownershipType),
                    // TODO PRSD-689: Add update link
                    "#",
                    withChangeLinks,
                )
                addRow(
                    "propertyDetails.propertyRecord.licensingType",
                    propertyOwnership.license?.let {
                        if (it.licenseType == LicensingType.NO_LICENSING) {
                            MessageKeyConverter.convert(LicensingType.NO_LICENSING)
                        } else {
                            listOf(MessageKeyConverter.convert(it.licenseType), it.licenseNumber)
                        }
                    } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                    // TODO PRSD-798: Add update link
                    "#",
                    withChangeLinks,
                )
                // TODO PRSD-799: Add update link
                addRow("propertyDetails.propertyRecord.occupied", isTenantedKey, "#", withChangeLinks)
                if (propertyOwnership.currentNumTenants > 0) {
                    // TODO PRSD-800: Add update link
                    addRow(
                        "propertyDetails.propertyRecord.numberOfHouseholds",
                        propertyOwnership.currentNumHouseholds,
                        "#",
                        withChangeLinks,
                    )
                    // TODO PRSD-801: Add update link
                    addRow("propertyDetails.propertyRecord.numberOfPeople", propertyOwnership.currentNumTenants, "#", withChangeLinks)
                }
            }.toList()
}
