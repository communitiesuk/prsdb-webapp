package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.checkAnswersChangeJourneys

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStepConfig

// Companies House change sub-journey from the organisation CYA page; the governing body task only runs on a changed answer.
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
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY ->
                    Destination(journey.orgCompaniesHouseChangeGovBodyTask.firstStep)
                OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY ->
                    Destination(journey.orgCompaniesHouseChangeGovBodyTask.firstStep)
            }
        }
    }
    task(journey.orgCompaniesHouseChangeGovBodyTask) {
        parents {
            OrParents(
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
            )
        }
        backDestination { Destination(journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep) }
        nextStep { journey.finishCyaStep }
        withDependencies {
            OrgCompaniesHouseChangeGovBodyDependencies(
                orgIsRegisteredCompanyStep = journey.orgLandlordRegistrationTask.companiesHouseTask.orgIsRegisteredCompanyStep,
                orgCompanyNumberStep = journey.orgLandlordRegistrationTask.companiesHouseTask.orgCompanyNumberStep,
                orgGovBodyState = journey.orgLandlordRegistrationTask.orgGovBodyTask,
                orgCompaniesHouseUpdateRoutingStep = journey.orgCompaniesHouseUpdateRoutingStep,
            )
        }
    }
}
