package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionOutcome
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseInterruptionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbWebService
class UpdateCompaniesHouseJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateCompaniesHouseJourney>,
    private val userToLandlordService: UserToLandlordService,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }

            step(journey.orgIsRegisteredCompanyStep) {
                initialStep()
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextStep { journey.orgCompaniesHouseUpdateRoutingStep }
            }
            step<OrgCompaniesHouseUpdateRouteMode, OrgCompaniesHouseUpdateRoutingStepConfig>(journey.orgCompaniesHouseUpdateRoutingStep) {
                stepSpecificInitialisation {
                    usingPreviousIsRegisteredCompany { getPreviousIsRegisteredCompanyFromDatabase(userToLandlordService) }
                }
                parents { journey.orgIsRegisteredCompanyStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY -> Destination(journey.orgCompanyNumberStep)
                        OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY -> Destination(journey.orgGovBodyMembersTask.firstStep)
                        OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY -> Destination(journey.interruptionStep)
                        OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY -> Destination(journey.interruptionStep)
                    }
                }
            }
            step<OrgCompaniesHouseInterruptionOutcome, OrgCompaniesHouseInterruptionStepConfig>(journey.interruptionStep) {
                routeSegment(OrgCompaniesHouseInterruptionStep.ROUTE_SEGMENT)
                stepSpecificInitialisation {
                    usingChangingToCompany {
                        journey.orgCompaniesHouseUpdateRoutingStep.outcome == OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY
                    }
                }
                parents {
                    OrParents(
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY),
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY),
                    )
                }
                nextStep { outcome ->
                    when (outcome) {
                        OrgCompaniesHouseInterruptionOutcome.TO_COMPANY -> journey.orgCompanyNumberStep
                        OrgCompaniesHouseInterruptionOutcome.TO_NON_COMPANY -> journey.orgGovBodyMembersTask.firstStep
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED_COMPANY),
                        journey.interruptionStep.hasOutcome(OrgCompaniesHouseInterruptionOutcome.TO_COMPANY),
                    )
                }
                nextStep { journey.checkAnswersStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents {
                    OrParents(
                        journey.orgCompaniesHouseUpdateRoutingStep.hasOutcome(OrgCompaniesHouseUpdateRouteMode.UNCHANGED_NON_COMPANY),
                        journey.interruptionStep.hasOutcome(OrgCompaniesHouseInterruptionOutcome.TO_NON_COMPANY),
                    )
                }
                nextStep { journey.checkAnswersStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        whoToProvideEmptyBackDestination = { Destination(journey.orgIsRegisteredCompanyStep) },
                        removeLastMemberDestination = { Destination(journey.orgGovBodyMembersTask.orgGovBodyWhoToProvideStep) },
                    )
                }
            }
            step(journey.checkAnswersStep) {
                routeSegment(CompaniesHouseUpdateCheckAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.orgCompanyNumberStep.isComplete(),
                        journey.orgGovBodyMembersTask.isComplete(),
                    )
                }
                nextStep { journey.completeCompaniesHouseUpdateStep }
            }
            step(journey.completeCompaniesHouseUpdateStep) {
                parents { journey.checkAnswersStep.isComplete() }
                nextUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateCompaniesHouseJourney(
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    override val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep,
    override val interruptionStep: OrgCompaniesHouseInterruptionStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val checkAnswersStep: CompaniesHouseUpdateCheckAnswersStep,
    override val completeCompaniesHouseUpdateStep: CompleteCompaniesHouseUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "companies-house",
) : AbstractJourneyState(journeyStateService),
    UpdateCompaniesHouseJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateCompaniesHouseJourneyState : OrgCompaniesHouseUpdateState {
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
    val orgCompaniesHouseUpdateRoutingStep: OrgCompaniesHouseUpdateRoutingStep
    val interruptionStep: OrgCompaniesHouseInterruptionStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val checkAnswersStep: CompaniesHouseUpdateCheckAnswersStep
    val completeCompaniesHouseUpdateStep: CompleteCompaniesHouseUpdateStep
}
