package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

abstract class LicenceNumberFormPage(
    page: Page,
    urlSegment: String,
) : FormBasePage(page, urlSegment) {
    val licenceNumberInput = form.getTextInput("licenceNumber")
}
