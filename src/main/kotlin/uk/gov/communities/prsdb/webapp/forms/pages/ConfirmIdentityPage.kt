package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.LandlordRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.reflect.KClass

class ConfirmIdentityPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    displaySectionHeader: Boolean = false,
) : AbstractPage(formModel, templateName, content, displaySectionHeader) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!

        val formData =
            mutableListOf(
                SummaryListRowViewModel(
                    "forms.confirmDetails.rowHeading.name",
                    LandlordRegistrationJourneyDataHelper.getVerifiedName(filteredJourneyData)!!,
                    null,
                ),
                SummaryListRowViewModel(
                    "forms.confirmDetails.rowHeading.dob",
                    LandlordRegistrationJourneyDataHelper.getVerifiedDOB(filteredJourneyData)!!,
                    null,
                ),
            )

        modelAndView.addObject("formData", formData)
    }
}
