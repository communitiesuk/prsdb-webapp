package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper.Companion.getFieldIntegerValue
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class PropertyRegistrationNumberOfPeoplePage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    shouldDisplaySectionHeader: Boolean = false,
    private val journeyDataService: JourneyDataService,
) : AbstractPage(formModel, templateName, content, shouldDisplaySectionHeader) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {}

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
        val journeyDataValue = getNumberOfHouseholdsOrNull(journeyData)
        val originalJourneyDataValue = getNumberOfHouseholdsFromOriginalJourneyDataIfPresent(journeyData)
        val numberOfHouseholds = getLatestValue(journeyDataValue, originalJourneyDataValue)
        return numberOfHouseholds.toString()
    }

    private fun getLatestValue(
        journeyDataValue: Int?,
        originalJourneyDataValue: Int?,
    ): Int {
        if (originalJourneyDataValue != null && journeyDataValue == null) {
            return originalJourneyDataValue
        }
        return journeyDataValue ?: 0
    }

    private fun getNumberOfHouseholdsFromOriginalJourneyDataIfPresent(journeyData: JourneyData): Int? {
        val originalJourneyData = journeyDataService.getOriginalJourneyDataOrNull(journeyData) as JourneyData? ?: return null
        return getNumberOfHouseholdsOrNull(originalJourneyData)
    }

    private fun getNumberOfHouseholdsOrNull(journeyData: JourneyData): Int? =
        getFieldIntegerValue(
            journeyData,
            RegisterPropertyStepId.NumberOfHouseholds.urlPathSegment,
            "numberOfHouseholds",
        )
}
