package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.factory.ObjectFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.journeys.example.FooExampleJourney
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Controller
@RequestMapping("new-journey")
class JourneyTestController(
    val factory: ObjectFactory<FooExampleJourney>,
) {
    @GetMapping("{propertyId}/{stepName}")
    fun getStep(
        @PathVariable("propertyId") propertyId: Long,
        @PathVariable("stepName") stepName: String,
    ): ModelAndView {
        val journey = factory.getObject()
        journey.journeyStateInitialisation(propertyId)
        return journey.buildJourneySteps(propertyId.toString())[stepName]?.getStepModelAndView() ?: throw Exception("Step not found")
    }

    @PostMapping("{propertyId}/{stepName}")
    fun postStep(
        @PathVariable("propertyId") propertyId: Long,
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView =
        factory.getObject().buildJourneySteps(propertyId.toString())[stepName]?.postStepModelAndView(formData)
            ?: throw Exception("Step not found")
}

@Component
@Scope("prototype")
class FooStep : AbstractStep<Complete, NoInputFormModel, DynamicJourneyState>() {
    override fun getStepSpecificContent(state: DynamicJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "certificateNumber" to getReleventEpc(state),
        )

    override fun chooseTemplate() = "forms/epcSupersededForm"

    override val formModelClazz = NoInputFormModel::class

    override fun mode(state: DynamicJourneyState) = formModel?.let { Complete.COMPLETE }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    lateinit var getReleventEpc: (DynamicJourneyState) -> String?
}

/*
@Component
@Scope("prototype")
class ExperimentalJourney(
    override val step1: FooStep,
    override val step2: FooStep,
    override val step3: FooStep,
    override val step4: FooStep,
    journeyStateService: JourneyStateService,
) : AbstractJourney(journeyStateService),
    EpcJourneyState {
    override fun buildJourneySteps(journeyId: String): Map<String, StepLifecycleOrchestrator> {
        initialise("experimental-journey-$journeyId")
        return journey(this) {
            step("one", step1) {
                redirectToStep { step2 }
                stepSpecificInitialisation {
                    getReleventEpc = { automatchedEpc?.certificateNumber }
                }
            }
            step("two", step2) {
                parents { step1.hasOutcome(Complete.COMPLETE) }
                redirectToStep { step3 }
                stepSpecificInitialisation {
                    getReleventEpc = { automatchedEpc?.certificateNumber }
                }
            }
            step("three", step3) {
                parents { step2.hasOutcome(Complete.COMPLETE) }
                redirectToStep { step4 }
                stepSpecificInitialisation {
                    getReleventEpc = { searchedEpc?.certificateNumber }
                }
            }
            step("four", step4) {
                parents { step3.hasOutcome(Complete.COMPLETE) }
                redirectToUrl { "." }
                stepSpecificInitialisation {
                    getReleventEpc = { automatchedEpc?.certificateNumber }
                }
            }
        }
    }

    override var automatchedEpc: EpcDataModel? by delegate("automatchedEpc", serializer())
    override var searchedEpc: EpcDataModel? by delegate("searchedEpc", serializer())
    override val propertyId: Long by compulsoryDelegate("propertyId", serializer())
}
*/
