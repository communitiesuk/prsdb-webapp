package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

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
                OrgCompaniesHouseUpdateRouteMode.UNCHANGED ->
                    if (journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                        Destination(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep)
                    } else {
                        Destination(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.firstStep)
                    }
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY -> Destination(journey.orgCompaniesHouseInterruptionStep)
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY -> Destination(journey.orgCompaniesHouseInterruptionStep)
            }
        }
    }
    step(journey.orgCompaniesHouseInterruptionStep) {
        routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
        parents {
            OrParents(
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
            )
        }
        nextDestination {
            if (journey.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY) {
                Destination(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep)
            } else {
                Destination(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.firstStep)
            }
        }
    }
    step(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep) {
        routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
        parents {
            OrParents(
                AndParents(
                    journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED),
                    journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES),
                ),
                AndParents(
                    journey.orgCompaniesHouseInterruptionStep.isComplete(),
                    journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                ),
            )
        }
        nextStep { journey.finishCyaStep }
    }
    task(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask) {
        parents {
            OrParents(
                AndParents(
                    journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED),
                    journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO),
                ),
                AndParents(
                    journey.orgCompaniesHouseInterruptionStep.isComplete(),
                    journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
                ),
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
