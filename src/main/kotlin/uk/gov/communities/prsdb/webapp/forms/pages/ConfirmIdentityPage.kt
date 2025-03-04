package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.reflect.KClass

class ConfirmIdentityPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    displaySectionHeader: Boolean = false,
) : Page(formModel, templateName, content, displaySectionHeader) {
    override fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): ModelAndView {
        journeyData!!

        val formData =
            mutableListOf(
                SummaryListRowViewModel(
                    "forms.confirmDetails.rowHeading.name",
                    LandlordRegistrationJourneyDataHelper.getVerifiedName(journeyData)!!,
                    null,
                ),
                SummaryListRowViewModel(
                    "forms.confirmDetails.rowHeading.dob",
                    LandlordRegistrationJourneyDataHelper.getVerifiedDOB(journeyData)!!,
                    null,
                ),
            )
        val modelAndView = super.getModelAndView(validator, pageData, prevStepUrl)
        modelAndView.addObject("formData", formData)

        return modelAndView
    }
}
