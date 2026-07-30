package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.UpdateDetailsTodoStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OrgLandlordRegistrationTask(
    journeyStateService: JourneyStateService,
    override val yourDetailsStep: YourDetailsStep,
    override val orgNameStep: OrgNameStep,
    override val orgAddressStep: OrgAddressStep,
    override val orgEmailStep: OrgEmailStep,
    override val orgPhoneNumberStep: OrgPhoneNumberStep,
    override val orgTypeStep: OrgTypeStep,
    override val leadTrusteeTask: LeadTrusteeTask,
    override val charityTask: OrgCharityTask,
    override val companiesHouseTask: OrgCompaniesHouseTask,
    override val orgGovBodyTask: OrgGovBodyTask,
    override val orgMainContactStep: OrgMainContactStep,
    // TODO PDJB-1237 PDJB-1238: remove this placeholder once the org type and companies house update journeys exist.
    override val updateDetailsTodoStep: UpdateDetailsTodoStep,
) : DuplicableTask<LandlordRegistrationOrgLandlordState>(journeyStateService),
    LandlordRegistrationOrgLandlordState {
    override val taskState get() = this

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
                nextDestination { mode ->
                    when (mode) {
                        OrgTypeMode.INCLUDES_TRUST -> Destination(journey.leadTrusteeTask.firstStep)
                        OrgTypeMode.EXCLUDES_TRUST -> Destination(journey.charityTask.firstStep)
                    }
                }
            }
            duplicableTask(journey.leadTrusteeTask) {
                parents { journey.orgTypeStep.hasOutcome(OrgTypeMode.INCLUDES_TRUST) }
                nextStep { journey.charityTask.firstStep }
            }
            duplicableTask(journey.charityTask) {
                parents {
                    OrParents(
                        journey.leadTrusteeTask.isComplete(),
                        journey.orgTypeStep.hasOutcome(OrgTypeMode.EXCLUDES_TRUST),
                    )
                }
                nextStep { journey.companiesHouseTask.firstStep }
            }
            duplicableTask(journey.companiesHouseTask) {
                parents { journey.charityTask.isComplete() }
                nextDestination {
                    if (journey.companiesHouseTask.orgCompaniesHouseStep.outcome == YesOrNo.NO) {
                        Destination(journey.orgGovBodyTask.firstStep)
                    } else {
                        Destination(journey.orgMainContactStep)
                    }
                }
            }
            duplicableTask(journey.orgGovBodyTask) {
                parents { journey.companiesHouseTask.orgCompaniesHouseStep.hasOutcome(YesOrNo.NO) }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgMainContactStep) {
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        AndParents(
                            journey.companiesHouseTask.isComplete(),
                            journey.companiesHouseTask.orgCompaniesHouseStep.hasOutcome(YesOrNo.YES),
                        ),
                        journey.orgGovBodyTask.isComplete(),
                    )
                }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.orgMainContactStep.isComplete() }
            }
        }
}
