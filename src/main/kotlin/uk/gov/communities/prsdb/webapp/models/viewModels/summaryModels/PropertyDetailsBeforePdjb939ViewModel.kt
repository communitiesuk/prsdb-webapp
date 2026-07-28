package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// TODO PDJB-939: delete this class and its message keys when the flag is permanently on.
class PropertyDetailsBeforePdjb939ViewModel(
    propertyOwnership: PropertyOwnership,
    isLandlordView: Boolean = true,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, isLandlordView, messageSource) {
    val beforePdjb939PropertyRecord: List<SummaryListRowViewModel> =
        listOfNotNull(
            registrationDateRow(),
            registrationNumberRow(),
            beforePdjb939AddressRow(),
            beforePdjb939UprnRow(),
            localCouncilRow(),
            propertyTypeRow(),
            ownershipTypeRow("propertyDetails.propertyRecord.beforePdjb939.ownershipType"),
        )

    val beforePdjb939LicensingInformation: List<SummaryListRowViewModel> =
        listOfNotNull(licensingTypeRow(), licensingNumberRow())

    val beforePdjb939TenancyAndRentalInformation: List<SummaryListRowViewModel> =
        buildList {
            add(occupiedRow("propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied"))
            if (isOccupied) {
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

    private fun beforePdjb939AddressRow(): SummaryListRowViewModel =
        row("propertyDetails.propertyRecord.beforePdjb939.address", address, withActionLink = false)

    private fun beforePdjb939UprnRow(): SummaryListRowViewModel? =
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
}
