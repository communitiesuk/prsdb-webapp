package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class Form(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("form")) {
    constructor(page: Page) : this(page.locator("html"))

    fun getErrorMessage(fieldName: String? = null): Locator =
        locator.locator(if (fieldName == null) ".govuk-error-message" else "p[id='$fieldName-error']")

    // It's not guaranteed that all forms will have a fieldset heading, but the overwhelming majority do, so we define
    // the property here in the Form class
    val fieldsetHeading = FieldsetHeading(locator)

    fun submit() = SubmitButton(locator).clickAndWait()

    class FieldsetHeading(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-fieldset__heading"))

    class FieldsetLegend(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-fieldset__legend"))

    class SubmitButton(
        parentLocator: Locator,
    ) : Button(parentLocator.locator("button[type='submit']"))
}
