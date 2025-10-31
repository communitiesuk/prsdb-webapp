package uk.gov.communities.prsdb.webapp.journeys.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.VisitableJourneyElement
import uk.gov.communities.prsdb.webapp.journeys.always
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.example.steps.CheckEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcQuestionStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooCheckAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.FooTaskListStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.SearchEpcStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.example.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.example.tasks.OccupationTask
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel

@PrsdbWebService
class FooExampleJourneyFactory(
    private val stateFactory: ObjectFactory<FooJourneyState>,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, VisitableJourneyElement> {
        val state = stateFactory.getObject()
        state.validateStateMatchesPropertyId(propertyId)

        return journey(stateFactory.getObject()) {
            unreachableStepStep { journey.taskListStep }
            step("task-list", journey.taskListStep) {
                nextUrl { "task-list" }
            }
            task(journey.occupationTask) {
                parents { journey.taskListStep.always() }
                redirectToStep { journey.fooCheckYourAnswersStep }
            }
            task(journey.epcTask) {
                parents { journey.occupationTask.hasOutcome(Complete.COMPLETE) }
                redirectToStep { journey.fooCheckYourAnswersStep }
            }
            step("check-your-answers", journey.fooCheckYourAnswersStep) {
                parents {
                    AndParents(
                        journey.occupationTask.hasOutcome(Complete.COMPLETE),
                        journey.epcTask.hasOutcome(Complete.COMPLETE),
                    )
                }
                nextUrl { "/" }
            }
        }
    }

    fun initializeJourneyState(propertyId: Long): String = stateFactory.getObject().initializeJourneyState(propertyId)
}

@PrsdbWebComponent
@Scope("prototype")
class FooJourneyState(
    val taskListStep: FooTaskListStep,
    override val occupied: OccupiedStep,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
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
) : AbstractJourneyState(journeyStateService),
    OccupiedJourneyState,
    EpcJourneyState {
    override var automatchedEpc: EpcDataModel? by mutableDelegate("automatchedEpc", serializer())
    override var searchedEpc: EpcDataModel? by mutableDelegate("searchedEpc", serializer())
    override val propertyId: Long by requiredDelegate("propertyId", serializer())

    // TODO PRSD-1546: Choose where to initialize and validate journey state
    final fun initializeJourneyState(propertyId: Long): String {
        val journeyId = generateJourneyId(propertyId)

        journeyStateService
            .initialiseJourneyWithId(journeyId) {
                setValue("propertyId", Json.Default.encodeToString(serializer(), propertyId))
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
