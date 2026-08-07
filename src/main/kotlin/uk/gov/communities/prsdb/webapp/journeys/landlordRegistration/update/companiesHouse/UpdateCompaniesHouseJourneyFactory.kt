package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import java.security.Principal

@PrsdbWebService
class UpdateCompaniesHouseJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateCompaniesHouseJourney>,
    private val userToLandlordService: UserToLandlordService,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (state.wasRegisteredWithCompaniesHouse == null) {
            state.wasRegisteredWithCompaniesHouse = userToLandlordService.getCurrentOrganisationLandlordForUser().isCompany
        }

        return journey(state) {
            unreachableStepUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }

            step(journey.orgIsRegisteredCompanyStep) {
                initialStep()
                routeSegment(OrgIsRegisteredCompanyStep.ROUTE_SEGMENT)
                backUrl { LANDLORD_DETAILS_FOR_LANDLORD_ROUTE }
                nextDestination { mode ->
                    when {
                        answerHasChanged(journey) -> Destination(journey.interruptionStep)
                        mode == YesOrNo.YES -> Destination(journey.orgCompanyNumberStep)
                        else -> Destination(journey.orgGovBodyMembersTask.firstStep)
                    }
                }
            }
            step<Complete, CompaniesHouseUpdateInterruptionStepConfig>(journey.interruptionStep) {
                stepSpecificInitialisation { originalIsRegisteredCompany = journey.originalIsRegisteredCompany }
                routeSegment(CompaniesHouseUpdateInterruptionStep.ROUTE_SEGMENT)
                parents {
                    SingleParent(journey.orgIsRegisteredCompanyStep) {
                        journey.orgIsRegisteredCompanyStep.outcome != null && answerHasChanged(journey)
                    }
                }
                nextDestination {
                    if (journey.orgIsRegisteredCompanyStep.outcome == YesOrNo.YES) {
                        Destination(journey.orgCompanyNumberStep)
                    } else {
                        Destination(journey.orgGovBodyMembersTask.firstStep)
                    }
                }
            }
            step(journey.orgCompanyNumberStep) {
                routeSegment(OrgCompanyNumberStep.ROUTE_SEGMENT)
                parents { journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkAnswersStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents { journey.orgIsRegisteredCompanyStep.hasOutcome(YesOrNo.NO) }
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

    // The answer has changed when the landlord has re-answered the question with a value that differs from the original
    // (their current DB record, injected onto the interruption step).
    private fun answerHasChanged(journey: UpdateCompaniesHouseJourneyState): Boolean {
        val current = journey.orgIsRegisteredCompanyStep.outcome
        return current != null && current != journey.interruptionStep.originalIsRegisteredCompany
    }
}

@JourneyFrameworkComponent
class UpdateCompaniesHouseJourney(
    override val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep,
    override val interruptionStep: CompaniesHouseUpdateInterruptionStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val checkAnswersStep: CompaniesHouseUpdateCheckAnswersStep,
    override val completeCompaniesHouseUpdateStep: CompleteCompaniesHouseUpdateStep,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "companies-house",
) : AbstractJourneyState(journeyStateService),
    UpdateCompaniesHouseJourneyState {
    override var wasRegisteredWithCompaniesHouse: Boolean? by delegateProvider.nullableDelegate("wasRegisteredWithCompaniesHouse")

    override val originalIsRegisteredCompany: YesOrNo?
        get() = wasRegisteredWithCompaniesHouse?.let { if (it) YesOrNo.YES else YesOrNo.NO }

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateCompaniesHouseJourneyState : JourneyState {
    val orgIsRegisteredCompanyStep: OrgIsRegisteredCompanyStep
    val interruptionStep: CompaniesHouseUpdateInterruptionStep
    val orgCompanyNumberStep: OrgCompanyNumberStep
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val checkAnswersStep: CompaniesHouseUpdateCheckAnswersStep
    val completeCompaniesHouseUpdateStep: CompleteCompaniesHouseUpdateStep

    // The landlord's Companies House registration status before this update began (their current DB record). Persisted
    // when the journey starts and used to decide whether the interruption should be shown - it is skipped when unchanged.
    var wasRegisteredWithCompaniesHouse: Boolean?
    val originalIsRegisteredCompany: YesOrNo?
}
