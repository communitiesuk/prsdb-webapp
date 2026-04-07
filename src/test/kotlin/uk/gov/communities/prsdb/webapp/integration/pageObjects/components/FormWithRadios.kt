package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

open class FormWithRadios(
    page: Page,
) : PostForm(page) {
    val radios = Radios(locator)
}
