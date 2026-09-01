package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey

/**
 * View model for the letting-agent Property Details page.
 *
 * It extends [PropertyDetailsViewModelBase] with `isLandlordView = false` so that every reused row is
 * rendered without a change link. Change links for the letting-agent view will be added by the
 * respective update-journey tickets (PDJB-1571 to PDJB-1579).
 */
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

    val licensingSection: List<SummaryListRowViewModel> =
        if (isLicensingProvideLater) {
            listOf(licensingProvideLaterRow())
        } else {
            listOfNotNull(licensingTypeRow(), licensingNumberRow())
        }

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
