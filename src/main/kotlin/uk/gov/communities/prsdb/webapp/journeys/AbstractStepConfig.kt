package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

abstract class AbstractStepConfig<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : JourneyState> {
    abstract fun getStepSpecificContent(state: TState): Map<String, Any?>

    abstract fun chooseTemplate(state: TState): String

    abstract val formModelClass: KClass<TFormModel>

    open fun beforeAttemptingToReachStep(state: TState) {}

    open fun afterStepIsReached(state: TState) {}

    open fun whenStepIsUnreachable(state: TState) {}

    open fun enrichSubmittedDataBeforeValidation(
        state: TState,
        formData: PageData,
    ): PageData = formData

    open fun afterPrimaryValidation(
        state: TState,
        bindingResult: BindingResult,
    ) {}

    open fun resolvePageContent(
        state: TState,
        defaultContent: Map<String, Any?>,
    ): Map<String, Any?> = defaultContent

    open fun resolveChosenTemplate(
        state: TState,
        templateName: String,
    ): Destination = Destination.Template(templateName)

    open fun beforeStepDataIsAdded(
        state: TState,
        data: PageData,
    ) {}

    open fun afterStepDataIsAdded(state: TState) {}

    open fun beforeSaveState(state: TState) {}

    open fun saveState(state: TState) = state.save()

    open fun afterSaveState(
        state: TState,
        saveStateId: SavedJourneyState,
    ) {}

    open fun beforeChoosingNextDestination(state: TState) {}

    open fun resolveNextDestination(
        state: TState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    open fun beforeChosingUnreachableStepDestination(state: TState) {}

    open fun resolveUnreachableStepDestination(
        state: TState,
        defaultDestination: Destination,
    ): Destination = defaultDestination

    abstract fun isSubClassInitialised(): Boolean

    abstract fun mode(state: TState): TEnum?

    fun getFormModelFromStateOrNull(state: TState): TFormModel? =
        state.getStepData(routeSegment)?.let {
            val binder = WebDataBinder(formModelClass.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))

            formModelClass.cast(binder.bindingResult.target)
        }

    fun getFormModelFromState(state: TState): TFormModel =
        getFormModelFromStateOrNull(state)
            ?: throw NotNullFormModelValueIsNullException("Form model for step '$routeSegment' is null in journey state")

    // TODO PRSD-1550: It is ugly that step config has a value set during JourneyStep initialisation - it is only used to make "getFormModelFromState" work
    // Perhaps either the routeSegment or formModel should be passed into that method instead (and therefore all the other functions)
    // Alternatively, steps could reflexively access the form model on the JourneyStep in state without needing the route segment
    // Another idea would to have this be set directly in the DSL (but enforce that it is set before the other values)
    lateinit var routeSegment: String

    fun isRouteSegmentInitialised(): Boolean = ::routeSegment.isInitialized

    @Autowired
    lateinit var validator: Validator

    protected fun BindingResult.getFormModel() = formModelClass.cast(target)

    protected fun BindingResult.rejectValueWithMessageKey(
        fieldName: String,
        messageKey: String,
    ) = rejectValue(fieldName, "RejectValueWithMessageKey", messageKey)
}

// Generic step config should be used where the subclass does not need any additional initialisation
abstract class AbstractGenericStepConfig<TEnum : Enum<TEnum>, TModel : FormModel, TState : JourneyState> :
    AbstractStepConfig<TEnum, TModel, TState>() {
    override fun isSubClassInitialised(): Boolean = true
}
