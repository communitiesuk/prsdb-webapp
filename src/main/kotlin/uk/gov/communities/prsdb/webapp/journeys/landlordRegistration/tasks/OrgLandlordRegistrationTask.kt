package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.UpdateDetailsTodoStep

@JourneyFrameworkComponent
class OrgLandlordRegistrationTask(
    journeyStateService: JourneyStateService,
    override val orgNameStep: OrgNameStep,
    override val orgAddressStep: OrgAddressStep,
    override val orgEmailStep: OrgEmailStep,
    override val orgPhoneNumberStep: OrgPhoneNumberStep,
    override val orgTypeTask: OrgTypeTask,
    override val charityTask: OrgCharityTask,
    override val companiesHouseTask: OrgCompaniesHouseTask,
    override val orgMainContactStep: OrgMainContactStep,
    // TODO PDJB-1237 PDJB-1238: remove this placeholder once the org type and companies house update journeys exist.
    override val updateDetailsTodoStep: UpdateDetailsTodoStep,
) : DuplicableTask<LandlordRegistrationOrgLandlordState>(journeyStateService),
    LandlordRegistrationOrgLandlordState {
    override val taskState get() = this

    override fun makeSubJourney(state: LandlordRegistrationOrgLandlordState) =
        subJourney(state) {
            step(journey.orgNameStep) {
                routeSegment(OrgNameStep.ROUTE_SEGMENT)
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
                nextStep { journey.orgTypeTask.firstStep }
            }
            duplicableTask(journey.orgTypeTask) {
                parents { journey.orgPhoneNumberStep.isComplete() }
                nextStep { journey.charityTask.firstStep }
            }
            duplicableTask(journey.charityTask) {
                parents { journey.orgTypeTask.isComplete() }
                nextStep { journey.companiesHouseTask.firstStep }
            }
            duplicableTask(journey.companiesHouseTask) {
                parents { journey.charityTask.isComplete() }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgMainContactStep) {
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                parents { journey.companiesHouseTask.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.orgMainContactStep.isComplete() }
            }
        }
}
