package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionOutcome
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

// Companies House change sub-flow from the organisation CYA page; the interruption only shows on a changed answer.
fun <T : LandlordRegistrationState> JourneyBuilder<T>.orgCompaniesHouseChangeCyaFlow() {
    step(journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep) {
        initialStep()
        routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
        nextStep { journey.orgCompaniesHouseUpdateRoutingStep }
    }
    step<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateRoutingStepConfig>(journey.orgCompaniesHouseUpdateRoutingStep) {
        stepSpecificInitialisation {
            usingPreviousIsRegisteredCompany {
                getPreviousIsRegisteredCompanyFromBaseJourney(
                    journey,
                    journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep,
                )
            }
        }
        parents { journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep.isComplete() }
        nextDestination { mode ->
            when (mode) {
                OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY ->
                    Destination(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep)
                OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY ->
                    Destination(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.firstStep)
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY -> Destination(journey.orgCompaniesHouseInterruptionStep)
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY -> Destination(journey.orgCompaniesHouseInterruptionStep)
            }
        }
    }
    step<OrgCompaniesHouseInterruptionOutcome, OrgCompaniesHouseInterruptionStepConfig>(journey.orgCompaniesHouseInterruptionStep) {
        routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
        stepSpecificInitialisation {
            usingChangingToCompany {
                journey.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY
            }
        }
        parents {
            OrParents(
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
            )
        }
        nextDestination { outcome ->
            when (outcome) {
                OrgCompaniesHouseInterruptionOutcome.TO_COMPANY ->
                    Destination(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep)
                OrgCompaniesHouseInterruptionOutcome.TO_NON_COMPANY ->
                    Destination(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.firstStep)
            }
        }
    }
    step(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep) {
        routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
        parents {
            OrParents(
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY),
                journey.orgCompaniesHouseInterruptionStep.hasOutcome(OrgCompaniesHouseInterruptionOutcome.TO_COMPANY),
            )
        }
        nextStep { journey.finishCyaStep }
    }
    task(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask) {
        parents {
            OrParents(
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY),
                journey.orgCompaniesHouseInterruptionStep.hasOutcome(OrgCompaniesHouseInterruptionOutcome.TO_NON_COMPANY),
            )
        }
        nextStep { journey.finishCyaStep }
        withDependencies {
            OrgGovBodyMembersDependencies(
                whoToProvideEmptyBackDestination = {
                    Destination(journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep)
                },
                removeLastMemberDestination = {
                    Destination(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.orgGovBodyWhoToProvideStep)
                },
            )
        }
    }
}
