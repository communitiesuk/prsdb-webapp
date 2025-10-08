package uk.gov.communities.prsdb.webapp.theJourneyFramework

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.NoParents
import uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps.Parentage
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.collections.plus
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

class StepConductor(
    val innerStep: AbstractStep<*, *, *>,
) : VisitableStep {
    override fun getStepModelAndView(): ModelAndView {
        innerStep.beforeIsStepReachable()
        if (innerStep.isStepReachable) {
            innerStep.afterIsStepReachable(true)

            innerStep.beforeGetStepContent()
            val content = innerStep.getPageVisitContent()
            innerStep.afterGetStepContent()

            innerStep.beforeGetTemplate()
            val template = innerStep.chooseTemplate()
            innerStep.afterGetTemplate()

            return ModelAndView(template, content)
        }
        innerStep.afterIsStepReachable(false)

        val unreachableStepRedirect = innerStep.getUnreachableStepRedirect
        return ModelAndView("redirect:$unreachableStepRedirect")
    }

    override fun postStepModelAndView(formData: PageData): ModelAndView {
        innerStep.beforeIsStepReachable()
        if (innerStep.isStepReachable) {
            innerStep.afterIsStepReachable(true)

            val newFormData = innerStep.beforeValidateSubmittedData(formData)
            val bindingResult = innerStep.validateSubmittedData(newFormData)
            innerStep.afterValidateSubmittedData(bindingResult)

            if (bindingResult.hasErrors()) {
                innerStep.beforeGetStepContent()
                val content = innerStep.getInvalidSubmissionContent(bindingResult)
                innerStep.afterGetStepContent()

                innerStep.beforeGetTemplate()
                val template = innerStep.chooseTemplate()
                innerStep.afterGetTemplate()

                return ModelAndView(template, content)
            }
            innerStep.afterIsStepReachable(true)

            innerStep.updateJourneyState(formData)
            innerStep.afterUpdateJourneyState()
            val redirect = innerStep.determineRedirect()
            return ModelAndView("redirect:$redirect")
        }
        val unreachableStepRedirect = innerStep.getUnreachableStepRedirect
        return ModelAndView("redirect:$unreachableStepRedirect")
    }
}

abstract class AbstractStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, TState : JourneyState> :
    StepInitialiser<TEnum, TState>,
    UsableStep<TFormModel> {
    open fun beforeIsStepReachable() {}

    open fun afterIsStepReachable(result: Boolean) {}

    open fun beforeGetStepContent() {}

    open fun afterGetStepContent() {}

    open fun beforeGetTemplate() {}

    open fun afterGetTemplate() {}

    open fun beforeValidateSubmittedData(formData: PageData): PageData = formData

    open fun afterValidateSubmittedData(bindingResult: BindingResult) {}

    open fun afterUpdateJourneyState() {}

    lateinit var state: TState
    private lateinit var redirectTo: (mode: TEnum) -> UsableStep<*>?

    @Autowired
    private lateinit var validator: Validator

    final override var isInitialised: Boolean = false
        private set

    fun determineRedirect(): String? = mode(state)?.let { redirectTo(it)?.routeSegment }

    private var backUrlOverride: String? = null

    val backUrl: String?
        get() {
            val parentSteps =
                parentage.parentSteps
                    .mapNotNull { it as? UsableStep<*> }
            return backUrlOverride ?: parentSteps
                .singleOrNull()
                ?.routeSegment
        }
    final override lateinit var routeSegment: String
        private set

    override val formModel: TFormModel?
        get() = getFormModelFromState(state)

    fun getFormModelFromState(state: TState): TFormModel? =
        objectToStringKeyedMap(state.journeyData[routeSegment])?.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))

            formModelClazz.cast(binder.bindingResult.target)
        }

    fun validateSubmittedData(formData: PageData): BindingResult =
        formData.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))
            binder.validate()
            binder.bindingResult
        }

    fun updateJourneyState(formData: PageData) {
        state.addStepData(routeSegment, formData)
    }

    val isStepReachable: Boolean
        get() =
            if (!this::isStepReachableOverride.isInitialized) {
                parentage.allowsChild()
            } else {
                isStepReachableOverride()
            }

    override fun outcome(): TEnum? = if (isStepReachable) mode(state) else null

    public val getUnreachableStepRedirect: String = "task-list"

    private lateinit var isStepReachableOverride: () -> Boolean

    abstract val formModelClazz: KClass<TFormModel>

    abstract fun getStepContent(state: TState): Map<String, Any?>

    abstract fun chooseTemplate(): String

    override fun step(
        segment: String,
        init: StepInitialiser<TEnum, TState>.() -> Unit,
    ): Pair<String, VisitableStep> {
        if (isInitialised) {
            throw IllegalStateException("Step is already initialised")
        }
        routeSegment = segment
        this.init()
        if (!this::parentage.isInitialized) {
            parentage = NoParents()
        }
        isInitialised = true
        return Pair(segment, StepConductor(this))
    }

    override fun reachableWhen(condition: () -> Boolean): StepInitialiser<TEnum, TState> {
        this.isStepReachableOverride = condition
        return this
    }

    override fun state(stateProvider: () -> TState): StepInitialiser<TEnum, TState> {
        state = stateProvider()
        return this
    }

    override fun redirectTo(nextStepProvider: (mode: TEnum) -> UsableStep<*>?): StepInitialiser<TEnum, TState> {
        redirectTo = nextStepProvider
        return this
    }

    override fun backUrl(backUrlProvider: () -> String) {
        backUrlOverride = backUrlProvider()
    }

    override fun parents(currentParentage: () -> Parentage): StepInitialiser<TEnum, TState> {
        parentage = currentParentage()
        return this
    }

    fun getPageVisitContent() =
        getStepContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                "formModel" to (formModel ?: formModelClazz.createInstance()),
            )

    fun getInvalidSubmissionContent(bindingResult: BindingResult) =
        getStepContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult,
            )

    private lateinit var parentage: Parentage

    override val ancestry: List<StepInitialiser<*, *>>
        get() = (listOf(this) + parentage.ancestry).distinct()
}

interface StepInitialiser<out TEnum : Enum<out TEnum>, in TState : JourneyState> {
    val isInitialised: Boolean

    fun step(
        segment: String,
        init: StepInitialiser<TEnum, TState>.() -> Unit,
    ): Pair<String, VisitableStep>

    fun reachableWhen(condition: () -> Boolean): StepInitialiser<TEnum, TState>

    fun parents(currentParentage: () -> Parentage): StepInitialiser<TEnum, TState>

    fun state(stateProvider: () -> TState): StepInitialiser<TEnum, TState>

    fun redirectTo(nextStepProvider: (mode: TEnum) -> UsableStep<*>?): StepInitialiser<TEnum, TState>

    fun outcome(): TEnum?

    fun mode(state: TState): TEnum?

    fun backUrl(backUrlProvider: () -> String)

    val ancestry: List<StepInitialiser<*, *>>
}

interface UsableStep<TFormModel : FormModel> {
    val routeSegment: String
    val formModel: TFormModel?
}

interface VisitableStep {
    fun getStepModelAndView(): ModelAndView

    fun postStepModelAndView(formData: PageData): ModelAndView
}
