package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LandlordDeregistrationAreYouSurePage(
    commonContent: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
    contentProvider: () -> Map<String, Any>,
) : PageWithContentProvider(
        LandlordDeregistrationAreYouSureFormModel::class,
        "forms/areYouSureForm",
        commonContent,
        shouldDisplaySectionHeader = false,
        contentProvider,
    ) {
    override fun bindDataToFormModel(
        validator: Validator,
        formData: PageData?,
    ): BindingResult {
        val newFormData = formData?.toMutableMap()
        if (newFormData != null) {
            val journeyData = journeyDataService.getJourneyDataFromSession()
            val landlordHasRegisteredProperties = journeyData.getLandlordUserHasRegisteredProperties()
            newFormData["userHasRegisteredProperties"] = landlordHasRegisteredProperties
        }

        return super.bindDataToFormModel(validator, newFormData)
    }
}
