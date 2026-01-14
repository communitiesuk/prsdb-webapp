package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.BindingResult
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
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

        override fun initialiseRouteSegment(routeSegment: String?) {
            if (isRouteSegmentInitialised()) {
                throw JourneyInitialisationException("routeSegment is already initialised")
            }
            if (routeSegment == null) {
                throw JourneyInitialisationException("routeSegment cannot be null for a requestable step")
            }

            stepConfig.routeSegment = routeSegment
        }

        override fun submitFormData(bindingResult: BindingResult) =
            addStepData(routeSegment, stepConfig.formModelClass.cast(bindingResult.target).toPageData())
    }

    open class InternalStep<out TEnum : Enum<out TEnum>, in TState : JourneyState>(
        stepConfig: AbstractInternalStepConfig<TEnum, TState>,
    ) : JourneyStep<TEnum, NoInputFormModel, TState>(stepConfig) {
        override fun getRouteSegmentOrNull(): String? = null

        override fun isRouteSegmentInitialised(): Boolean = true

        override fun initialiseRouteSegment(routeSegment: String?) {
            routeSegment?.let {
                throw JourneyInitialisationException(
                    "route segment cannot be set for an internal step - was set to $routeSegment",
                )
            }
        }

        override fun submitFormData(bindingResult: BindingResult) {}
    }

    abstract fun getRouteSegmentOrNull(): String?

    abstract fun isRouteSegmentInitialised(): Boolean

    protected abstract fun initialiseRouteSegment(routeSegment: String?)

    abstract fun submitFormData(bindingResult: BindingResult)

    fun attemptToReachStep(): Boolean {
        stepConfig.beforeAttemptingToReachStep(state)
        if (isStepReachable) {
            stepConfig.afterStepIsReached(state)
            return true
        }
        stepConfig.whenStepIsUnreachable(state)
        return false
    }

    fun getPageVisitContent(): Map<String, Any?> {
        val contentWithFormModel =
            stepConfig.getStepSpecificContent(state) +
                additionalContentProvider() +
                mapOf(
                    BACK_URL_ATTR_NAME to backUrl,
                    "formModel" to (formModelOrNull ?: stepConfig.formModelClass.createInstance()),
                )
        return stepConfig.resolvePageContent(state, contentWithFormModel)
    }

    fun chooseTemplate(): Destination {
        val templateName = stepConfig.chooseTemplate(state)
        return stepConfig.resolveChosenTemplate(state, templateName)
    }

    fun validateSubmittedData(submittedData: PageData): BindingResult {
        val enrichedFormData = stepConfig.enrichSubmittedDataBeforeValidation(state, submittedData)

        val binder = WebDataBinder(stepConfig.formModelClass.createInstance())
        binder.validator = stepConfig.validator
        binder.bind(MutablePropertyValues(enrichedFormData))
        binder.validate()

        stepConfig.afterPrimaryValidation(state, binder.bindingResult)
        return binder.bindingResult
    }

    protected fun addStepData(
        routeSegment: String,
        data: PageData,
    ) {
        stepConfig.beforeStepDataIsAdded(state, data)
        state.addStepData(routeSegment, data)
        stepConfig.afterStepDataIsAdded(state)
    }

    fun saveStateIfAllowed() {
        if (shouldSaveOnCompletion) {
            stepConfig.beforeSaveState(state)
            val savedState = stepConfig.saveState(state)
            stepConfig.afterSaveState(state, savedState)
        }
    }

    fun getNextDestination(): Destination {
        stepConfig.beforeChoosingNextDestination(state)
        val defaultDestination =
            stepConfig.mode(state)?.let { nextDestination(it) }
                ?: throw UnrecoverableJourneyStateException(currentJourneyId, "Determining next destination failed - step mode is null")
        return stepConfig.resolveNextDestination(state, defaultDestination)
    }

    fun getInvalidSubmissionContent(bindingResult: BindingResult): Map<String, Any?> {
        val contentWithBindingResult =
            stepConfig.getStepSpecificContent(state) +
                additionalContentProvider() +
                mapOf(
                    BACK_URL_ATTR_NAME to backUrl,
                    BindingResult.MODEL_KEY_PREFIX + "formModel" to bindingResult,
                )
        return stepConfig.resolvePageContent(state, contentWithBindingResult)
    }

    fun getUnreachableStepDestination(): Destination {
        stepConfig.beforeChosingUnreachableStepDestination(state)
        val defaultDestination = unreachableStepDestination()
        return stepConfig.resolveUnreachableStepDestination(state, defaultDestination)
    }

    private lateinit var unreachableStepDestination: () -> Destination

    private var shouldSaveOnCompletion: Boolean = false

    val isStepReachable: Boolean
        get() = parentage.allowsChild()

    val formModelOrNull: TFormModel?
        get() = stepConfig.getFormModelFromStateOrNull(state)

    val formModel: TFormModel
        get() = stepConfig.getFormModelFromState(state)

    lateinit var parentage: Parentage

    private lateinit var state: TState

    val outcome: TEnum? get() = if (isStepReachable) stepConfig.mode(state) else null

    private lateinit var nextDestination: (mode: TEnum) -> Destination

    private var backUrlOverride: (() -> Destination)? = null

    private var additionalContentProvider: () -> Map<String, Any> = { mapOf() }

    val backUrl: String?
        get() {
            val singleParentUrl =
                when (val singleParentStep = parentage.allowingParentSteps.singleOrNull()) {
                    is InternalStep<*, *> -> singleParentStep.backUrl
                    is RequestableStep<*, *, *> -> Destination(singleParentStep).toUrlStringOrNull()
                    null -> null
                }
            val backUrlOverrideValue = this.backUrlOverride?.let { it().toUrlStringOrNull() }
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
        backDestinationOverride: (() -> Destination)?,
        redirectDestinationProvider: (mode: TEnum) -> Destination,
        parentage: Parentage,
        unreachableStepDestinationProvider: () -> Destination,
        shouldSaveOnCompletion: Boolean,
        additionalContentProvider: (() -> Map<String, Any>)? = null,
    ) {
        if (initialisationStage != StepInitialisationStage.UNINITIALISED) {
            throw JourneyInitialisationException("Step $this has already been initialised")
        }
        initialiseRouteSegment(segment)
        this.state = state
        this.backUrlOverride = backDestinationOverride
        this.nextDestination = redirectDestinationProvider
        this.parentage = parentage
        this.unreachableStepDestination = unreachableStepDestinationProvider
        this.shouldSaveOnCompletion = shouldSaveOnCompletion
        additionalContentProvider?.let { this.additionalContentProvider = it }
    }

    val currentJourneyId: String
        get() = state.journeyId
}

enum class StepInitialisationStage {
    UNINITIALISED,
    PARTIALLY_INITIALISED,
    FULLY_INITIALISED,
}
