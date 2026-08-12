package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.checkAnswersChangeJourneys

import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

// Companies House change sub-journey from the organisation CYA page; the interruption only shows on a changed answer.
fun <T : LandlordRegistrationState> JourneyBuilder<T>.orgCompaniesHouseChangeCyaJourney() {
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
                OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY -> Destination(journey.finishCyaStep)
                OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY -> Destination(journey.finishCyaStep)
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY -> Destination(journey.orgCompaniesHouseInterruptionStep)
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY -> Destination(journey.orgCompaniesHouseInterruptionStep)
            }
        }
    }
    step<Complete, OrgCompaniesHouseInterruptionStepConfig>(journey.orgCompaniesHouseInterruptionStep) {
        routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
        stepSpecificInitialisation {
            recordingGovBodyDetailsCompleteAt(OrgGovBodyDetailsStep.ROUTE_SEGMENT)
        }
        parents {
            OrParents(
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
            )
        }
        nextStep {
            if (journey.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY) {
                journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep
            } else {
                journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.firstStep
            }
        }
    }
    step(journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep) {
        routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
        parents {
            AndParents(
                journey.orgCompaniesHouseInterruptionStep.isComplete(),
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
            )
        }
        backDestination { Destination(journey.orgCompaniesHouseInterruptionStep) }
        nextStep { journey.finishCyaStep }
    }
    task(journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask) {
        parents {
            AndParents(
                journey.orgCompaniesHouseInterruptionStep.isComplete(),
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
            )
        }
        backDestination { Destination(journey.orgCompaniesHouseInterruptionStep) }
        nextStep { journey.orgCompaniesHouseChangeGovBodyMembersBackRoutingStep }
        withDependencies {
            OrgGovBodyMembersDependencies(
                listState = journey.orgLandlordRegistrationTask.orgGovBodyTask,
            )
        }
    }
    step<AnyMembers, GovBodyMembersBackRoutingStepConfig>(journey.orgCompaniesHouseChangeGovBodyMembersBackRoutingStep) {
        stepSpecificInitialisation {
            usingMembersList { journey.orgLandlordRegistrationTask.orgGovBodyTask.governingBodyMembersMap }
        }
        parents { journey.orgLandlordRegistrationTask.orgGovBodyTask.orgGovBodyMembersTask.isComplete() }
        nextDestination { mode ->
            when (mode) {
                AnyMembers.NO_MEMBERS -> Destination(journey.orgCompaniesHouseInterruptionStep)
                AnyMembers.SOME_MEMBERS -> Destination(journey.finishCyaStep)
            }
        }
    }
}
