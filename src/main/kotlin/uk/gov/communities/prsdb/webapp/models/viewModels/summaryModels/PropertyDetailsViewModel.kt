package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController.Companion.getUpdateLicensingBaseRoute
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.steps.UpdatePropertyDetailsStepId
import uk.gov.communities.prsdb.webapp.helpers.BillsIncludedHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
    private val withChangeLinks: Boolean = true,
    private val hideNullUprn: Boolean = true,
    private val messageSource: MessageSource,
    landlordDetailsUrl: String = LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE,
) {
    val address: String = propertyOwnership.address.singleLineAddress

    private val baseChangeLink = PropertyDetailsController.getUpdatePropertyDetailsPath(propertyOwnership.id)

    private val changeLinkMessageKey = "forms.links.change"

    val isTenantedKey: String = MessageKeyConverter.convert(propertyOwnership.isOccupied)

    val keyDetails: List<SummaryListRowViewModel> =
        listOf(
            SummaryListRowViewModel(
                "propertyDetails.keyDetails.registeredLandlord",
                propertyOwnership.primaryLandlord.name,
                valueUrl = landlordDetailsUrl,
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
                if (propertyOwnership.address.uprn != null) {
                    addRow(
                        "propertyDetails.propertyRecord.uprn",
                        propertyOwnership.address.uprn
                            .toString(),
                    )
                } else if (!hideNullUprn) {
                    addRow("propertyDetails.propertyRecord.uprn", "propertyDetails.propertyRecord.uprn.unavailable")
                }
                addRow(
                    "propertyDetails.propertyRecord.localCouncil",
                    propertyOwnership.address.localCouncil
                        ?.name,
                )
                addRow(
                    "propertyDetails.propertyRecord.propertyType",
                    MessageKeyConverter.convert(propertyOwnership.propertyBuildType),
                )
                addRow(
                    "propertyDetails.propertyRecord.ownershipType",
                    MessageKeyConverter.convert(propertyOwnership.ownershipType),
                    changeLinkMessageKey,
                    UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnership.id) +
                        "/${RegisterPropertyStepId.OwnershipType.urlPathSegment}",
                    withChangeLinks,
                )
            }.toList()

    val licensingInformation: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.licensingInformation.licensingType",
                    propertyOwnership.license?.let {
                        MessageKeyConverter.convert(it.licenseType)
                    } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                    changeLinkMessageKey,
                    getUpdateLicensingBaseRoute(propertyOwnership.id) +
                        "/${UpdatePropertyDetailsStepId.UpdateLicensingType.urlPathSegment}",
                    withChangeLinks,
                )
                if (propertyOwnership.license != null && propertyOwnership.license!!.licenseType != LicensingType.NO_LICENSING) {
                    addRow(
                        "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                        propertyOwnership.license!!.licenseNumber,
                    )
                }
            }.toList()

    val tenancyAndRentalInformation: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied",
                    isTenantedKey,
                    changeLinkMessageKey,
                    UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                        "/${RegisterPropertyStepId.Occupancy.urlPathSegment}",
                    withChangeLinks,
                )
                if (propertyOwnership.isOccupied) {
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds",
                        propertyOwnership.currentNumHouseholds,
                        changeLinkMessageKey,
                        "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateNumberOfHouseholds.urlPathSegment}",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                        propertyOwnership.currentNumTenants,
                        changeLinkMessageKey,
                        "$baseChangeLink/${UpdatePropertyDetailsStepId.UpdateNumberOfPeople.urlPathSegment}",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                        propertyOwnership.numBedrooms,
                        changeLinkMessageKey,
                        // TODO PDJB-105: Add link when update step is created
                        null,
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills",
                        MessageKeyConverter.convert(propertyOwnership.rentIncludesBills),
                        changeLinkMessageKey,
                        // TODO PDJB-105: Add link when update step is created
                        null,
                        withChangeLinks,
                    )
                    if (propertyOwnership.rentIncludesBills) {
                        addRow(
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                            BillsIncludedHelper.getBillsIncludedForPropertyDetails(propertyOwnership, messageSource),
                            changeLinkMessageKey,
                            // TODO PDJB-105: Add link when update step is created
                            null,
                            withChangeLinks,
                        )
                    }
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                        // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                        MessageKeyConverter.convert(propertyOwnership.furnishedStatus!!),
                        changeLinkMessageKey,
                        // TODO PDJB-105: Add link when update step is created
                        null,
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency",
                        // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                        RentDataHelper.getRentFrequency(propertyOwnership.rentFrequency!!, propertyOwnership.customRentFrequency),
                        changeLinkMessageKey,
                        // TODO PDJB-105: Add link when update step is created
                        null,
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
                        // TODO PDJB-548 remove not-null assertions !! once occupancy is embedded in PropertyOwnership
                        RentDataHelper.getRentAmount(
                            propertyOwnership.rentAmount!!.toString(),
                            propertyOwnership.rentFrequency!!,
                            messageSource,
                        ),
                        changeLinkMessageKey,
                        // TODO PDJB-105: Add link when update step is created
                        null,
                        withChangeLinks,
                    )
                }
            }.toList()
}
