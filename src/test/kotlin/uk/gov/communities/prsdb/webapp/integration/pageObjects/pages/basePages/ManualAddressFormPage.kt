package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class ManualAddressFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = ManualAddressForm(page)

    fun submitAddress(
        addressLineOne: String? = null,
        addressLineTwo: String? = null,
        townOrCity: String? = null,
        county: String? = null,
        postcode: String? = null,
    ) {
        if (addressLineOne != null) {
            form.addressLineOneInput.fill(addressLineOne)
        }
        if (addressLineTwo != null) {
            form.addressLineTwoInput.fill(addressLineTwo)
        }
        if (townOrCity != null) {
            form.townOrCityInput.fill(townOrCity)
        }
        if (county != null) {
            form.countyInput.fill(county)
        }
        if (postcode != null) {
            form.postcodeInput.fill(postcode)
        }
        form.submit()
    }

    class ManualAddressForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val addressLineOneInput = TextInput.textByFieldName(locator, "addressLineOne")
        val addressLineTwoInput = TextInput.textByFieldName(locator, "addressLineTwo")
        val townOrCityInput = TextInput.textByFieldName(locator, "townOrCity")
        val countyInput = TextInput.textByFieldName(locator, "county")
        val postcodeInput = TextInput.textByFieldName(locator, "postcode")
    }
}
