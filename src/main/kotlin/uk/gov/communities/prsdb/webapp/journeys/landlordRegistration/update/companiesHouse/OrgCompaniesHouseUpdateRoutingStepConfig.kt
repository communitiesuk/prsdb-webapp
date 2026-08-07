package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

enum class OrgCompaniesHouseUpdateRouteMode {
    UNCHANGED,
    CHANGED_TO_COMPANY,
    CHANGED_TO_NON_COMPANY,
}

// Internal routing step that detects whether the landlord's Companies House answer has changed, and if so in which
// direction, so the interruption is shown only on a change.
@JourneyFrameworkComponent
class OrgCompaniesHouseUpdateRoutingStepConfig :
    AbstractInternalStepConfig<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateState>() {
    override fun mode(state: OrgCompaniesHouseUpdateState): OrgCompaniesHouseUpdateRouteMode? {
        val current = state.orgIsRegisteredCompanyStep.outcome ?: return null
        return when {
            current == state.previousIsRegisteredCompany -> OrgCompaniesHouseUpdateRouteMode.UNCHANGED
            current == YesOrNo.YES -> OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY
            else -> OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY
        }
    }
}

@JourneyFrameworkComponent
class OrgCompaniesHouseUpdateRoutingStep(
    stepConfig: OrgCompaniesHouseUpdateRoutingStepConfig,
) : InternalStep<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateState>(stepConfig)
