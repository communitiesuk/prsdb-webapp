package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
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
    private val isLandlordView: Boolean = true,
    private val messageSource: MessageSource,
    private val featureFlagManager: FeatureFlagManager,
) {
    val provideLaterEnabled: Boolean =
        featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)

    // Change links are only shown to landlords; the local council view is read-only.
    private val withChangeLinks: Boolean = isLandlordView

    private val changeLinkMessageKey = "forms.links.change"

    val address: String = propertyOwnership.address.singleLineAddress
    val addressParts: List<String> = propertyOwnership.address.toMultiLineAddress().split("\n")

    val isOccupied = propertyOwnership.isOccupied

    // An occupied property may still have its tenancy details "skipped" during registration (PDJB-942),
    // in which case the household/tenant/rent/furnishing fields are not populated. The beforePdjb939 tenancy rows
    // are driven by whether tenancy details were provided rather than by occupancy alone.
    private val tenancyInformationProvided = propertyOwnership.currentNumTenants > 0

    val isOccupiedKey: String = getIsTenantedKey(isOccupied)

    val isLicensingProvideLater: Boolean = propertyOwnership.licenseProvideLater == true

    val isTenancyProvideLater: Boolean = isOccupied && propertyOwnership.tenancyProvideLater == true

    val showTenancySection: Boolean = !isLandlordView || isOccupied

    val tenancyHeadingKey: String = "propertyDetails.propertyRecord.tenancy.heading"

    // ---- Base (post-PDJB-939 registration-flow) layout: primary output, rendered when the flag is enabled ----

    val registrationDetails: List<SummaryListRowViewModel> by lazy {
        listOf(registrationNumberRow(), registrationDateRow())
    }

    val propertyDetailsSection: List<SummaryListRowViewModel> by lazy {
        listOf(addressRow(), localCouncilRow(), propertyTypeRow(), bedroomsRow())
    }

    val ownershipSection: List<SummaryListRowViewModel> by lazy {
        listOf(ownershipTypeRow("propertyDetails.propertyRecord.ownership.ownershipType"))
    }

    val occupationSection: List<SummaryListRowViewModel> by lazy {
        listOf(occupiedRow("propertyDetails.propertyRecord.occupation.isOccupied"))
    }

    val licensingSection: List<SummaryListRowViewModel> by lazy {
        when {
            !isLicensingProvideLater -> listOfNotNull(licensingTypeRow(), licensingNumberRow())
            isLandlordView -> listOf(licensingProvideLaterRow())
            else -> emptyList()
        }
    }

    val licensingProvideLaterParagraph: String? by lazy {
        if (isLicensingProvideLater && !isLandlordView) {
            if (isOccupied) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.councilOccupied")
            } else {
                messageSource.getMessageForKey("propertyDetails.propertyRecord.licensing.councilNotProvided")
            }
        } else {
            null
        }
    }

    val tenancySection: List<SummaryListRowViewModel> by lazy {
        when {
            !showTenancySection -> emptyList()
            !isOccupied -> emptyList()
            isTenancyProvideLater && isLandlordView -> listOf(tenancyProvideLaterRow())
            isTenancyProvideLater -> emptyList()
            else ->
                buildList {
                    add(householdsRow())
                    add(tenantsRow())
                    add(rentFrequencyRow(withoutBottomBorder = false))
                    add(furnishedStatusRow())
                    add(rentIncludesBillsRow())
                    if (propertyOwnership.rentIncludesBills) add(billsIncludedRow(includeChangeLink = true))
                    add(rentAmountRow(includeChangeLink = true))
                }
        }
    }

    val tenancyProvideLaterParagraph: String? by lazy {
        when {
            !showTenancySection || isLandlordView -> null
            !isOccupied -> messageSource.getMessageForKey("propertyDetails.propertyRecord.tenancy.councilNotProvided")
            isTenancyProvideLater ->
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.tenancy.councilOccupied")
            else -> null
        }
    }

    // ---- beforePdjb939 (flag-off) layout: rendered only when the flag is disabled. ----
    // TODO(PDJB-939): delete everything named beforePdjb939* and its message keys when the flag is permanently on.

    val beforePdjb939PropertyRecord: List<SummaryListRowViewModel> by lazy {
        listOfNotNull(
            registrationDateRow(),
            registrationNumberRow(),
            beforePdjb939AddressRow(),
            uprnRow(),
            localCouncilRow(),
            propertyTypeRow(),
            ownershipTypeRow("propertyDetails.propertyRecord.beforePdjb939.ownershipType"),
        )
    }

    val beforePdjb939LicensingInformation: List<SummaryListRowViewModel> by lazy {
        listOfNotNull(licensingTypeRow(), licensingNumberRow())
    }

    val beforePdjb939TenancyAndRentalInformation: List<SummaryListRowViewModel> by lazy {
        buildList {
            add(occupiedRow("propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied"))
            if (tenancyInformationProvided) {
                add(householdsRow())
                add(tenantsRow())
                add(bedroomsRow())
                add(rentIncludesBillsRow())
                if (propertyOwnership.rentIncludesBills) add(billsIncludedRow(includeChangeLink = false))
                add(furnishedStatusRow())
                add(rentFrequencyRow(withoutBottomBorder = true))
                add(rentAmountRow(includeChangeLink = false))
            }
        }
    }

    // ---- Atomic row builders shared by both layouts ----

    private fun registrationNumberRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.registrationNumber",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber),
            withActionLink = false,
        )

    private fun registrationDateRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.registrationDate",
            DateTimeHelper.getDateInUK(propertyOwnership.createdDate.toKotlinInstant()),
            withActionLink = false,
        )

    private fun beforePdjb939AddressRow(): SummaryListRowViewModel =
        row("propertyDetails.propertyRecord.beforePdjb939.address", address, withActionLink = false)

    private fun addressRow(): SummaryListRowViewModel =
        row("propertyDetails.propertyRecord.propertyDetails.address", addressParts, withActionLink = false)

    private fun uprnRow(): SummaryListRowViewModel? =
        when {
            propertyOwnership.address.uprn != null ->
                row(
                    "propertyDetails.propertyRecord.beforePdjb939.uprn",
                    propertyOwnership.address.uprn
                        .toString(),
                    withActionLink = false,
                )
            !isLandlordView ->
                row(
                    "propertyDetails.propertyRecord.beforePdjb939.uprn",
                    "propertyDetails.propertyRecord.beforePdjb939.uprn.unavailable",
                    withActionLink = false,
                )
            else -> null
        }

    private fun localCouncilRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.localCouncil",
            propertyOwnership.address.localCouncil
                ?.name,
            withActionLink = false,
        )

    private fun propertyTypeRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.propertyType",
            propertyOwnership.customPropertyType ?: MessageKeyConverter.convert(propertyOwnership.propertyBuildType),
            withActionLink = false,
        )

    private fun ownershipTypeRow(labelKey: String): SummaryListRowViewModel =
        row(
            labelKey,
            MessageKeyConverter.convert(propertyOwnership.ownershipType),
            changeLinkMessageKey,
            UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnership.id) +
                "/${OwnershipTypeStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun occupiedRow(labelKey: String): SummaryListRowViewModel =
        row(
            labelKey,
            MessageKeyConverter.convert(isOccupied),
            changeLinkMessageKey,
            UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                "/${OccupiedStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun bedroomsRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
            propertyOwnership.numBedrooms,
            changeLinkMessageKey,
            UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) +
                "/${BedroomsStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun householdsRow(): SummaryListRowViewModel =
        row(
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

    private fun tenantsRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
            propertyOwnership.currentNumTenants,
            withActionLink = false,
        )

    private fun rentIncludesBillsRow(): SummaryListRowViewModel =
        row(
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

    private fun billsIncludedRow(includeChangeLink: Boolean): SummaryListRowViewModel {
        val value = BillsIncludedHelper.getBillsIncludedForPropertyDetails(propertyOwnership, messageSource)
        return if (includeChangeLink) {
            row(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                value,
                changeLinkMessageKey,
                UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                    "/${BillsIncludedStep.ROUTE_SEGMENT}",
                withChangeLinks,
            )
        } else {
            row("propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded", value, withActionLink = false)
        }
    }

    private fun furnishedStatusRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
            // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
            MessageKeyConverter.convert(propertyOwnership.furnishedStatus!!),
            changeLinkMessageKey,
            UpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(propertyOwnership.id) +
                "/${FurnishedStatusStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun rentFrequencyRow(withoutBottomBorder: Boolean): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
            // TODO PDJB-548 remove not-null assertion !! once occupancy is embedded in PropertyOwnership
            RentDataHelper.getRentFrequency(propertyOwnership.rentFrequency!!, propertyOwnership.customRentFrequency),
            changeLinkMessageKey,
            UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                "/${RentFrequencyStep.ROUTE_SEGMENT}",
            withChangeLinks,
            withoutBottomBorder = withoutBottomBorder,
            withAriaLabelForAction =
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.changeLinkAriaLabel",
        )

    private fun rentAmountRow(includeChangeLink: Boolean): SummaryListRowViewModel {
        // TODO PDJB-548 remove not-null assertions !! once occupancy is embedded in PropertyOwnership
        val value =
            RentDataHelper.getRentAmount(
                propertyOwnership.rentAmount!!.toString(),
                propertyOwnership.rentFrequency!!,
                messageSource,
            )
        return if (includeChangeLink) {
            row(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
                value,
                changeLinkMessageKey,
                UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                    "/${RentAmountStep.ROUTE_SEGMENT}",
                withChangeLinks,
            )
        } else {
            row("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount", value, withActionLink = false)
        }
    }

    private fun licensingTypeRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.licensingInformation.licensingType",
            propertyOwnership.license?.let {
                MessageKeyConverter.convert(it.licenseType)
            } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
            changeLinkMessageKey,
            getUpdateLicensingBaseRoute(propertyOwnership.id) +
                "/${LicensingTypeStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun licensingNumberRow(): SummaryListRowViewModel? =
        if (propertyOwnership.license != null && propertyOwnership.license!!.licenseType != LicensingType.NO_LICENSING) {
            row(
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                propertyOwnership.license!!.licenseNumber,
                withActionLink = false,
            )
        } else {
            null
        }

    private fun licensingProvideLaterRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.licensing.rowName",
            if (isOccupied) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.provideLaterOccupied")
            } else {
                "propertyDetails.propertyRecord.licensing.provideLaterUnoccupied"
            },
            changeLinkMessageKey,
            getUpdateLicensingBaseRoute(propertyOwnership.id) +
                "/${LicensingTypeStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun tenancyProvideLaterRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancy.rowName",
            getProvideLaterDeadlineText("propertyDetails.propertyRecord.tenancy.provideLaterOccupied"),
            changeLinkMessageKey,
            UpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(propertyOwnership.id) +
                "/${HouseholdStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    private fun row(
        key: String,
        value: Any?,
        actionText: String? = null,
        actionLink: String? = null,
        withActionLink: Boolean = true,
        withoutBottomBorder: Boolean = false,
        withAriaLabelForAction: String? = null,
    ): SummaryListRowViewModel =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                addRow(
                    key = key,
                    value = value,
                    actionText = actionText,
                    actionLink = actionLink,
                    withActionLink = withActionLink,
                    withoutBottomBorder = withoutBottomBorder,
                    withAriaLabelForAction = withAriaLabelForAction,
                )
            }.single()

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
