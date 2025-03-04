package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.servlet.ModelAndView
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
    open fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
    ): ModelAndView {
        val modelAndView = ModelAndView(templateName)

        val bindingResult = bindDataToFormModel(validator, pageData)
        modelAndView.addObject(BindingResult.MODEL_KEY_PREFIX + "formModel", bindingResult)

        if (prevStepUrl != null) {
            modelAndView.addObject(BACK_URL_ATTR_NAME, prevStepUrl)
        }

        for ((key, value) in content) {
            modelAndView.addObject(key, value)
        }
        return modelAndView
    }

    open fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ) = getModelAndView(validator, pageData, prevStepUrl)

    open fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
        sectionHeaderInfo: SectionHeaderViewModel?,
    ): ModelAndView {
        val modelAndView = getModelAndView(validator, pageData, prevStepUrl, journeyData)
        return addSectionHeaderInfoToModel(modelAndView, sectionHeaderInfo)
    }

    private fun addSectionHeaderInfoToModel(
        modelAndView: ModelAndView,
        sectionHeaderInfo: SectionHeaderViewModel?,
    ): ModelAndView {
        if (shouldDisplaySectionHeader) {
            if (sectionHeaderInfo == null) {
                throw PrsdbWebException("Section heading requested but heading message key not found")
            }
            modelAndView.addObject("sectionHeaderInfo", sectionHeaderInfo)
        }
        return modelAndView
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
