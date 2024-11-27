package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.models.dataModels.FormSummaryDataModel
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.formModels.VerifiedIdentityModel
import kotlin.reflect.KClass

class ConfirmIdentityPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val getIdentityData: () -> PageData?,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String {
        val identityData = getIdentityData()
        val formData =
            mutableListOf(
                FormSummaryDataModel(
                    "forms.confirmDetails.rowHeading.name",
                    identityData?.get(VerifiedIdentityModel.NAME_KEY),
                    null,
                ),
                FormSummaryDataModel(
                    "forms.confirmDetails.rowHeading.dob",
                    identityData?.get(VerifiedIdentityModel.BIRTH_DATE_KEY),
                    null,
                ),
            )

        model.addAttribute("formData", formData)
        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
