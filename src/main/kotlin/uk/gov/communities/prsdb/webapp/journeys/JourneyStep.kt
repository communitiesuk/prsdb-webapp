package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.collections.plus
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

sealed class JourneyStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState>(
    val stepConfig: AbstractStepConfig<TEnum, TFormModel, TState>,
) {
    open class RequestableStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState>(
        stepConfig: AbstractStepConfig<TEnum, TFormModel, TState>,
    ) : JourneyStep<TEnum, TFormModel, TState>(stepConfig) {
        val routeSegment: String get() = stepConfig.routeSegment

        override fun getRouteSegmentOrNull(): String? = stepConfig.routeSegment

        override fun isRouteSegmentInitialised(): Boolean = stepConfig.isRouteSegmentInitialised()
    }

    open class InternalStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState>(
        stepConfig: AbstractStepConfig<TEnum, TFormModel, TState>,
    ) : JourneyStep<TEnum, TFormModel, TState>(stepConfig) {
        override fun getRouteSegmentOrNull(): String? = null

        override fun isRouteSegmentInitialised(): Boolean = true
    }

    abstract fun getRouteSegmentOrNull(): String?

    abstract fun isRouteSegmentInitialised(): Boolean

    // TODO PRSD-1550: Review which lifecycle hooks are needed and update names based on use cases, especially if they have a return value
    fun beforeIsStepReachable() {
        stepConfig.beforeIsStepReachable(state)
    }

    fun afterIsStepReached() {
        stepConfig.afterIsStepReached(state)
    }

    fun beforeValidateSubmittedData(formData: PageData): PageData = stepConfig.beforeValidateSubmittedData(formData, state)

    fun afterValidateSubmittedData(bindingResult: BindingResult) = stepConfig.afterValidateSubmittedData(bindingResult, state)

    fun beforeGetPageVisitContent() {
        stepConfig.beforeGetStepContent(state)
    }

    fun chooseTemplate(): Destination = Destination.Template(stepConfig.chooseTemplate(state))

    fun afterGetPageVisitContent() {
        stepConfig.afterGetStepContent(state)
    }

    fun beforeChooseTemplate() {
        stepConfig.beforeGetTemplate(state)
    }

    fun afterChooseTemplate() {
        stepConfig.afterGetTemplate(state)
    }

    fun beforeSubmitFormData() {
        stepConfig.beforeSubmitFormData(state)
    }

    fun afterSubmitFormData() {
        stepConfig.afterSubmitFormData(state)
    }

    fun beforeDetermineRedirect() {
        stepConfig.beforeDetermineRedirect(state)
    }

    fun afterDetermineRedirect() {
        stepConfig.afterDetermineRedirect(state)
    }

    val isStepReachable: Boolean
        get() = parentage.allowsChild()

    fun validateSubmittedData(formData: PageData): BindingResult =
        formData.let {
            val binder = WebDataBinder(stepConfig.formModelClass.createInstance())
            binder.validator = stepConfig.validator
            binder.bind(MutablePropertyValues(it))
            binder.validate()
            binder.bindingResult
        }

    fun getPageVisitContent() =
        stepConfig.getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                "formModel" to (formModel ?: stepConfig.formModelClass.createInstance()),
            )

    fun getInvalidSubmissionContent(bindingResult: BindingResult) =
        stepConfig.getStepSpecificContent(state) +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult,
            )

    fun submitFormData(bindingResult: BindingResult) {
        getRouteSegmentOrNull()?.let { state.addStepData(it, stepConfig.formModelClass.cast(bindingResult.target).toPageData()) }
    }

    fun determineNextDestination(): Destination =
        stepConfig.mode(state)?.let { nextDestination(it) }
            ?: throw UnrecoverableJourneyStateException(currentJourneyId, "Determining next destination failed - step mode is null")

    private lateinit var unreachableStepDestination: () -> Destination

    fun getUnreachableStepDestination() = unreachableStepDestination()

    val formModel: TFormModel?
        get() = stepConfig.getFormModelFromState(state)

    lateinit var parentage: Parentage

    private lateinit var state: TState

    fun outcome(): TEnum? = if (isStepReachable)stepConfig.mode(state) else null

    private lateinit var nextDestination: (mode: TEnum) -> Destination

    private var backUrlOverride: (() -> String?)? = null

    val backUrl: String?
        get() {
            val singleParentUrl =
                parentage.allowingParentSteps
                    .singleOrNull()
                    ?.getRouteSegmentOrNull()
            val backUrlOverrideValue = this.backUrlOverride?.let { it() }
            return if (backUrlOverride != null) backUrlOverrideValue else singleParentUrl
        }

    val initialisationStage: StepInitialisationStage
        get() =
            when {
                !isBaseClassInitialised -> StepInitialisationStage.UNINITIALISED
                isBaseClassInitialised && !stepConfig.isSubClassInitialised() -> StepInitialisationStage.PARTIALLY_INITIALISED
                else -> StepInitialisationStage.FULLY_INITIALISED
            }

    private val isBaseClassInitialised: Boolean
        get() =
            isRouteSegmentInitialised() && ::state.isInitialized && ::nextDestination.isInitialized && ::parentage.isInitialized

    fun initialize(
        segment: String?,
        state: TState,
        backUrlOverride: (() -> String?)?,
        redirectDestinationProvider: (mode: TEnum) -> Destination,
        parentage: Parentage,
        unreachableStepDestinationProvider: () -> Destination,
    ) {
        if (initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("Step $this has already been initialised")
        }
        segment?.let { this.stepConfig.routeSegment = it }
        this.state = state
        this.backUrlOverride = backUrlOverride
        this.nextDestination = redirectDestinationProvider
        this.parentage = parentage
        this.unreachableStepDestination = unreachableStepDestinationProvider
    }

    val currentJourneyId: String
        get() = state.journeyId
}

enum class StepInitialisationStage {
    UNINITIALISED,
    PARTIALLY_INITIALISED,
    FULLY_INITIALISED,
}
