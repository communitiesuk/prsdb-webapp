package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.beans.MutablePropertyValues
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class Page(
    private val formModel: KClass<out FormModel>,
    private val templateName: String,
    private val content: Map<String, Any>,
) {
    fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String {
        var bindingResult = bindDataToFormModel(validator, pageData)
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "formModel", bindingResult)
        for ((key, value) in content) {
            model.addAttribute(key, value)
        }
        if (prevStepUrl != null) {
            model.addAttribute(BACK_URL_ATTR_NAME, prevStepUrl)
        }
        return templateName
    }

    fun isSatisfied(
        validator: Validator,
        formData: Map<String, Any?>,
    ): Boolean {
        val bindingResult = bindDataToFormModel(validator, formData)
        return !bindingResult.hasErrors()
    }

    private fun bindDataToFormModel(
        validator: Validator,
        formData: Map<String, Any?>?,
    ): BindingResult {
        val binder = WebDataBinder(formModel.createInstance())
        binder.validator = validator
        binder.bind(MutablePropertyValues(formData))
        if (formData != null) {
            binder.validate()
        }
        return binder.bindingResult
    }
}
