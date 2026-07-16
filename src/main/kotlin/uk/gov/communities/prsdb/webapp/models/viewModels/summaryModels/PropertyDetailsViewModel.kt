package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
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
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.time.format.DateTimeFormatter
import java.util.Locale

class PropertyDetailsViewModel(
    private val propertyOwnership: PropertyOwnership,
    private val withChangeLinks: Boolean = true,
    private val hideNullUprn: Boolean = true,
    private val messageSource: MessageSource,
    private val provideLaterEnabled: Boolean = false,
) {
    val address: String = propertyOwnership.address.singleLineAddress
    val addressParts: List<String> = propertyOwnership.address.toMultiLineAddress().split("\n")

    private val changeLinkMessageKey = "forms.links.change"

    val isOccupied = propertyOwnership.isOccupied

    val isOccupiedKey: String = getIsTenantedKey(isOccupied)

    // An occupied property may still have its tenancy details "skipped" during registration
    // (PDJB-942), in which case the household/tenant/rent/furnishing fields are not populated.
    // The tenancy and rental rows are therefore driven by whether tenancy details were provided
    // rather than by occupancy alone.
    val tenancyInformationProvided = propertyOwnership.currentNumTenants > 0

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
                    propertyOwnership.customPropertyType ?: MessageKeyConverter.convert(propertyOwnership.propertyBuildType),
                )
                addRow(
                    "propertyDetails.propertyRecord.ownershipType",
                    MessageKeyConverter.convert(propertyOwnership.ownershipType),
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
                    propertyOwnership.license?.let {
                        MessageKeyConverter.convert(it.licenseType)
                    } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                    changeLinkMessageKey,
                    getUpdateLicensingBaseRoute(propertyOwnership.id) +
                        "/${LicensingTypeStep.ROUTE_SEGMENT}",
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
                    MessageKeyConverter.convert(isOccupied),
                    changeLinkMessageKey,
                    UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                        "/${OccupiedStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
                if (tenancyInformationProvided) {
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                        propertyOwnership.currentNumHouseholds,
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
                        propertyOwnership.currentNumTenants,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                        propertyOwnership.numBedrooms,
                        changeLinkMessageKey,
                        UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) +
                            "/${BedroomsStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                        MessageKeyConverter.convert(propertyOwnership.rentIncludesBills),
                        changeLinkMessageKey,
                        UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                            "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                        withoutBottomBorder = propertyOwnership.rentIncludesBills,
                        withAriaLabelForAction =
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.changeLinkAriaLabel",
                    )
                    if (propertyOwnership.rentIncludesBills) {
                        addRow(
                            "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                            BillsIncludedHelper.getBillsIncludedForPropertyDetails(propertyOwnership, messageSource),
                        )
                    }
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                        // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                        MessageKeyConverter.convert(propertyOwnership.furnishedStatus!!),
                        changeLinkMessageKey,
                        UpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(propertyOwnership.id) +
                            "/${FurnishedStatusStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                    )
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                        // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                        RentDataHelper.getRentFrequency(propertyOwnership.rentFrequency!!, propertyOwnership.customRentFrequency),
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
                            propertyOwnership.rentAmount!!.toString(),
                            propertyOwnership.rentFrequency!!,
                            messageSource,
                        ),
                    )
                }
            }.toList()

    // ---- New registration-flow layout (behind PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING) ----
    // These properties are only populated when the flag is enabled; the flag-off path continues to use
    // propertyRecord / licensingInformation / tenancyAndRentalInformation above, unchanged.

    val registrationDetails: List<SummaryListRowViewModel> =
        if (provideLaterEnabled) buildRegistrationDetails() else emptyList()

    val propertyDetailsSection: List<SummaryListRowViewModel> =
        if (provideLaterEnabled) buildPropertyDetailsSection() else emptyList()

    val ownershipSection: List<SummaryListRowViewModel> =
        if (provideLaterEnabled) buildOwnershipSection() else emptyList()

    val occupationSection: List<SummaryListRowViewModel> =
        if (provideLaterEnabled) buildOccupationSection() else emptyList()

    // TODO(PDJB-990): licensing "provide later" is inferred here from a null license.
    // Verify this matches the real registration flow once PDJB-990 is implemented.
    val isLicensingProvideLater: Boolean = provideLaterEnabled && propertyOwnership.license == null

    val licensingSection: List<SummaryListRowViewModel> =
        when {
            !provideLaterEnabled -> emptyList()
            !isLicensingProvideLater -> buildLicensingRowsFromLicense()
            withChangeLinks -> listOf(buildLicensingProvideLaterRow())
            else -> emptyList()
        }

    val licensingProvideLaterParagraph: String? =
        if (isLicensingProvideLater && !withChangeLinks) {
            if (isOccupied) {
                getProvideLaterDeadlineText(
                    "propertyDetails.propertyRecord.newLayout.licensing.councilOccupied",
                )
            } else {
                messageSource.getMessageForKey("propertyDetails.propertyRecord.newLayout.licensing.councilNotProvided")
            }
        } else {
            null
        }

    val showTenancySection: Boolean = provideLaterEnabled && (if (withChangeLinks) isOccupied else true)

    val tenancyHeadingKey: String =
        if (!provideLaterEnabled) {
            ""
        } else {
            "propertyDetails.propertyRecord.newLayout.tenancy.heading"
        }

    // TODO(PDJB-942): tenancy "provide later" is inferred here from an occupied property with no households.
    // Verify this matches the real registration flow once PDJB-942 is implemented.
    val isTenancyProvideLater: Boolean =
        provideLaterEnabled && isOccupied && propertyOwnership.currentNumHouseholds == 0

    val tenancySection: List<SummaryListRowViewModel> =
        when {
            !showTenancySection -> emptyList()
            !isOccupied -> emptyList()
            isTenancyProvideLater && withChangeLinks -> listOf(buildTenancyProvideLaterRow())
            isTenancyProvideLater -> emptyList()
            else -> buildTenancyRows()
        }

    val tenancyProvideLaterParagraph: String? =
        when {
            !showTenancySection || withChangeLinks -> null
            !isOccupied -> messageSource.getMessageForKey("propertyDetails.propertyRecord.newLayout.tenancy.councilNotProvided")
            isTenancyProvideLater ->
                getProvideLaterDeadlineText(
                    "propertyDetails.propertyRecord.newLayout.tenancy.councilOccupied",
                )
            else -> null
        }

    private fun buildRegistrationDetails(): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.registrationNumber",
                    RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber),
                )
                addRow(
                    "propertyDetails.propertyRecord.registrationDate",
                    DateTimeHelper.getDateInUK(propertyOwnership.createdDate.toKotlinInstant()),
                )
            }.toList()

    private fun buildPropertyDetailsSection(): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow("propertyDetails.propertyRecord.newLayout.propertyDetails.address", addressParts)
                addRow(
                    "propertyDetails.propertyRecord.localCouncil",
                    propertyOwnership.address.localCouncil
                        ?.name,
                )
                addRow(
                    "propertyDetails.propertyRecord.propertyType",
                    propertyOwnership.customPropertyType ?: MessageKeyConverter.convert(propertyOwnership.propertyBuildType),
                )
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                    propertyOwnership.numBedrooms,
                    changeLinkMessageKey,
                    UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) +
                        "/${BedroomsStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.toList()

    private fun buildOwnershipSection(): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.newLayout.ownership.ownershipType",
                    MessageKeyConverter.convert(propertyOwnership.ownershipType),
                    changeLinkMessageKey,
                    UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnership.id) +
                        "/${OwnershipTypeStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.toList()

    private fun buildOccupationSection(): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.newLayout.occupation.isOccupied",
                    MessageKeyConverter.convert(isOccupied),
                    changeLinkMessageKey,
                    UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                        "/${OccupiedStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.toList()

    private fun buildLicensingRowsFromLicense(): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.licensingInformation.licensingType",
                    propertyOwnership.license?.let {
                        MessageKeyConverter.convert(it.licenseType)
                    } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
                    changeLinkMessageKey,
                    getUpdateLicensingBaseRoute(propertyOwnership.id) +
                        "/${LicensingTypeStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
                if (propertyOwnership.license != null && propertyOwnership.license!!.licenseType != LicensingType.NO_LICENSING) {
                    addRow(
                        "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                        propertyOwnership.license!!.licenseNumber,
                    )
                }
            }.toList()

    private fun buildLicensingProvideLaterRow(): SummaryListRowViewModel =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.newLayout.licensing.rowName",
                    if (isOccupied) {
                        getProvideLaterDeadlineText(
                            "propertyDetails.propertyRecord.newLayout.licensing.provideLaterOccupied",
                        )
                    } else {
                        "propertyDetails.propertyRecord.newLayout.licensing.provideLaterUnoccupied"
                    },
                    changeLinkMessageKey,
                    getUpdateLicensingBaseRoute(propertyOwnership.id) +
                        "/${LicensingTypeStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.single()

    private fun buildTenancyProvideLaterRow(): SummaryListRowViewModel =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.newLayout.tenancy.rowName",
                    getProvideLaterDeadlineText(
                        "propertyDetails.propertyRecord.newLayout.tenancy.provideLaterOccupied",
                    ),
                    changeLinkMessageKey,
                    UpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(propertyOwnership.id) +
                        "/${HouseholdStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.single()

    private fun buildTenancyRows(): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                    propertyOwnership.currentNumHouseholds,
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
                    propertyOwnership.currentNumTenants,
                )
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                    // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                    RentDataHelper.getRentFrequency(propertyOwnership.rentFrequency!!, propertyOwnership.customRentFrequency),
                    changeLinkMessageKey,
                    UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                        "/${RentFrequencyStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                    withAriaLabelForAction =
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.changeLinkAriaLabel",
                )
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                    // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
                    MessageKeyConverter.convert(propertyOwnership.furnishedStatus!!),
                    changeLinkMessageKey,
                    UpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(propertyOwnership.id) +
                        "/${FurnishedStatusStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                    MessageKeyConverter.convert(propertyOwnership.rentIncludesBills),
                    changeLinkMessageKey,
                    UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                        "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                    withoutBottomBorder = propertyOwnership.rentIncludesBills,
                    withAriaLabelForAction =
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.changeLinkAriaLabel",
                )
                if (propertyOwnership.rentIncludesBills) {
                    addRow(
                        "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                        BillsIncludedHelper.getBillsIncludedForPropertyDetails(propertyOwnership, messageSource),
                        changeLinkMessageKey,
                        UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                            "/${BillsIncludedStep.ROUTE_SEGMENT}",
                        withChangeLinks,
                    )
                }
                addRow(
                    "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
                    // TODO PDJB-548 remove not-null assertions !! once occupancy is embedded in PropertyOwnership
                    RentDataHelper.getRentAmount(
                        propertyOwnership.rentAmount!!.toString(),
                        propertyOwnership.rentFrequency!!,
                        messageSource,
                    ),
                    changeLinkMessageKey,
                    UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                        "/${RentAmountStep.ROUTE_SEGMENT}",
                    withChangeLinks,
                )
            }.toList()

    private fun getProvideLaterDeadlineText(deadlineMessageKey: String): String {
        // Matches the compliance tab (ComplianceViewModelFactoryBase): an occupied property in a provide-later
        // state is expected to always have a lastOccupiedDate. TODO(PDJB-548) revisit once occupancy is embedded
        // in PropertyOwnership.
        val deadline =
            propertyOwnership.lastOccupiedDate?.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
                ?: throw IllegalStateException("Cannot get provide-later-with-deadline text without an occupied date")
        return messageSource.getMessageForKey(deadlineMessageKey, arrayOf<Any>(deadline.format(PROVIDE_LATER_DATE_FORMATTER)))
    }

    private fun getIsTenantedKey(isOccupied: Boolean): String =
        when (isOccupied) {
            true -> "propertyDetails.occupationStatus.occupied"
            false -> "propertyDetails.occupationStatus.unoccupied"
        }

    companion object {
        private val PROVIDE_LATER_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
    }
}
