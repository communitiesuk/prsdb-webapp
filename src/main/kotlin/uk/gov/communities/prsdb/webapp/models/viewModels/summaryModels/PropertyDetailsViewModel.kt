package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
    private val withChangeLinks: Boolean = true,
    private val hideNullUprn: Boolean = true,
    landlordDetailsUrl: String = LandlordDetailsController.LANDLORD_DETAILS_ROUTE,
) {
    val address: String = propertyOwnership.property.address.singleLineAddress

    private val baseChangeLink = PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnership.id)

    val isTenantedKey: String = MessageKeyConverter.convert(propertyOwnership.isOccupied)

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
                    "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateOwnershipType.urlPathSegment}",
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
                    "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment}",
                    // TODO PRSD-1108: Set to withChangeLinks when ticket has been implemented
                    withChangeLinks = false,
                )
                addRow(
                    "propertyDetails.propertyRecord.occupied",
                    isTenantedKey,
                    "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateOccupancy.urlPathSegment}",
                    // TODO PRSD-1109: Set to withChangeLinks when ticket has been implemented
                    withChangeLinks = false,
                )
                if (propertyOwnership.isOccupied) {
                    addRow(
                        "propertyDetails.propertyRecord.numberOfHouseholds",
                        propertyOwnership.currentNumHouseholds,
                        "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment}",
                        // TODO PRSD-1109: Set to withChangeLinks when ticket has been implemented
                        withChangeLinks = false,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.numberOfPeople",
                        propertyOwnership.currentNumTenants,
                        "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment}",
                        // TODO PRSD-1109: Set to withChangeLinks when ticket has been implemented
                        withChangeLinks = false,
                    )
                }
            }.toList()
}
