package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

class TextInput(
    locator: Locator,
) : BaseComponent(locator) {
    companion object {
        fun textByFieldName(
            parentLocator: Locator,
            fieldName: String,
        ) = factory(parentLocator, "text", fieldName)

        fun emailByFieldName(
            parentLocator: Locator,
            fieldName: String,
        ) = factory(parentLocator, "email", fieldName)

        private fun factory(
            parentLocator: Locator,
            type: String = "text",
            fieldName: String? = null,
        ) = TextInput(parentLocator.locator("input[type='$type']${if (fieldName == null) "" else "[name='$fieldName']"}"))
    }

    fun fill(text: String) = locator.fill(text)
}
