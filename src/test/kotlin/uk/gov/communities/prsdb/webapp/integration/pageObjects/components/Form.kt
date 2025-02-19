package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class Form(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("form")) {
    constructor(page: Page) : this(page.locator("html"))

    fun getErrorMessage(fieldName: String? = null): Locator =
        locator.locator(if (fieldName == null) ".govuk-error-message" else "p[id='$fieldName-error']")

    fun getTextInput(fieldName: String? = null): Locator = locator.locator("input${if (fieldName == null) "" else "[name='$fieldName']"}")

    fun getSectionHeader() = getChildComponent("#section-header")

    fun getRadios() = Radios(locator)

    fun getFieldsetHeading() = FieldsetHeading(locator)

    fun getSelect() = Select(locator)

    fun getTextArea(): Locator = locator.locator("textarea")

    fun getCheckboxes(label: String? = null) = Checkboxes(locator, label)

    fun getSummaryList() = SummaryList(locator)

    fun submit() = SubmitButton(locator).clickAndWait()

    class FieldsetHeading(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-fieldset__heading"))

    class SubmitButton(
        parentLocator: Locator,
    ) : Button(parentLocator.locator("button[type='submit']"))
}
