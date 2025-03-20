package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.DeregistrationJourneyDataExtensions.Companion.getLandlordUserHasRegisteredProperties
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import kotlin.reflect.KClass

class LandlordDeregistrationAreYouSurePage(
    commonContent: Map<String, Any>,
    private val formModelProvider: () -> KClass<out FormModel>,
) : AbstractPage(
        formModelProvider(),
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

        if (userHasRegisteredProperties) {
            modelAndView.addObject("fieldSetHeading", "forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading")
            modelAndView.addObject("fieldSetHint", null)
        } else {
            modelAndView.addObject("fieldSetHeading", "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHeading")
            modelAndView.addObject("fieldSetHint", "forms.areYouSure.landlordDeregistration.hasProperties.fieldSetHint")
        }
    }
}
