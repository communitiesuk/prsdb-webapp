package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.AddressDataService
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import kotlin.reflect.KClass

class SelectAddressPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    displaySectionHeader: Boolean = false,
    private val lookupAddressPathSegment: String,
    private val addressLookupService: AddressLookupService,
    private val addressDataService: AddressDataService,
) : Page(formModel, templateName, content, displaySectionHeader) {
    override fun populateModelAndGetTemplateName(
        validator: Validator,
        model: Model,
        pageData: Map<String, Any?>?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): String {
        journeyData!!

        val (houseNameOrNumber, postcode) =
            JourneyDataHelper.getLookupAddressHouseNameOrNumberAndPostcode(
                journeyData,
                lookupAddressPathSegment,
            )!!

        val addressLookupResults = addressLookupService.search(houseNameOrNumber, postcode)
        addressDataService.setAddressData(addressLookupResults)

        var addressRadiosViewModel: List<RadiosViewModel> =
            addressLookupResults.mapIndexed { index, address ->
                RadiosButtonViewModel(
                    value = address.singleLineAddress,
                    valueStr = (index + 1).toString(),
                )
            }
        addressRadiosViewModel = addressRadiosViewModel.toMutableList()
        addressRadiosViewModel.addAll(
            listOf(
                RadiosDividerViewModel("forms.radios.dividerText"),
                RadiosButtonViewModel(MANUAL_ADDRESS_CHOSEN, labelMsgKey = "forms.selectAddress.addAddressManually"),
            ),
        )

        model.addAttribute("houseNameOrNumber", houseNameOrNumber)
        model.addAttribute("postcode", postcode)
        model.addAttribute("addressCount", addressLookupResults.size)
        model.addAttribute("options", addressRadiosViewModel)

        return super.populateModelAndGetTemplateName(validator, model, pageData, prevStepUrl)
    }

    override fun isSatisfied(
        validator: Validator,
        formData: Map<String, Any?>,
    ): Boolean {
        val selectedAddress = formData["address"].toString()
        return selectedAddress == MANUAL_ADDRESS_CHOSEN || addressDataService.getAddressData(selectedAddress) != null
    }
}
