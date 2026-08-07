package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCompaniesHouseChangeDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCompaniesHouseChangeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// The Companies House change sub-journey reached from the organisation check-your-answers page: the landlord re-answers
// whether they are registered with Companies House, and - only if that answer has changed - sees an interruption page
// (TODO PDJB-1447) before either updating their company number (Yes) or recording their governing body members (No),
// then returns to the check-your-answers page. It reuses the registration steps (keyed by route segment) so answers are
// shared, and detects the change via the shared routing step using OrgCompaniesHouseChangeDependencies.
@JourneyFrameworkComponent
class OrgCompaniesHouseChangeTask(
    journeyStateService: JourneyStateService,
    override val companiesHouseTask: OrgCompaniesHouseTask,
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep,
    override val orgCompaniesHouseInterruptionStep: OrgCompaniesHouseInterruptionStep,
) : Task<OrgCompaniesHouseChangeState, OrgCompaniesHouseChangeDependencies>(journeyStateService),
    OrgCompaniesHouseChangeState {
    override val taskState get() = this

    // The answer held before this change flow started, supplied by the enclosing journey and consumed by the routing step.
    override val previousIsRegisteredCompany: YesOrNo?
        get() = dependencies.previousIsRegisteredCompany()

    override fun makeSubJourney(state: OrgCompaniesHouseChangeState) =
        subJourney(state) {
            step(journey.companiesHouseTask.orgIsRegisteredCompanyStep) {
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                nextStep { journey.orgCompaniesHouseUpdateRoutingStep }
            }
            step(journey.orgCompaniesHouseUpdateRoutingStep) {
                parents { journey.companiesHouseTask.orgIsRegisteredCompanyStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        OrgCompaniesHouseUpdateRouteMode.UNCHANGED ->
                            if (journey.companiesHouseTask.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                                Destination(journey.companiesHouseTask.orgCompanyNumberStep)
                            } else {
                                Destination(journey.orgGovBodyMembersTask.firstStep)
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
                        Destination(journey.companiesHouseTask.orgCompanyNumberStep)
                    } else {
                        Destination(journey.orgGovBodyMembersTask.firstStep)
                    }
                }
            }
            step(journey.companiesHouseTask.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        AndParents(
                            journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED),
                            journey.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES),
                        ),
                        AndParents(
                            journey.orgCompaniesHouseInterruptionStep.isComplete(),
                            journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                        ),
                    )
                }
                nextStep { exitStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents {
                    OrParents(
                        AndParents(
                            journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED),
                            journey.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO),
                        ),
                        AndParents(
                            journey.orgCompaniesHouseInterruptionStep.isComplete(),
                            journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
                        ),
                    )
                }
                nextStep { exitStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        whoToProvideEmptyBackDestination = { Destination(journey.companiesHouseTask.orgIsRegisteredCompanyStep) },
                        removeLastMemberDestination = { Destination(journey.orgGovBodyMembersTask.orgGovBodyWhoToProvideStep) },
                    )
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.companiesHouseTask.orgCompanyNumberStep.isComplete(),
                        journey.orgGovBodyMembersTask.isComplete(),
                    )
                }
            }
        }
}
