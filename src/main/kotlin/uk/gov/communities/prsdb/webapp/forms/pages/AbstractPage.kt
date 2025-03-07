package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class AbstractPage(
    private val formModel: KClass<out FormModel>,
    private val templateName: String,
    private val content: Map<String, Any>,
    val shouldDisplaySectionHeader: Boolean = false,
) {
    fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        filteredJourneyData: JourneyData?,
        sectionHeaderInfo: SectionHeaderViewModel?,
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

        enrichModel(modelAndView, filteredJourneyData)

        return addSectionHeaderInfoToModel(modelAndView, sectionHeaderInfo)
    }

    abstract fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    )

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
