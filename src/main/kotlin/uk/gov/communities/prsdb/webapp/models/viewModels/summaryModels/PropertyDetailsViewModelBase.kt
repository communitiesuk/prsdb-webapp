package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import kotlinx.datetime.toKotlinInstant
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyDetailsViewType
import uk.gov.communities.prsdb.webapp.controllers.UpdateBedroomsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateFurnishedStatusController
import uk.gov.communities.prsdb.webapp.controllers.UpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController.Companion.getUpdateLicensingBaseRoute
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipTypeController
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateTenancyDetailsController
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
    protected val viewType: PropertyDetailsViewType,
    protected val messageSource: MessageSource,
) {
    protected val changeLinkMessageKey = "forms.links.change"

    val address: String = propertyOwnership.address.singleLineAddress
    val addressParts: List<String> = propertyOwnership.address.toMultiLineAddress().split("\n")

    val isOccupied = propertyOwnership.isOccupied

    val isOccupiedKey: String = getIsTenantedKey(isOccupied)

    private val registrationDate = propertyOwnership.registrationDate

    protected val hasBeenOccupiedSinceRegistration: Boolean = propertyOwnership.hasBeenOccupiedSinceRegistration

    val isLicensingProvideLater: Boolean = propertyOwnership.licenseProvideLater == true

    val isTenancyProvideLater: Boolean = propertyOwnership.tenancyProvideLater == true

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
        rowWithViewTypeSpecificChangeLink(
            labelKey,
            MessageKeyConverter.convert(propertyOwnership.ownershipType),
            UpdateOwnershipTypeController.getUpdateOwnershipTypeRoute(propertyOwnership.id) +
                "/${OwnershipTypeStep.ROUTE_SEGMENT}",
        )

    protected fun occupiedRow(
        labelKey: String,
        occupied: Boolean = isOccupied,
    ): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            labelKey,
            MessageKeyConverter.convert(occupied),
            UpdateOccupancyController.getUpdateOccupancyRoute(propertyOwnership.id) +
                "/${OccupiedStep.ROUTE_SEGMENT}",
        )

    protected fun bedroomsRow(): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
            propertyOwnership.numBedrooms
                ?: "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms.notAdded",
            UpdateBedroomsController.getUpdateBedroomsRoute(propertyOwnership.id) +
                "/${BedroomsStep.ROUTE_SEGMENT}",
        )

    protected fun householdsRow(): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
            propertyOwnership.currentNumHouseholds,
            UpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(propertyOwnership.id) +
                "/${HouseholdStep.ROUTE_SEGMENT}",
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
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
            MessageKeyConverter.convert(propertyOwnership.rentIncludesBills),
            UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
            withoutBottomBorder = propertyOwnership.rentIncludesBills,
            withAriaLabelForAction =
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.changeLinkAriaLabel",
        )

    protected fun billsIncludedRow(includeChangeLink: Boolean = true): SummaryListRowViewModel {
        val value = BillsIncludedHelper.getBillsIncludedForPropertyDetails(propertyOwnership, messageSource)
        return if (includeChangeLink) {
            rowWithViewTypeSpecificChangeLink(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                value,
                UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(propertyOwnership.id) +
                    "/${BillsIncludedStep.ROUTE_SEGMENT}",
            )
        } else {
            row("propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded", value, withActionLink = false)
        }
    }

    protected fun furnishedStatusRow(): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
            // TODO PDJB-548 remove not-null assertion !! once tenancyDetails is embedded in PropertyOwnership
            MessageKeyConverter.convert(propertyOwnership.furnishedStatus!!),
            UpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(propertyOwnership.id) +
                "/${FurnishedStatusStep.ROUTE_SEGMENT}",
        )

    protected fun rentFrequencyRow(withoutBottomBorder: Boolean = false): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
            // TODO PDJB-548 remove not-null assertion !! once tenancyDetails is embedded in PropertyOwnership
            RentDataHelper.getRentFrequency(propertyOwnership.rentFrequency!!, propertyOwnership.customRentFrequency),
            UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                "/${RentFrequencyStep.ROUTE_SEGMENT}",
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
            rowWithViewTypeSpecificChangeLink(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
                value,
                UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(propertyOwnership.id) +
                    "/${RentAmountStep.ROUTE_SEGMENT}",
            )
        } else {
            row("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount", value, withActionLink = false)
        }
    }

    protected fun licensingTypeRow(): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.licensingInformation.licensingType",
            propertyOwnership.license?.let {
                MessageKeyConverter.convert(it.licenseType)
            } ?: MessageKeyConverter.convert(LicensingType.NO_LICENSING),
            getUpdateLicensingBaseRoute(propertyOwnership.id) +
                "/${LicensingTypeStep.ROUTE_SEGMENT}",
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

    protected fun licensingProvideLaterRow(): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.licensing.rowName",
            if (hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.provideLaterWithDeadline")
            } else {
                "propertyDetails.propertyRecord.licensing.provideLaterNoDeadline"
            },
            getUpdateLicensingBaseRoute(propertyOwnership.id) +
                "/${LicensingTypeStep.ROUTE_SEGMENT}",
        )

    protected fun tenancyProvideLaterRow(): SummaryListRowViewModel =
        rowWithViewTypeSpecificChangeLink(
            "propertyDetails.propertyRecord.tenancy.rowName",
            if (hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.tenancy.provideLaterWithDeadline")
            } else {
                "propertyDetails.propertyRecord.tenancy.provideLaterNoDeadline"
            },
            UpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(propertyOwnership.id) +
                "/${HouseholdStep.ROUTE_SEGMENT}",
        )

    // The local council view hides provide-later rows and shows an explanatory paragraph instead; all other
    // views show a provide-later row (with a change link only where rowWithViewTypeSpecificChangeLink supplies a route for the view type).
    protected fun buildLicensingSection(): List<SummaryListRowViewModel> =
        when {
            !isLicensingProvideLater -> listOfNotNull(licensingTypeRow(), licensingNumberRow())
            viewType == PropertyDetailsViewType.LOCAL_COUNCIL -> emptyList()
            else -> listOf(licensingProvideLaterRow())
        }

    protected fun buildTenancySection(): List<SummaryListRowViewModel> =
        when {
            !isOccupied -> emptyList()
            isTenancyProvideLater && viewType == PropertyDetailsViewType.LOCAL_COUNCIL -> emptyList()
            isTenancyProvideLater -> listOf(tenancyProvideLaterRow())
            else ->
                buildList {
                    add(householdsRow())
                    add(tenantsRow())
                    add(rentIncludesBillsRow())
                    if (propertyOwnership.rentIncludesBills) add(billsIncludedRow(includeChangeLink = false))
                    add(furnishedStatusRow())
                    add(rentFrequencyRow(withoutBottomBorder = true))
                    add(rentAmountRow(includeChangeLink = false))
                }
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

    // Builds a row whose change link is shown only where the current view type has a route to link to:
    //  - Landlord: always linked, using the landlord update route.
    //  - Local council: never linked - the council view is read-only.
    //  - Letting agent: linked only once the relevant update journey supplies a letting-agent route via
    //    lettingAgentActionLink; until then the row renders without a link.
    // This lets the letting-agent update journeys be built in parallel (PDJB-1571, PDJB-1572, PDJB-1573,
    // PDJB-1574, PDJB-1575, PDJB-1576): each ticket wires up lettingAgentActionLink for its own row(s)
    // independently, without turning on (or pointing at the wrong route for) any of the others.
    protected fun rowWithViewTypeSpecificChangeLink(
        key: String,
        value: Any?,
        landlordActionLink: String,
        lettingAgentActionLink: String? = null,
        withoutBottomBorder: Boolean = false,
        withAriaLabelForAction: String? = null,
    ): SummaryListRowViewModel {
        val (actionLink, withActionLink) =
            when (viewType) {
                PropertyDetailsViewType.LANDLORD -> landlordActionLink to true
                PropertyDetailsViewType.LOCAL_COUNCIL -> null to false
                PropertyDetailsViewType.LETTING_AGENT -> lettingAgentActionLink to (lettingAgentActionLink != null)
            }
        return row(
            key = key,
            value = value,
            actionText = changeLinkMessageKey,
            actionLink = actionLink,
            withActionLink = withActionLink,
            withoutBottomBorder = withoutBottomBorder,
            withAriaLabelForAction = withAriaLabelForAction,
        )
    }

    protected fun getProvideLaterDeadlineText(deadlineMessageKey: String): String {
        // Occupied-at-registration properties anchor the 28-day deadline to their registration date.
        val deadline = registrationDate.plusDays(PROVIDE_LATER_DEADLINE_DAYS.toLong())
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
