package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
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
    override fun getModelAndView(
        validator: Validator,
        pageData: PageData?,
        prevStepUrl: String?,
        journeyData: JourneyData?,
    ): ModelAndView {
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

        val modelAndView = super.getModelAndView(validator, pageData, prevStepUrl)
        modelAndView.addObject("houseNameOrNumber", houseNameOrNumber)
        modelAndView.addObject("postcode", postcode)
        modelAndView.addObject("addressCount", addressLookupResults.size)
        modelAndView.addObject("options", addressRadiosViewModel)

        return modelAndView
    }

    override fun isSatisfied(
        validator: Validator,
        formData: PageData,
    ): Boolean {
        val selectedAddress = formData["address"].toString()
        return selectedAddress == MANUAL_ADDRESS_CHOSEN || addressDataService.getAddressData(selectedAddress) != null
    }
}
