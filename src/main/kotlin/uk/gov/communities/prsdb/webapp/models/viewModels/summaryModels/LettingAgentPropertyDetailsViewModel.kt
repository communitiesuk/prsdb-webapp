package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import java.util.UUID

class LettingAgentPropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    private val complianceAllValid: Boolean,
    private val token: UUID,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, isLandlordView = false, messageSource) {
    init {
        check(isOccupied) {
            "Property ownership ${propertyOwnership.id} is not occupied so cannot be viewed by a letting agent"
        }
    }

    val isLicensingProvideLater: Boolean = propertyOwnership.licenseProvideLater == true

    val isTenancyProvideLater: Boolean = propertyOwnership.tenancyProvideLater == true

    val showProvideDetailsBanner: Boolean =
        isLicensingProvideLater || isTenancyProvideLater || !complianceAllValid

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

    val tenancySection: List<SummaryListRowViewModel> =
        if (isTenancyProvideLater) {
            listOf(tenancyProvideLaterRow())
        } else {
            buildList {
                add(
                    householdsRow(
                        updateRouteBase = LettingAgentUpdateHouseholdsAndTenantsController.getBaseRoute(token),
                        withChangeLink = true,
                    ),
                )
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
            changeLinkMessageKey,
            "${LettingAgentUpdateTenancyDetailsController.getBaseRoute(token)}/${HouseholdStep.ROUTE_SEGMENT}",
            withActionLink = true,
        )
}
