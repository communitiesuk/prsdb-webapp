package uk.gov.communities.prsdb.webapp.forms.pages

import org.springframework.validation.BindingResult
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.JourneyDataExtensions.Companion.getLookedUpAddress
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyDataExtensions.JourneyDataExtensions.Companion.withUpdatedLookedUpAddresses
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.AddressLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.reflect.KClass

class SelectAddressPage(
    formModel: KClass<out FormModel>,
    templateName: String,
    content: Map<String, Any>,
    displaySectionHeader: Boolean = false,
    private val lookupAddressPathSegment: String,
    private val addressLookupService: AddressLookupService,
    private val journeyDataService: JourneyDataService,
) : AbstractPage(formModel, templateName, content, displaySectionHeader) {
    override fun enrichModel(
        modelAndView: ModelAndView,
        filteredJourneyData: JourneyData?,
    ) {
        filteredJourneyData!!

        val (houseNameOrNumber, postcode) =
            JourneyDataHelper.getLookupAddressHouseNameOrNumberAndPostcode(
                filteredJourneyData,
                lookupAddressPathSegment,
            )!!

        val addressLookupResults = addressLookupService.search(houseNameOrNumber, postcode)

        val journeyData = journeyDataService.getJourneyDataFromSession()
        val updatedJourneyData = journeyData.withUpdatedLookedUpAddresses(addressLookupResults)
        journeyDataService.setJourneyDataInSession(updatedJourneyData)

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

        modelAndView.addObject("houseNameOrNumber", houseNameOrNumber)
        modelAndView.addObject("postcode", postcode)
        modelAndView.addObject("addressCount", addressLookupResults.size)
        modelAndView.addObject("options", addressRadiosViewModel)
    }

    override fun isSatisfied(bindingResult: BindingResult): Boolean {
        val selectAddressFormModel = bindingResult.target as SelectAddressFormModel
        val selectedAddress = selectAddressFormModel.address
        val journeyData = journeyDataService.getJourneyDataFromSession()

        return selectedAddress == MANUAL_ADDRESS_CHOSEN ||
            (selectedAddress != null && journeyData.getLookedUpAddress(selectedAddress) != null)
    }
}
