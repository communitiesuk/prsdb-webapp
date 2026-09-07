package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.governingBody

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.ORGANISATION_CONTACTS_FRAGMENT
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.GovBodyMembersListState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberListStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgGovBodyMembersTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import java.security.Principal

@PrsdbWebService
class UpdateGoverningBodyJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateGoverningBodyJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return if (state.checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            checkYourAnswersJourneyMap(state)
        }
    }

    private fun mainJourneyMap(state: UpdateGoverningBodyJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }

            step(journey.initialiseGovBodyMembersStep) {
                initialStep()
                routeSegment(InitialiseGovBodyMembersForGovBodyUpdateStep.ROUTE_SEGMENT)
                nextStep { journey.orgGovBodyMembersTask.firstStep }
            }
            task(journey.orgGovBodyMembersTask) {
                parents { journey.initialiseGovBodyMembersStep.isComplete() }
                backUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
                nextStep { journey.cyaStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        listState = journey,
                        allowRemovingLastMember = false,
                    )
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateGoverningBodyCyaStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyMembersTask.isComplete() }
                nextUrl { "$LANDLORD_DETAILS_FOR_LANDLORD_ROUTE#$ORGANISATION_CONTACTS_FRAGMENT" }
            }
        }

    private fun checkYourAnswersJourneyMap(state: UpdateGoverningBodyJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configure { withAdditionalContentProperty { "title" to "landlordDetails.update.title" } }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            unreachableStepDestination { journey.returnToCyaPageDestination }

            when (state.checkingAnswersFor) {
                OrgGovBodyMemberListStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(
                        journey.orgGovBodyMembersTask,
                        { OrgGovBodyMembersDependencies(listState = journey, allowRemovingLastMember = false) },
                    )
                    configureStep(journey.orgGovBodyMembersTask.orgGovBodyMemberListStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                else -> {
                    throw IllegalStateException("Unknown step being checked: ${state.checkingAnswersFor}")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
        }

    fun initializeJourneyState(user: Principal): String = stateFactory.getObject().initializeOrRestoreState(user)
}

@JourneyFrameworkComponent
class UpdateGoverningBodyJourney(
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
    override val initialiseGovBodyMembersStep: InitialiseGovBodyMembersForGovBodyUpdateStep,
    override val cyaStep: UpdateGoverningBodyCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateGoverningBodyJourneyState>,
    journeyStateService: JourneyStateService,
    private val journeyName: String = "governing-body",
) : AbstractJourneyState(journeyStateService),
    UpdateGoverningBodyJourneyState {
    override var governingBodyMembersMap: Map<Int, GoverningBodyMemberDataModel>? by delegateProvider.nullableDelegate(
        "governingBodyMembersMap",
    )
    override var nextGoverningBodyMemberId: Int? by delegateProvider.nullableDelegate("nextGoverningBodyMemberId")
    override var editingGovBodyMemberId: Int? by delegateProvider.nullableDelegate("editingGovBodyMemberId")
    override var governingBodyMembersInitialised: Boolean? by delegateProvider.nullableDelegate("governingBodyMembersInitialised")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override fun generateJourneyId(seed: Any?): String {
        val user: Principal? = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(
            user?.let { "Update $journeyName for landlord ${it.name}" },
        )
    }
}

interface UpdateGoverningBodyJourneyState :
    CheckYourAnswersJourneyState,
    GovBodyMembersListState {
    val orgGovBodyMembersTask: OrgGovBodyMembersTask
    val initialiseGovBodyMembersStep: InitialiseGovBodyMembersForGovBodyUpdateStep
    override val cyaStep: UpdateGoverningBodyCyaStep
    override val finishCyaStep: FinishCyaJourneyStep
    var governingBodyMembersInitialised: Boolean?
}
