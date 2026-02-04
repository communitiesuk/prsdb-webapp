package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.SearchEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

@PrsdbWebService
class FooExampleJourneyFactory(
    private val stateFactory: ObjectFactory<FooJourneyState>,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        state.validateStateMatchesPropertyId(propertyId)

        return journey(stateFactory.getObject()) {
            unreachableStepStep { journey.taskListStep }
            step(journey.taskListStep) {
                routeSegment("task-list")
                initialStep()
                nextUrl { "task-list" }
            }
            section {
                withHeadingMessageKey("tasks-section-part-1")
                task(journey.occupationTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.fooCheckYourAnswersStep }
                }
            }
            section {
                withHeadingMessageKey("tasks-section-part-2")
                task(journey.epcTask) {
                    parents { journey.taskListStep.always() }
                    nextStep { journey.fooCheckYourAnswersStep }
                }
            }
            step(journey.fooCheckYourAnswersStep) {
                routeSegment("check-your-answers")
                parents {
                    AndParents(
                        journey.occupationTask.isComplete(),
                        journey.epcTask.isComplete(),
                    )
                }
                nextUrl { "/" }
            }
        }
    }

    fun initializeJourneyState(propertyId: Long): String = stateFactory.getObject().initializeJourneyState(propertyId)
}

@JourneyFrameworkComponent
class FooJourneyState(
    val taskListStep: FooTaskListStep,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
    override val bedrooms: BedroomsStep,
    override val rentIncludesBills: RentIncludesBillsStep,
    override val billsIncluded: BillsIncludedStep,
    override val furnishedStatus: FurnishedStatusStep,
    override val rentFrequency: RentFrequencyStep,
    override val rentAmount: RentAmountStep,
    override val epcQuestion: EpcQuestionStep,
    override val checkAutomatchedEpc: CheckEpcStep,
    override val searchForEpc: SearchEpcStep,
    override val epcNotFound: EpcNotFoundStep,
    override val epcSuperseded: EpcSupersededStep,
    override val checkSearchedEpc: CheckEpcStep,
    val fooCheckYourAnswersStep: FooCheckAnswersStep,
    private val journeyStateService: JourneyStateService,
    val occupationTask: OccupationTask,
    val epcTask: EpcTask,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    OccupationState,
    EpcJourneyState {
    override var automatchedEpc: EpcDataModel? by delegateProvider.nullableDelegate("automatchedEpc")
    override var searchedEpc: EpcDataModel? by delegateProvider.nullableDelegate("searchedEpc")
    override val propertyId: Long by delegateProvider.requiredDelegate("propertyId")

    // TODO PRSD-1546: Choose where to initialize and validate journey state
    final fun initializeJourneyState(propertyId: Long): String {
        val journeyId = generateJourneyId(propertyId)

        journeyStateService
            .initialiseJourneyWithId(journeyId) {
                setValue("propertyId", Json.encodeToString(serializer(), propertyId))
            }
        return journeyId
    }

    final fun validateStateMatchesPropertyId(currentPropertyId: Long) {
        if (currentPropertyId != propertyId) {
            throw NoSuchJourneyException()
        }
    }

    companion object {
        fun generateJourneyId(propertyId: Long): String =
            "Foo Example Journey for property $propertyId"
                .hashCode()
                .toUInt()
                .times(111113111U)
                .and(0x7FFFFFFFu)
                .toString(36)
    }
}
