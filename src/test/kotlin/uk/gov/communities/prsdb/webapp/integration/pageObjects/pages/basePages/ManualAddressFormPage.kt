package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

abstract class ManualAddressFormPage(
    page: Page,
    urlSegment: String,
) : FormBasePage(page, urlSegment) {
    val addressLineOneInput = form.getTextInput("addressLineOne")
    val addressLineTwoInput = form.getTextInput("addressLineTwo")
    val townOrCityInput = form.getTextInput("townOrCity")
    val countyInput = form.getTextInput("county")
    val postcodeInput = form.getTextInput("postcode")
}
