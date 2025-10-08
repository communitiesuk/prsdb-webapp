package uk.gov.communities.prsdb.webapp.theJourneyFramework

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
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.factories.JourneyDataServiceFactory
import uk.gov.communities.prsdb.webapp.theJourneyFramework.JourneyBuilder.Companion.journey

@Controller
@RequestMapping("new-journey")
class JourneyTestController(
    val factory: ObjectFactory<ExperimentalJourney>,
) {
    @GetMapping("/{stepName}")
    fun getStep(
        @PathVariable("stepName") stepName: String,
    ): ModelAndView = factory.getObject().initialize()[stepName]?.getStepModelAndView() ?: throw Exception("Step not found")

    @PostMapping("/{stepName}")
    fun postStep(
        @PathVariable("stepName") stepName: String,
        @RequestParam formData: PageData,
    ): ModelAndView = factory.getObject().initialize()[stepName]?.postStepModelAndView(formData) ?: throw Exception("Step not found")
}

@Component
@Scope("prototype")
class FooStep : AbstractStep<Complete, NoInputFormModel, DynamicJourneyState, FooStep>() {
    override fun getStepSpecificContent(state: DynamicJourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "certificateNumber" to if (::getReleventEpc.isInitialized) getReleventEpc(state) else "CERTIFICATE NOT INITIALISED",
        )

    override fun chooseTemplate() = "forms/epcSupersededForm"

    override val formModelClazz = NoInputFormModel::class

    override fun mode(state: DynamicJourneyState) = formModel?.let { Complete.COMPLETE }

    lateinit var getReleventEpc: (DynamicJourneyState) -> String?
}

enum class Complete {
    COMPLETE,
}

@Component
@Scope("prototype")
class ExperimentalJourney(
    journeyDataServiceFactory: JourneyDataServiceFactory,
    val step1: FooStep,
    val step2: FooStep,
    val step3: FooStep,
    val step4: FooStep,
) : AbstractJourney(journeyDataServiceFactory.dataService()) {
    companion object {
        fun JourneyDataServiceFactory.dataService(): JourneyDataService = this.create("key")
    }

    fun initialize(): Map<String, StepConductor> =
        journey(this) {
            step("one", step1) {
                redirectToStep { step2 }
            }
            step("two", step2) {
                parents { step1.hasOutcome(Complete.COMPLETE) }
                redirectToStep { step3 }
            }
            step("three", step3) {
                parents { step2.hasOutcome(Complete.COMPLETE) }
                redirectToStep { step4 }
                stepSpecificInitialisation {
                    getReleventEpc = { journeyData.count().toString() }
                }
            }
            step("four", step4) {
                parents { step3.hasOutcome(Complete.COMPLETE) }
                redirectToUrl { ".." }
            }
        }
}
