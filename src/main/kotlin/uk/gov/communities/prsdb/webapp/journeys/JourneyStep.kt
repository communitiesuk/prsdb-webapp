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

        override fun initialiseRouteSegment(routeSegment: String?) {
            if (isRouteSegmentInitialised()) {
                throw JourneyInitialisationException("routeSegment is already initialised")
            }
            if (routeSegment == null) {
                throw JourneyInitialisationException("routeSegment cannot be null for a requestable step")
            }

            stepConfig.routeSegment = routeSegment
        }
    }

    open class InternalStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState>(
        stepConfig: AbstractStepConfig<TEnum, TFormModel, TState>,
    ) : JourneyStep<TEnum, TFormModel, TState>(stepConfig) {
        override fun getRouteSegmentOrNull(): String? = null

        override fun isRouteSegmentInitialised(): Boolean = true

        override fun initialiseRouteSegment(routeSegment: String?) {
            routeSegment?.let {
                throw JourneyInitialisationException(
                    "route segment cannot be set for an internal step - was set to $routeSegment",
                )
            }
        }
    }

    abstract fun getRouteSegmentOrNull(): String?

    abstract fun isRouteSegmentInitialised(): Boolean

    abstract fun initialiseRouteSegment(routeSegment: String?)

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

    fun beforeDetermineNextDestination() {
        stepConfig.beforeDetermineNextDestination(state)
    }

    fun afterDetermineNextDestination(destination: Destination) = stepConfig.afterDetermineNextDestination(state, destination)

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
            additionalContentProvider() +
            mapOf(
                BACK_URL_ATTR_NAME to backUrl,
                "formModel" to (formModelOrNull ?: stepConfig.formModelClass.createInstance()),
            )

    fun getInvalidSubmissionContent(bindingResult: BindingResult) =
        stepConfig.getStepSpecificContent(state) +
            additionalContentProvider() +
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

    val formModelOrNull: TFormModel?
        get() = stepConfig.getFormModelFromStateOrNull(state)

    val formModel: TFormModel
        get() = stepConfig.getFormModelFromState(state)

    lateinit var parentage: Parentage

    private lateinit var state: TState

    val outcome: TEnum? get() = if (isStepReachable)stepConfig.mode(state) else null

    private lateinit var nextDestination: (mode: TEnum) -> Destination

    private var backUrlOverride: (() -> Destination)? = null

    private var additionalContentProvider: () -> Map<String, Any> = { mapOf() }

    val backUrl: String?
        get() {
            val singleParentUrl =
                when (val singleParentStep = parentage.allowingParentSteps.singleOrNull()) {
                    is InternalStep<*, *, *> -> singleParentStep.backUrl
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
