package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

abstract class LookupAddressFormPage(
    page: Page,
    urlSegment: String,
) : FormBasePage(page, urlSegment) {
    val postcodeInput = form.getTextInput("postcode")
    val houseNameOrNumberInput = form.getTextInput("houseNameOrNumber")
}
