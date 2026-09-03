package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

class LettingAgentPropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    propertyCompliance: PropertyCompliance,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, isLandlordView = false, messageSource) {
    init {
        check(propertyOwnership.isViewableByLettingAgent) {
            "Property ownership ${propertyOwnership.id} is not viewable by a letting agent"
        }
    }

    val isLicensingProvideLater: Boolean = propertyOwnership.licenseProvideLater == true

    val isTenancyProvideLater: Boolean = propertyOwnership.tenancyProvideLater == true

    val showProvideDetailsInset: Boolean =
        isLicensingProvideLater ||
            isTenancyProvideLater ||
            !ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAllValid

    val provideDetailsInsetText: String =
        if (hasBeenOccupiedSinceRegistration) {
            getProvideLaterDeadlineText("propertyDetails.lettingAgentView.provideDetailsInset")
        } else {
            messageSource.getMessageForKey("propertyDetails.lettingAgentView.provideDetailsInsetNoDeadline")
        }

    // TODO PDJB-1571: Re-enable the licensing change link by building this section with change links.
    val licensingSection: List<SummaryListRowViewModel> =
        if (isLicensingProvideLater) {
            listOf(licensingProvideLaterRow())
        } else {
            listOfNotNull(licensingTypeRow(), licensingNumberRow())
        }

    val tenancySection: List<SummaryListRowViewModel> =
        if (isTenancyProvideLater) {
            // TODO PDJB-1572: Re-enable the tenancy change link.
            listOf(tenancyProvideLaterRow())
        } else {
            buildList {
                // TODO PDJB-1573: Re-enable the households and tenants change links.
                add(householdsRow())
                add(tenantsRow())
                // TODO PDJB-1574: Re-enable the bills change link.
                add(rentIncludesBillsRow())
                if (propertyOwnership.rentIncludesBills) add(billsIncludedRow(includeChangeLink = false))
                // TODO PDJB-1575: Re-enable the furnished change link.
                add(furnishedStatusRow())
                // TODO PDJB-1576: Re-enable the rent change link.
                add(rentFrequencyRow(withoutBottomBorder = true))
                add(rentAmountRow(includeChangeLink = false))
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
