package uk.gov.communities.prsdb.webapp.journeys

import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createInstance

abstract class AbstractInnerStep<out TEnum : Enum<out TEnum>, TFormModel : FormModel, in TState : DynamicJourneyState> {
    abstract fun getStepSpecificContent(state: TState): Map<String, Any?>

    abstract fun chooseTemplate(state: TState): String

    abstract val formModelClazz: KClass<TFormModel>

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

    open fun beforeDetermineRedirect(state: TState) {}

    open fun afterDetermineRedirect(state: TState) {}

    abstract fun isSubClassInitialised(): Boolean

    abstract fun mode(state: TState): TEnum?

    fun getFormModelFromState(state: TState): TFormModel? =
        state.getStepData(routeSegment)?.let {
            val binder = WebDataBinder(formModelClazz.createInstance())
            binder.validator = validator
            binder.bind(MutablePropertyValues(it))

            formModelClazz.cast(binder.bindingResult.target)
        }

    lateinit var routeSegment: String

    fun isRouteSegmentInitialised(): Boolean = ::routeSegment.isInitialized

    @Autowired
    lateinit var validator: Validator
}

abstract class AbstractUninitialisableInnerStep<TEnum : Enum<TEnum>, TModel : FormModel, TState : DynamicJourneyState> :
    AbstractInnerStep<TEnum, TModel, TState>() {
    override fun isSubClassInitialised(): Boolean = true
}
