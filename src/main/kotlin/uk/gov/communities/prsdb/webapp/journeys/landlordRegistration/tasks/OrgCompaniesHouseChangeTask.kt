package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCompaniesHouseChangeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// The Companies House change sub-journey reached from the organisation check-your-answers page: the landlord re-answers
// whether they are registered with Companies House, sees an interruption page (TODO PDJB-1447), then either updates
// their company number (Yes) or records their governing body members (No) before returning to the check-your-answers
// page. It reuses the same steps as the registration journey (keyed by route segment) so answers are shared.
@JourneyFrameworkComponent
class OrgCompaniesHouseChangeTask(
    journeyStateService: JourneyStateService,
    override val companiesHouseTask: OrgCompaniesHouseTask,
    override val orgGovBodyTask: OrgGovBodyTask,
    override val orgCompaniesHouseInterruptionStep: OrgCompaniesHouseInterruptionStep,
) : TaskWithoutDependencies<OrgCompaniesHouseChangeState>(journeyStateService),
    OrgCompaniesHouseChangeState {
    override val taskState get() = this

    override fun makeSubJourney(state: OrgCompaniesHouseChangeState) =
        subJourney(state) {
            step(journey.companiesHouseTask.orgIsRegisteredCompanyStep) {
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                nextStep { journey.orgCompaniesHouseInterruptionStep }
            }
            step(journey.orgCompaniesHouseInterruptionStep) {
                parents { journey.companiesHouseTask.orgIsRegisteredCompanyStep.isComplete() }
                routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
                nextDestination {
                    if (journey.companiesHouseTask.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                        Destination(journey.companiesHouseTask.orgCompanyNumberStep)
                    } else {
                        Destination(journey.orgGovBodyTask.firstStep)
                    }
                }
            }
            step(journey.companiesHouseTask.orgCompanyNumberStep) {
                parents { journey.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES) }
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                nextStep { exitStep }
            }
            task(journey.orgGovBodyTask) {
                parents { journey.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.companiesHouseTask.orgCompanyNumberStep.isComplete(),
                        journey.orgGovBodyTask.isComplete(),
                    )
                }
            }
        }
}
