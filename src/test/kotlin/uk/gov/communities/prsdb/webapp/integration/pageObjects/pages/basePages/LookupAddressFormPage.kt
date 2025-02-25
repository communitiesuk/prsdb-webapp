package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class LookupAddressFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = LookupAddressForm(page)

    fun submitPostcodeAndBuildingNameOrNumber(
        postcode: String,
        buildingNameOrNumber: String,
    ) {
        form.postcodeInput.fill(postcode)
        form.houseNameOrNumberInput.fill(buildingNameOrNumber)
        form.submit()
    }

    class LookupAddressForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val postcodeInput = TextInput.textByFieldName(locator, "postcode")
        val houseNameOrNumberInput = TextInput.textByFieldName(locator, "houseNameOrNumber")
    }
}
