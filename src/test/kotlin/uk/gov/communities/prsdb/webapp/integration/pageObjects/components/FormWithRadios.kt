package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

class FormWithRadios(
    page: Page,
) : Form(page) {
    val radios = Radios(locator)
}
