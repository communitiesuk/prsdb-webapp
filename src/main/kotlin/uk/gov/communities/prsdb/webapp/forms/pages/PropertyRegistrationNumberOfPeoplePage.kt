package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.helpers.PropertyRegistrationJourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class PropertyRegistrationNumberOfPeoplePage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    shouldDisplaySectionHeader: Boolean = false,
    private val journeyDataService: JourneyDataService,
) : Page(formModel, templateName, content, shouldDisplaySectionHeader) {
    override fun bindDataToFormModel(
        validator: Validator,
        formData: PageData?,
    ): BindingResult {
        val newFormData = formData?.toMutableMap()
        if (newFormData != null) {
            val numberOfHouseholds = getNumberOfHouseholds()
            newFormData["numberOfHouseholds"] = numberOfHouseholds
        }
        return super.bindDataToFormModel(validator, newFormData)
    }

    private fun getNumberOfHouseholds(): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val numberOfHouseholds = PropertyRegistrationJourneyDataHelper.getNumberOfHouseholds(journeyData)
        return numberOfHouseholds.toString()
    }
}
