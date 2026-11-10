package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Radios(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-radios")) {
    constructor(page: Page) : this(page.locator("html"))

    val selectedValue: String?
        get() = selectedRadios.takeIf { it.count() > 0 }?.getAttribute("value")

    val isAnyOptionSelected: Boolean
        get() = selectedRadios.count() > 0

    val selectedRadios: Locator
        get() = locator.locator("input:checked")

    fun <E : Enum<E>> selectValue(value: E) {
        val radio = getRadio(value.name)
        radio.check()
    }

    fun selectValue(value: String) {
        getRadio(value).check()
    }

    private fun getRadio(value: String) = locator.locator("input[value='$value']")
}
