package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

abstract class NameFormPage(
    page: Page,
    urlSegment: String,
) : FormBasePage(page, urlSegment) {
    val nameInput = form.getTextInput("name")
}
