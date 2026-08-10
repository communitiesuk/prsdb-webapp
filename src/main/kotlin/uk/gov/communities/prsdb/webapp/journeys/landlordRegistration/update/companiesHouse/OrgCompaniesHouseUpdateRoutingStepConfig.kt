package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

enum class OrgCompaniesHouseUpdateRouteMode {
    UNCHANGED_COMPANY,
    UNCHANGED_NON_COMPANY,
    CHANGED_TO_COMPANY,
    CHANGED_TO_NON_COMPANY,
}

@JourneyFrameworkComponent
class OrgCompaniesHouseUpdateRoutingStepConfig :
    AbstractInternalStepConfig<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateState>() {
    private lateinit var previousIsRegisteredCompany: () -> Boolean

    fun usingPreviousIsRegisteredCompany(previousIsRegisteredCompany: () -> Boolean): OrgCompaniesHouseUpdateRoutingStepConfig {
        this.previousIsRegisteredCompany = previousIsRegisteredCompany
        return this
    }

    fun getPreviousIsRegisteredCompanyFromDatabase(userToLandlordService: UserToLandlordService): Boolean =
        userToLandlordService.getCurrentOrganisationLandlordForUser().isRegisteredCompany

    fun getPreviousIsRegisteredCompanyFromBaseJourney(
        state: CheckYourAnswersJourneyState,
        orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    ): Boolean =
        orgIsRegisteredCompanyStep.stepConfig
            .getFormModelFromState(state.getBaseJourneyState())
            .companiesHouse == true

    override fun isSubClassInitialised() = ::previousIsRegisteredCompany.isInitialized

    override fun mode(state: OrgCompaniesHouseUpdateState): OrgCompaniesHouseUpdateRouteMode? {
        val currentIsRegisteredCompany = state.orgIsRegisteredCompanyStep.outcome?.let { it == YesOrNo.YES } ?: return null
        return when {
            previousIsRegisteredCompany() == currentIsRegisteredCompany ->
                if (currentIsRegisteredCompany) {
                    OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY
                } else {
                    OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY
                }
            currentIsRegisteredCompany -> OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY
            else -> OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY
        }
    }
}

@JourneyFrameworkComponent
class OrgCompaniesHouseUpdateRoutingStep(
    stepConfig: OrgCompaniesHouseUpdateRoutingStepConfig,
) : InternalStep<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateState>(stepConfig)
