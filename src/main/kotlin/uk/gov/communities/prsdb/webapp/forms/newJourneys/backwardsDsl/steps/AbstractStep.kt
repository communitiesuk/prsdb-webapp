package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.JourneyState
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.collections.plus
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

abstract class AbstractStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState> :
    LifeCycleStep<TFormModel, TState>(),
    StepInitialiser<TEnum, TState>,
    UsableStep<TFormModel> {
    final override var isInitialised: Boolean = false
        private set

    private final lateinit var state: TState
    private final lateinit var redirectTo: (mode: TEnum) -> UsableStep<*>?

    private fun determineRedirect(): String? = mode(state)?.let { redirectTo(it)?.routeSegment }

    private var backUrlOverride: String? = null

    private val backUrl: String?
        get() {
            val parentSteps =
                parent.parentSteps
                    .mapNotNull { it as? UsableStep<*> }
            return backUrlOverride ?: parentSteps
                .singleOrNull()
                ?.routeSegment
        }
    final override lateinit var routeSegment: String
        private set

    @Autowired
    private final lateinit var validator: Validator

    final override fun getStepModelAndView(): ModelAndView {
        println("Getting step model and view for step $routeSegment")
        if (isStepReachable()) {
            val content =
                getStepContent(state) +
                    mapOf(BACK_URL_ATTR_NAME to backUrl, "formModel" to (getFormModelFromState(state) ?: formModelClazz.createInstance()))
            val template = chooseTemplate(state)
            return ModelAndView(template, content)
        }

        val unreachableStepRedirect = getUnreachableStepRedirect()
        return ModelAndView("redirect:$unreachableStepRedirect")
    }

    final override fun postStepModelAndView(formData: PageData): ModelAndView {
        println("Posting step model and view for step $routeSegment with form data $formData")
        if (isStepReachable()) {
            val newFormData = beforeValidateSubmittedData(state, formData)
            val bindingResult = validateSubmittedData(newFormData)
            if (bindingResult.hasErrors()) {
                val content =
                    getStepContent(state) +
                        mapOf(BACK_URL_ATTR_NAME to backUrl, BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult)
                val template = chooseTemplate(state)

                return ModelAndView(template, content)
            }

            updateJourneyState(formData)
            afterUpdateJourneyState(state)
            val redirect = determineRedirect()
            return ModelAndView("redirect:$redirect")
        }
        val unreachableStepRedirect = getUnreachableStepRedirect()
        return ModelAndView("redirect:$unreachableStepRedirect")
    }

    override val formModel: TFormModel?
        get() = getFormModelFromState(state)

    fun getFormModelFromState(state: TState): TFormModel? =
        objectToStringKeyedMap(state.journeyData[routeSegment])?.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))

            formModelClazz.cast(binder.bindingResult.target)
        }

    private fun validateSubmittedData(formData: PageData): BindingResult =
        formData.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))
            binder.validate()
            binder.bindingResult
        }

    private fun updateJourneyState(formData: PageData) {
        state.addStepData(routeSegment, formData)
    }

    override fun outcome(): TEnum? = if (isStepReachable()) mode(state) else null

    private fun getUnreachableStepRedirect(): String = "task-list"

    protected lateinit var isStepReachable: () -> Boolean
        private set

    abstract val formModelClazz: KClass<TFormModel>

    abstract fun getStepContent(state: TState): Map<String, Any?>

    abstract fun chooseTemplate(state: TState): String

    override fun step(
        segment: String,
        init: StepInitialiser<TEnum, TState>.() -> Unit,
    ): Pair<String, UsableStep<TFormModel>> {
        if (isInitialised) {
            throw IllegalStateException("Step is already initialised")
        }
        routeSegment = segment
        this.init()
        if (!this::parent.isInitialized) {
            parent = NoParents()
        }
        isInitialised = true
        return Pair(segment, this)
    }

    override fun reachableWhen(condition: () -> Boolean): StepInitialiser<TEnum, TState> {
        this.isStepReachable = condition
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

    override fun parents(currentParents: () -> StepParent): StepInitialiser<TEnum, TState> {
        if (this::isStepReachable.isInitialized) {
            throw IllegalStateException("Parents must be set before reachableWhen override")
        }

        parent = currentParents()
        isStepReachable = { parent.allowsChild() }
        return this
    }

    private lateinit var parent: StepParent

    override val ancestry: List<StepInitialiser<*, *>>
        get() = (listOf(this) + parent.ancestry).distinct()
}

abstract class LifeCycleStep<TFormModel : FormModel, in TState : JourneyState> {
    open fun beforeValidateSubmittedData(
        state: TState,
        formData: PageData,
    ): PageData = formData

    open fun afterUpdateJourneyState(state: TState) {}
}

interface StepInitialiser<out TEnum : Enum<out TEnum>, in TState : JourneyState> {
    val isInitialised: Boolean

    fun step(
        segment: String,
        init: StepInitialiser<TEnum, TState>.() -> Unit,
    ): Pair<String, UsableStep<*>>

    fun reachableWhen(condition: () -> Boolean): StepInitialiser<TEnum, TState>

    fun parents(currentParents: () -> StepParent): StepInitialiser<TEnum, TState>

    fun state(stateProvider: () -> TState): StepInitialiser<TEnum, TState>

    fun redirectTo(nextStepProvider: (mode: TEnum) -> UsableStep<*>?): StepInitialiser<TEnum, TState>

    fun outcome(): TEnum?

    fun mode(state: TState): TEnum?

    fun backUrl(backUrlProvider: () -> String)

    val ancestry: List<StepInitialiser<*, *>>
}

interface UsableStep<TFormModel : FormModel> : VisitableStep {
    val routeSegment: String
    val formModel: TFormModel?
}

interface VisitableStep {
    fun getStepModelAndView(): ModelAndView

    fun postStepModelAndView(formData: PageData): ModelAndView
}
