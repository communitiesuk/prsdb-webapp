package uk.gov.communities.prsdb.webapp.theJourneyFramework

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.collections.plus
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

@Suppress("ktlint:standard:max-line-length")
abstract class AbstractStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : DynamicJourneyState, TSelf : AbstractStep<TEnum, TFormModel, TState, TSelf>> {
    // Extra lifecycle methods
    open fun beforeIsStepReachable() {}

    open fun afterIsStepReached() {}

    open fun beforeValidateSubmittedData(formData: PageData): PageData = formData

    open fun afterValidateSubmittedData(bindingResult: BindingResult) {}

    open fun beforeGetStepContent() {}

    open fun afterGetStepContent() {}

    open fun beforeGetTemplate() {}

    open fun afterGetTemplate() {}

    open fun beforeSubmitFormData() {}

    open fun afterSubmitFormData() {}

    open fun beforeDetermineRedirect() {}

    open fun afterDetermineRedirect() {}

    // Main lifecycle methods
    val isStepReachable: Boolean
        get() {
            val override = isStepReachableOverride
            return if (override != null) {
                override()
            } else {
                parentage.allowsChild()
            }
        }

    fun validateSubmittedData(formData: PageData): BindingResult =
        formData.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))
            binder.validate()
            binder.bindingResult
        }

    fun getPageVisitContent() =
        getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                "formModel" to (formModel ?: formModelClazz.createInstance()),
            )

    fun getInvalidSubmissionContent(bindingResult: BindingResult) =
        getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult,
            )

    abstract fun getStepSpecificContent(state: TState): Map<String, Any?>

    abstract fun chooseTemplate(): String

    fun submitFormData(formData: PageData) {
        state.addStepData(routeSegment, formData)
    }

    fun determineRedirect(): String = mode(state)?.let { redirectToUrl(it) } ?: routeSegment

    val getUnreachableStepRedirect: String = "task-list"

    // Validation and the form model
    @Autowired
    private lateinit var validator: Validator
    abstract val formModelClazz: KClass<TFormModel>

    val formModel: TFormModel?
        get() = getFormModelFromState(state)

    private fun getFormModelFromState(state: TState): TFormModel? =
        objectToStringKeyedMap(state.journeyData[routeSegment])?.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))

            formModelClazz.cast(binder.bindingResult.target)
        }

    private lateinit var parentage: Parentage

    val ancestry: List<AbstractStep<*, *, *, *>>
        get() = (listOf(this) + parentage.ancestry).distinct()

    private lateinit var state: TState

    fun outcome(): TEnum? = if (isStepReachable) mode(state) else null

    abstract fun mode(state: TState): TEnum?

    private lateinit var redirectToUrl: (mode: TEnum) -> String

    private var backUrlOverride: (() -> String?)? = null

    val backUrl: String?
        get() {
            val singleParentUrl =
                parentage.parentSteps
                    .singleOrNull()
                    ?.routeSegment
            val backUrlOverrideValue = this.backUrlOverride?.let { it() }
            return if (backUrlOverride != null) backUrlOverrideValue else singleParentUrl
        }

    lateinit var routeSegment: String
        private set

    private var isStepReachableOverride: (() -> Boolean)? = null

    // Initialisation and building
    var initialisationStage: StepInitialisationStage = StepInitialisationStage.UNINITIALISED
        private set

    fun initialize(
        segment: String,
        state: TState,
        backUrlProvider: (() -> String?)?,
        redirectToProvider: (mode: TEnum) -> String,
        parentageProvider: () -> Parentage,
    ) {
        if (initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw Exception("Step $this has already been initialised")
        }
        this.routeSegment = segment
        this.state = state
        this.backUrlOverride = backUrlProvider
        this.redirectToUrl = redirectToProvider
        this.parentage = parentageProvider()
        initialisationStage = StepInitialisationStage.FULLY_INITIALISED
    }
    // End of class
}

enum class StepInitialisationStage {
    UNINITIALISED,
    AWAITING_STATE,
    FULLY_INITIALISED,
}

interface RedirectableLocation {
    val url: String
}
