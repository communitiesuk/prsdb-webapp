package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyDetailsViewType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
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
        isLicensingProvideLater ||
            isTenancyProvideLater ||
            !ComplianceStatusDataModel.fromPropertyCompliance(propertyCompliance).isAllValid

    val provideDetailsInsetText: String =
        if (hasBeenOccupiedSinceRegistration) {
            getProvideLaterDeadlineText("propertyDetails.lettingAgentView.provideDetailsInset")
        } else {
            messageSource.getMessageForKey("propertyDetails.lettingAgentView.provideDetailsInsetNoDeadline")
        }

    // TODO PDJB-1571, PDJB-1572, PDJB-1573, PDJB-1574, PDJB-1575, PDJB-1576: letting agents will get change links on
    //  these rows (pointing at letting-agent update journeys) once those journeys are built. The shared section
    //  builders show a letting-agent change link on a row only once rowWithViewTypeSpecificChangeLink is given a lettingAgentActionLink
    //  for it; until then the rows render without links (see PropertyDetailsViewModelBase).
    val licensingSection: List<SummaryListRowViewModel> = buildLicensingSection()

    val tenancySection: List<SummaryListRowViewModel> = buildTenancySection()
}
