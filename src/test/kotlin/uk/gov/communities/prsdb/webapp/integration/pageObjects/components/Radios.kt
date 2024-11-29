package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Radios(
    private val page: Page,
    locator: Locator = page.locator(".govuk-radios"),
) : BaseComponent(locator) {
    fun getRadio(value: String) = getChildComponent("input[value='$value']")

    fun getSelectedValue(): String = getChildComponent("input:checked").getAttribute("value")

    fun <E : Enum<E>> selectValue(value: E) {
        val radio = getRadio(value.name)
        radio.check()
    }

    fun selectValue(value: String) {
        getRadio(value).check()
    }
}
