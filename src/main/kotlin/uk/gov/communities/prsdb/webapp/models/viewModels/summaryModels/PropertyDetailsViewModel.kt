package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.controllers.UpdateBedroomsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateFurnishedStatusController
import uk.gov.communities.prsdb.webapp.controllers.UpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController.Companion.getUpdateLicensingBaseRoute
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.BillsIncludedHelper
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.helpers.RentDataHelper
import uk.gov.communities.prsdb.webapp.helpers.converters.MessageKeyConverter
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
    private val withChangeLinks: Boolean = true,
    private val hideNullUprn: Boolean = true,
    private val messageSource: MessageSource,
) {
    val address: String = propertyOwnership.propertyDetails.address.singleLineAddress

    private val changeLinkMessageKey = "forms.links.change"

    val isOccupied = propertyOwnership.tenancyDetails.isOccupied

    val isOccupiedKey: String = getIsTenantedKey(isOccupied)

    val propertyRecord: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.registrationDate",
                    DateTimeHelper.getDateInUK(propertyOwnership.createdDate.toKotlinInstant()),
                )
                addRow(
                    "propertyDetails.propertyRecord.registrationNumber",
                    RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.landlordship.registrationNumber),
                )
                addRow("propertyDetails.propertyRecord.address", address)
                if (propertyOwnership.propertyDetails.address.uprn != null) {
                    addRow(
                        "propertyDetails.propertyRecord.uprn",
                        propertyOwnership.propertyDetails.address.uprn
                            .toString(),
                    )
                } else if (!hideNullUprn) {
                    addRow("propertyDetails.propertyRecord.uprn", "propertyDetails.propertyRecord.uprn.unavailable")
                }
                addRow(
                    "propertyDetails.propertyRecord.localCouncil",
                    propertyOwnership.propertyDetails.address.localCouncil
                        ?.name,
                )
                addRow(
                    "propertyDetails.propertyRecord.propertyType",
                    propertyOwnership.propertyDetails.customPropertyType
                        ?: MessageKeyConverter.convert(
                            propertyOwnership.propertyDetails.propertyBuildType,
                        ),
                )
                addRow(
                    "propertyDetails.propertyRecord.ownershipType",
                    MessageKeyConverter.convert(propertyOwnership.landlordship.ownershipType),
                    changeLinkMessageKey,
                    UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnership.id) +
                        "/${OwnershipTypeStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.toList()

    val licensingInformation: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.licensingInformation.licensingType",
                    propertyOwnership.landlordship.license?.let {
                        MessageKeyConverter.convert(it.licenseType)
                    } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                    changeLinkMessageKey,
                    getUpdateLicensingBaseRoute(propertyOwnership.id) +
                        "/${LicensingTypeStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
                if (propertyOwnership.landlordship.license != null &&
                    propertyOwnership.landlordship.license!!.licenseType != LicensingType.NO_LICENSING
                ) {
                    addRow(
                        "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                        propertyOwnership.landlordship.license!!.licenseNumber,
                    )
                }
            }.toList()

    val tenancyAndRentalInformation: List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied",
                    MessageKeyConverter.convert(isOccupied),
                    changeLinkMessageKey,
                    UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                        "/${OccupiedStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
                if (isOccupied) {
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                        propertyOwnership.tenancyDetails.currentNumHouseholds,
                        changeLinkMessageKey,
                        UpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(propertyOwnership.id) +
                            "/${HouseholdStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                        withoutBottomBorder = true,
                        withAriaLabelForAction =
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.changeLinkAriaLabel",
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                        propertyOwnership.tenancyDetails.currentNumTenants,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                        propertyOwnership.propertyDetails.numBedrooms,
                        changeLinkMessageKey,
                        UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) +
                            "/${BedroomsStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                        MessageKeyConverter.convert(propertyOwnership.tenancyDetails.rentIncludesBills),
                        changeLinkMessageKey,
                        UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                            "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                        withoutBottomBorder = propertyOwnership.tenancyDetails.rentIncludesBills,
                        withAriaLabelForAction =
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.changeLinkAriaLabel",
                    )
                    if (propertyOwnership.tenancyDetails.rentIncludesBills) {
                        addRow(
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                            BillsIncludedHelper.getBillsIncludedForPropertyDetails(propertyOwnership, messageSource),
                        )
                    }
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                        // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                        MessageKeyConverter.convert(propertyOwnership.tenancyDetails.furnishedStatus!!),
                        changeLinkMessageKey,
                        UpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(propertyOwnership.id) +
                            "/${FurnishedStatusStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                        // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                        RentDataHelper.getRentFrequency(
                            propertyOwnership.tenancyDetails.rentFrequency!!,
                            propertyOwnership.tenancyDetails.customRentFrequency,
                        ),
                        changeLinkMessageKey,
                        UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                            "/${RentFrequencyStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                        withoutBottomBorder = true,
                        withAriaLabelForAction =
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.changeLinkAriaLabel",
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
                        // TODO PDJB-548 remove not-null assertions !! once occupancy is embedded in PropertyOwnership
                        RentDataHelper.getRentAmount(
                            propertyOwnership.tenancyDetails.rentAmount!!.toString(),
                            propertyOwnership.tenancyDetails.rentFrequency!!,
                            messageSource,
                        ),
                    )
                }
            }.toList()

    private fun getIsTenantedKey(isOccupied: Boolean): String =
        when (isOccupied) {
            true -> "propertyDetails.occupationStatus.occupied"
            false -> "propertyDetails.occupationStatus.unoccupied"
        }
}
