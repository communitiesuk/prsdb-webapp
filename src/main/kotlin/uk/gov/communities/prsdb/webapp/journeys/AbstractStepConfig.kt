package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
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

    open fun beforeIsStepReachable(state: TState) {}

    open fun afterIsStepReached(state: TState) {}

    open fun beforeValidateSubmittedData(
        formData: PageData,
        state: TState,
    ): PageData = formData

    open fun afterValidateSubmittedData(
        bindingResult: BindingResult,
        state: TState,
    ) {}

    open fun beforeGetStepContent(state: TState) {}

    open fun afterGetStepContent(state: TState) {}

    open fun beforeGetTemplate(state: TState) {}

    open fun afterGetTemplate(state: TState) {}

    open fun beforeSubmitFormData(state: TState) {}

    open fun afterSubmitFormData(state: TState) {}

    open fun beforeDetermineNextDestination(state: TState) {}

    open fun afterDetermineNextDestination(
        state: TState,
        destination: Destination,
    ): Destination = destination

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
}

// Generic step config should be used where the subclass does not need any additional initialisation
abstract class AbstractGenericStepConfig<TEnum : Enum<TEnum>, TModel : FormModel, TState : JourneyState> :
    AbstractStepConfig<TEnum, TModel, TState>() {
    override fun isSubClassInitialised(): Boolean = true
}
