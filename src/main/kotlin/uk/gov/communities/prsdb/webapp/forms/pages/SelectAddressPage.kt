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
        val buildingNameOrNumber =
            objectToStringKeyedMap(journeyData["lookup-address"])?.get("buildingNameOrNumber").toString()
        val postcode = objectToStringKeyedMap(journeyData["lookup-address"])?.get("postcode").toString()

        val addressLookupResults = addressLookupService.search(buildingNameOrNumber, postcode)
        model.addAttribute("options", addressLookupResults.map { RadiosViewModel(it.address) })

        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }
}
