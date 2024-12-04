package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

abstract class EmailFormPage(
    page: Page,
    urlSegment: String,
) : FormBasePage(page, urlSegment) {
    val emailInput = form.getTextInput("emailAddress")
}
