package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.forms.JourneyData
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
        // TODO: PRSD-703 - check in the journey data to see if the landlord has properties and add these values if they do
        modelAndView.addObject("fieldSetHeading", "forms.areYouSure.landlordDeregistration.noProperties.fieldSetHeading")
        modelAndView.addObject("fieldSetHint", "")

        // TODO: PRSD-705 - add content keys for "landlord with properties" version
    }
}
