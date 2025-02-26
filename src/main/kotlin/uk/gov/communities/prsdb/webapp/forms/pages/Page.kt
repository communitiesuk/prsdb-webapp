package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.beans.MutablePropertyValues
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class Page(
    private val formModel: KClass<out FormModel>,
    private val templateName: String,
    private val content: Map<String, Any>,
    val shouldDisplaySectionHeader: Boolean = false,
) {
    open fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: PageData?,
        prevStepUrl: String?,
    ): String {
        var bindingResult = bindDataToFormModel(validator, pageData)
        model.addAttribute(BindingResult.MODEL_KEY_PREFIX + "formModel", bindingResult)

        if (prevStepUrl != null) {
            model.addAttribute(BACK_URL_ATTR_NAME, prevStepUrl)
        }

        for ((key, value) in content) {
            model.addAttribute(key, value)
        }
        return templateName
    }

    open fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String = populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)

    open fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
        sectionHeaderInfo: SectionHeaderViewModel?,
    ): String {
        addSectionHeaderInfoToModel(model, sectionHeaderInfo)
        return populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl, journeyData)
    }

    private fun addSectionHeaderInfoToModel(
        model: Model,
        sectionHeaderInfo: SectionHeaderViewModel?,
    ) {
        if (shouldDisplaySectionHeader) {
            if (sectionHeaderInfo == null) {
                throw PrsdbWebException("Section heading requested but heading message key not found")
            }
            model.addAttribute("sectionHeaderInfo", sectionHeaderInfo)
        }
    }

    open fun isSatisfied(
        validator: Validator,
        formData: PageData,
    ): Boolean {
        val bindingResult = bindDataToFormModel(validator, formData)
        return !bindingResult.hasErrors()
    }

    protected fun bindDataToFormModel(
        validator: Validator,
        formData: PageData?,
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
