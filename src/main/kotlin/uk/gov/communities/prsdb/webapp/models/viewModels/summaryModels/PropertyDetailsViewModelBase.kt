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

// TODO PDJB-939 - this can be combined with PropertyDetailsViewModel once the pdjb939 flag is removed (assuming it is not used elsewhere)
abstract class PropertyDetailsViewModelBase(
    protected val propertyOwnership: PropertyOwnership,
    protected val isLandlordView: Boolean,
    protected val messageSource: MessageSource,
) {
    // Change links are only shown to landlords; the local council view is read-only.
    protected val withChangeLinks: Boolean = isLandlordView

    protected val changeLinkMessageKey = "forms.links.change"

    val address: String = propertyOwnership.address.singleLineAddress
    val addressParts: List<String> = propertyOwnership.address.toMultiLineAddress().split("\n")

    val isOccupied = propertyOwnership.isOccupied

    val isOccupiedKey: String = getIsTenantedKey(isOccupied)

    protected fun registrationNumberRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.registrationNumber",
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber),
            withActionLink = false,
        )

    protected fun registrationDateRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.registrationDate",
            DateTimeHelper.getDateInUK(propertyOwnership.createdDate.toKotlinInstant()),
            withActionLink = false,
        )

    protected fun addressRow(): SummaryListRowViewModel =
        row("propertyDetails.propertyRecord.propertyDetails.address", addressParts, withActionLink = false)

    protected fun localCouncilRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.localCouncil",
            propertyOwnership.address.localCouncil
                ?.name,
            withActionLink = false,
        )

    protected fun propertyTypeRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.propertyType",
            propertyOwnership.customPropertyType ?: MessageKeyConverter.convert(propertyOwnership.propertyBuildType),
            withActionLink = false,
        )

    protected fun ownershipTypeRow(labelKey: String): SummaryListRowViewModel =
        row(
            labelKey,
            MessageKeyConverter.convert(propertyOwnership.ownershipType),
            changeLinkMessageKey,
            UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnership.id) +
                "/${OwnershipTypeStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    protected fun occupiedRow(labelKey: String): SummaryListRowViewModel =
        row(
            labelKey,
            MessageKeyConverter.convert(isOccupied),
            changeLinkMessageKey,
            UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                "/${OccupiedStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    protected fun bedroomsRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
            propertyOwnership.numBedrooms,
            changeLinkMessageKey,
            UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) +
                "/${BedroomsStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    protected fun householdsRow(): SummaryListRowViewModel =
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

    protected fun tenantsRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
            propertyOwnership.currentNumTenants,
            withActionLink = false,
        )

    protected fun rentIncludesBillsRow(): SummaryListRowViewModel =
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

    protected fun billsIncludedRow(includeChangeLink: Boolean = true): SummaryListRowViewModel {
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

    protected fun furnishedStatusRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
            // TODO PDJB-548 remove not-null assertion !! once tenancyDetails is embedded in PropertyOwnership
            MessageKeyConverter.convert(propertyOwnership.furnishedStatus!!),
            changeLinkMessageKey,
            UpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(propertyOwnership.id) +
                "/${FurnishedStatusStep.ROUTE_SEGMENT}",
            withChangeLinks,
        )

    protected fun rentFrequencyRow(withoutBottomBorder: Boolean = false): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
            // TODO PDJB-548 remove not-null assertion !! once tenancyDetails is embedded in PropertyOwnership
            RentDataHelper.getRentFrequency(propertyOwnership.rentFrequency!!, propertyOwnership.customRentFrequency),
            changeLinkMessageKey,
            UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                "/${RentFrequencyStep.ROUTE_SEGMENT}",
            withChangeLinks,
            withoutBottomBorder = withoutBottomBorder,
            withAriaLabelForAction =
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.changeLinkAriaLabel",
        )

    protected fun rentAmountRow(includeChangeLink: Boolean = true): SummaryListRowViewModel {
        // TODO PDJB-548 remove not-null assertions !! once tenancyDetails is embedded in PropertyOwnership
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

    protected fun licensingTypeRow(): SummaryListRowViewModel =
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

    protected fun licensingNumberRow(): SummaryListRowViewModel? =
        if (propertyOwnership.license != null && propertyOwnership.license!!.licenseType != LicensingType.NO_LICENSING) {
            row(
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                propertyOwnership.license!!.licenseNumber,
                withActionLink = false,
            )
        } else {
            null
        }

    protected fun row(
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

    protected fun getProvideLaterDeadlineText(deadlineMessageKey: String): String {
        // Matches the compliance tab (ComplianceViewModelFactoryBase): an occupied property in a provide-later
        // state is expected to always have a lastOccupiedDate.
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
