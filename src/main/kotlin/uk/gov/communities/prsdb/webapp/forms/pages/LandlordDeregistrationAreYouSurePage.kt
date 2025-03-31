package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.LandlordDeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LandlordDeregistrationAreYouSureFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class LandlordDeregistrationAreYouSurePage(
    commonContent: Map<String, Any>,
    private val journeyDataService: JourneyDataService,
) : AbstractPage(
        LandlordDeregistrationAreYouSureFormModel::class,
        "forms/areYouSureForm",
        commonContent,
        shouldDisplaySectionHeader = false,
    ) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        val userHasRegisteredProperties =
            filteredJourneyData?.getLandlordUserHasRegisteredProperties()
                ?: throw (
                    ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "userHasRegisteredProperties was not found in journeyData",
                    )
                )

        if (!userHasRegisteredProperties) {
            modelAndView.addObject("fieldSetHeading", "forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading")
        } else {
            modelAndView.addObject("fieldSetHeading", "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHeading")
            modelAndView.addObject("fieldSetHint", "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHint")
        }
    }

    override fun enrichFormData(formData: PageData?): PageData? {
        if (formData == null) {
            return null
        }
        val newFormData = formData.toMutableMap()
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val landlordHasRegisteredProperties = journeyData.getLandlordUserHasRegisteredProperties()
        newFormData["userHasRegisteredProperties"] = landlordHasRegisteredProperties
        return newFormData
    }
}
