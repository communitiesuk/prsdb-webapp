package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCompaniesHouseChangeDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgCompaniesHouseChangeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// The Companies House change sub-journey reached from the organisation check-your-answers page: the landlord re-answers
// whether they are registered with Companies House, and - only if that answer has changed - sees an interruption page
// (TODO PDJB-1447) before either updating their company number (Yes) or recording their governing body members (No),
// then returns to the check-your-answers page. When the answer is unchanged the interruption is skipped entirely.
// It reuses the same steps as the registration journey (keyed by route segment) so answers are shared, and depends only
// on the landlord's original Companies House answer (via OrgCompaniesHouseChangeDependencies) so it can detect a change.
@JourneyFrameworkComponent
class OrgCompaniesHouseChangeTask(
    journeyStateService: JourneyStateService,
    override val companiesHouseTask: OrgCompaniesHouseTask,
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val orgCompaniesHouseInterruptionStep: OrgCompaniesHouseInterruptionStep,
) : Task<OrgCompaniesHouseChangeState, OrgCompaniesHouseChangeDependencies>(journeyStateService),
    OrgCompaniesHouseChangeState {
    override val taskState get() = this

    // The answer held before this change flow started, supplied by the enclosing journey (which reads it from its base
    // journey - the child copy holds the in-progress re-answer).
    override val originalIsRegisteredCompany: YesOrNo?
        get() = dependencies.originalIsRegisteredCompany()

    override fun makeSubJourney(state: OrgCompaniesHouseChangeState) =
        subJourney(state) {
            step(journey.companiesHouseTask.orgIsRegisteredCompanyStep) {
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                nextDestination { mode ->
                    when {
                        answerHasChanged(journey) -> Destination(journey.orgCompaniesHouseInterruptionStep)
                        mode == YesOrNo.YES -> Destination(journey.companiesHouseTask.orgCompanyNumberStep)
                        else -> Destination(journey.orgGovBodyMembersTask.firstStep)
                    }
                }
            }
            step<Complete, OrgCompaniesHouseInterruptionStepConfig>(journey.orgCompaniesHouseInterruptionStep) {
                stepSpecificInitialisation { originalIsRegisteredCompany = journey.originalIsRegisteredCompany }
                routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
                parents {
                    SingleParent(journey.companiesHouseTask.orgIsRegisteredCompanyStep) {
                        journey.companiesHouseTask.orgIsRegisteredCompanyStep.outcome != null && answerHasChanged(journey)
                    }
                }
                nextDestination {
                    if (journey.companiesHouseTask.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                        Destination(journey.companiesHouseTask.orgCompanyNumberStep)
                    } else {
                        Destination(journey.orgGovBodyMembersTask.firstStep)
                    }
                }
            }
            step(journey.companiesHouseTask.orgCompanyNumberStep) {
                parents { journey.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES) }
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                nextStep { exitStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents { journey.companiesHouseTask.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO) }
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

    // The answer has changed when the landlord has re-answered the question (child copy) with a value that differs from
    // the original (injected onto the interruption step from the base journey).
    private fun answerHasChanged(journey: OrgCompaniesHouseChangeState): Boolean {
        val current = journey.companiesHouseTask.orgIsRegisteredCompanyStep.outcome
        return current != null && current != journey.orgCompaniesHouseInterruptionStep.originalIsRegisteredCompany
    }
}
