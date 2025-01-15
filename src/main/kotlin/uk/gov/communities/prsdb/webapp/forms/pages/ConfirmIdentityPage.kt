package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.SummaryListRowViewModel
import kotlin.reflect.KClass

class ConfirmIdentityPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
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

        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
