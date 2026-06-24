package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.YourDetailsStep

@JourneyFrameworkComponent
class LandlordRegistrationOrgLandlordTask : Task<LandlordRegistrationOrgLandlordState>() {
    override fun makeSubJourney(state: LandlordRegistrationOrgLandlordState) =
        subJourney(state) {
            step(journey.yourDetailsStep) {
                routeSegment(YourDetailsStep.ROUTE_SEGMENT)
                nextStep { journey.orgNameStep }
            }
            step(journey.orgNameStep) {
                routeSegment(OrgNameStep.ROUTE_SEGMENT)
                parents { journey.yourDetailsStep.isComplete() }
                nextStep { journey.orgAddressStep }
            }
            step(journey.orgAddressStep) {
                routeSegment(OrgAddressStep.ROUTE_SEGMENT)
                parents { journey.orgNameStep.isComplete() }
                nextStep { journey.orgEmailStep }
            }
            step(journey.orgEmailStep) {
                routeSegment(OrgEmailStep.ROUTE_SEGMENT)
                parents { journey.orgAddressStep.isComplete() }
                nextStep { journey.orgPhoneNumberStep }
            }
            step(journey.orgPhoneNumberStep) {
                routeSegment(OrgPhoneNumberStep.ROUTE_SEGMENT)
                parents { journey.orgEmailStep.isComplete() }
                nextStep { journey.orgTypeStep }
            }
            step(journey.orgTypeStep) {
                routeSegment(OrgTypeStep.ROUTE_SEGMENT)
                parents { journey.orgPhoneNumberStep.isComplete() }
                nextStep { journey.orgCompaniesHouseStep }
            }
            step(journey.orgCompaniesHouseStep) {
                routeSegment(OrgCompaniesHouseStep.ROUTE_SEGMENT)
                parents { journey.orgTypeStep.isComplete() }
                nextStep { journey.orgCharityStep }
            }
            step(journey.orgCharityStep) {
                routeSegment(OrgCharityStep.ROUTE_SEGMENT)
                parents { journey.orgCompaniesHouseStep.isComplete() }
                nextStep { journey.orgDirectorsStep }
            }
            step(journey.orgDirectorsStep) {
                routeSegment(OrgDirectorsStep.ROUTE_SEGMENT)
                parents { journey.orgCharityStep.isComplete() }
                nextStep { journey.orgTrusteesStep }
            }
            step(journey.orgTrusteesStep) {
                routeSegment(OrgTrusteesStep.ROUTE_SEGMENT)
                parents { journey.orgDirectorsStep.isComplete() }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgMainContactStep) {
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                parents { journey.orgTrusteesStep.isComplete() }
                nextStep { journey.orgLandlordCyaStep }
            }
            step(journey.orgLandlordCyaStep) {
                routeSegment(OrgLandlordCyaStep.ROUTE_SEGMENT)
                parents { journey.orgMainContactStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.orgLandlordCyaStep.isComplete() }
            }
        }
}
