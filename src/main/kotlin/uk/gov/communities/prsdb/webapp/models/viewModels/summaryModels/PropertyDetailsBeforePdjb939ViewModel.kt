package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

// TODO PDJB-939: delete this class and its message keys when the flag is permanently on.
class PropertyDetailsBeforePdjb939ViewModel(
    propertyOwnership: PropertyOwnership,
    isLandlordView: Boolean = true,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, isLandlordView, messageSource) {
    // Property state may have been created while the flag was on, so an occupied property can be in a
    // provide-later state with no tenancy details. This view cannot render those, so it treats such a
    // property as unoccupied and hides the tenancy section.
    private val isTenancyProvideLater = propertyOwnership.tenancyProvideLater == true

    val effectivelyOccupied = isOccupied && !isTenancyProvideLater

    val effectiveIsOccupiedKey: String =
        if (effectivelyOccupied) {
            "propertyDetails.occupationStatus.occupied"
        } else {
            "propertyDetails.occupationStatus.unoccupied"
        }

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
            add(
                occupiedRow(
                    "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied",
                    effectivelyOccupied,
                ),
            )
            if (effectivelyOccupied) {
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
