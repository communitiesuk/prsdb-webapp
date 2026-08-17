package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.checkAnswersChangeJourneys

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

@JourneyFrameworkComponent
class OrgCompaniesHouseChangeGovBodyTask(
    journeyStateService: JourneyStateService,
    override val interruptionStep: OrgCompaniesHouseInterruptionStep,
    override val govBodyMembersBackRoutingStep: GovBodyMembersBackRoutingStep,
) : Task<OrgCompaniesHouseChangeGovBodyState, OrgCompaniesHouseChangeGovBodyDependencies>(journeyStateService),
    OrgCompaniesHouseChangeGovBodyState {
    override val taskState get() = this

    override val orgIsRegisteredCompanyStep get() = dependencies.orgIsRegisteredCompanyStep
    override val orgCompaniesHouseUpdateRoutingStep get() = dependencies.orgCompaniesHouseUpdateRoutingStep
    override val orgCompanyNumberStep: OrgCompanyNumberStep get() = dependencies.orgCompanyNumberStep
    override val orgGovBodyMembersTask get() = dependencies.orgGovBodyState.orgGovBodyMembersTask
    override val govBodyDetailsModeState get() = dependencies.orgGovBodyState

    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>?
        get() = dependencies.orgGovBodyState.governingBodyMembersMap
        set(value) {
            dependencies.orgGovBodyState.governingBodyMembersMap = value
        }
    override var nextGoverningBodyMemberId: Int?
        get() = dependencies.orgGovBodyState.nextGoverningBodyMemberId
        set(value) {
            dependencies.orgGovBodyState.nextGoverningBodyMemberId = value
        }
    override var editingGovBodyMemberId: Int?
        get() = dependencies.orgGovBodyState.editingGovBodyMemberId
        set(value) {
            dependencies.orgGovBodyState.editingGovBodyMemberId = value
        }

    override fun makeSubJourney(state: OrgCompaniesHouseChangeGovBodyState) =
        subJourney(state) {
            step<Complete, OrgCompaniesHouseInterruptionStepConfig>(journey.interruptionStep) {
                routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
                stepSpecificInitialisation {
                    recordingGovBodyDetailsCompleteVia(journey.govBodyDetailsModeState)
                }
                nextStep {
                    if (journey.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                        journey.orgCompanyNumberStep
                    } else {
                        journey.orgGovBodyMembersTask.firstStep
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents {
                    AndParents(
                        journey.interruptionStep.isComplete(),
                        journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES),
                    )
                }
                backDestination { Destination(journey.interruptionStep) }
                nextStep { exitStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents {
                    AndParents(
                        journey.interruptionStep.isComplete(),
                        journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO),
                    )
                }
                backDestination { Destination(journey.interruptionStep) }
                nextStep { journey.govBodyMembersBackRoutingStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        listState = journey,
                    )
                }
            }
            step<AnyMembers, GovBodyMembersBackRoutingStepConfig>(journey.govBodyMembersBackRoutingStep) {
                stepSpecificInitialisation { usingMembersList { journey.governingBodyMembersMap } }
                parents { journey.orgGovBodyMembersTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.interruptionStep
                        AnyMembers.SOME_MEMBERS -> exitStep
                    }
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.orgCompanyNumberStep.isComplete(),
                        journey.govBodyMembersBackRoutingStep.hasOutcome(AnyMembers.SOME_MEMBERS),
                    )
                }
            }
        }
}
