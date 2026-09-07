package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel

class LettingAgentPropertyDetailsViewModel(
    propertyOwnership: PropertyOwnership,
    propertyCompliance: PropertyCompliance,
    messageSource: MessageSource,
) : PropertyDetailsViewModelBase(propertyOwnership, PropertyDetailsViewType.LETTING_AGENT, messageSource) {
    init {
        check(propertyOwnership.isOccupied) {
            "Property ownership ${propertyOwnership.id} is not occupied and cannot be shown in the letting agent view"
        }
    }

    val showProvideDetailsInset: Boolean =
        hasBeenOccupiedSinceRegistration &&
            (
                isLicensingProvideLater ||
                    isTenancyProvideLater ||
                    ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAnyProvideLater
            )

    val provideDetailsInsetText: String =
        getProvideLaterDeadlineText("propertyDetails.lettingAgentView.provideDetailsInset")

    val licensingSection: List<SummaryListRowViewModel> = buildLicensingSection()

    val tenancySection: List<SummaryListRowViewModel> = buildTenancySection()
}
