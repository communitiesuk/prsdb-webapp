package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class SelectAddressPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    private val urlPathSegment: String,
    private val journeyDataService: JourneyDataService,
    private val addressLookupService: AddressLookupService,
) : Page(formModel, templateName, content) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
    ): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val houseNameOrNumber =
            objectToStringKeyedMap(journeyData[urlPathSegment])?.get("houseNameOrNumber").toString()
        val postcode = objectToStringKeyedMap(journeyData[urlPathSegment])?.get("postcode").toString()

        val addressLookupResults = addressLookupService.search(houseNameOrNumber, postcode)
        model.addAttribute("addressCount", addressLookupResults.size)
        model.addAttribute("postcode", postcode)
        model.addAttribute("houseNameOrNumber", houseNameOrNumber)
        model.addAttribute(
            "options",
            addressLookupResults.mapIndexed { index, address ->
                RadiosViewModel(value = address.address, valueStr = (index + 1).toString())
            },
        )

        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}