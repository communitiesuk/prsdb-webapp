package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UploadCertificateFormModel
import kotlin.reflect.KClass

class FileUploadPage(
    formModel: KClass<out UploadCertificateFormModel>,
    content: Map<String, Any>,
    shouldDisplaySectionHeader: Boolean = false,
) : AbstractPage(formModel, "forms/uploadCertificateForm", content, shouldDisplaySectionHeader) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {}

    override fun isSatisfied(
        filteredJourneyData: JourneyData,
        bindingResult: BindingResult,
    ): Boolean {
        val isMetadataSatisfied = super.isSatisfied(filteredJourneyData, bindingResult)
        val uploadFormModel = bindingResult.target as UploadCertificateFormModel

        if (isMetadataSatisfied && uploadFormModel.isMetadataOnly) {
            throw IllegalStateException("Metadata only file submissions are only allowed for invalid files to elicit validation errors")
        }

        return isMetadataSatisfied
    }
}
