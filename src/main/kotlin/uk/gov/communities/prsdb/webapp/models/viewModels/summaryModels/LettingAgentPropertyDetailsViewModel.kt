package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey

class LettingAgentPropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    private val complianceAllValid: Boolean,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, isLandlordView = false, messageSource) {
    val isLicensingProvideLater: Boolean = propertyOwnership.licenseProvideLater == true

    val isTenancyProvideLater: Boolean = propertyOwnership.tenancyProvideLater == true

    val showTenancySection: Boolean = isOccupied

    val showProvideDetailsBanner: Boolean =
        isLicensingProvideLater || (showTenancySection && isTenancyProvideLater) || !complianceAllValid

    val provideDetailsBannerText: String =
        if (hasBeenOccupiedSinceRegistration) {
            getProvideLaterDeadlineText("propertyDetails.lettingAgentView.provideDetailsBanner")
        } else {
            messageSource.getMessageForKey("propertyDetails.lettingAgentView.provideDetailsBannerNoDeadline")
        }

    // TODO PDJB-1571: Re-enable the licensing change link by building this section with change links.
    val licensingSection: List<SummaryListRowViewModel> =
        if (isLicensingProvideLater) {
            listOf(licensingProvideLaterRow())
        } else {
            listOfNotNull(licensingTypeRow(), licensingNumberRow())
        }

    // TODO PDJB-1572 to PDJB-1576: Re-enable the tenancy change links (tenancy, households/tenants, bills, furnished, rent) by building this section with change links.
    val tenancySection: List<SummaryListRowViewModel> =
        when {
            !showTenancySection -> {
                emptyList()
            }

            isTenancyProvideLater -> {
                listOf(tenancyProvideLaterRow())
            }

            else -> {
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
        }

    private fun licensingProvideLaterRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.licensing.rowName",
            if (hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.licensing.provideLaterWithDeadline")
            } else {
                "propertyDetails.propertyRecord.licensing.provideLaterNoDeadline"
            },
            withActionLink = false,
        )

    private fun tenancyProvideLaterRow(): SummaryListRowViewModel =
        row(
            "propertyDetails.propertyRecord.tenancy.rowName",
            if (hasBeenOccupiedSinceRegistration) {
                getProvideLaterDeadlineText("propertyDetails.propertyRecord.tenancy.provideLaterWithDeadline")
            } else {
                "propertyDetails.propertyRecord.tenancy.provideLaterNoDeadline"
            },
            withActionLink = false,
        )
}
