package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class PostForm(
    parentLocator: Locator,
    formSelector: String = "form",
) : Form(parentLocator, formSelector) {
    constructor(page: Page) : this(page.locator("html"))

    val csrfToken: String
        get() = locator.locator("input[name='_csrf']").inputValue()
}
