package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

class TextInput(
    override val locator: Locator,
) : BaseComponent(locator),
    TextFillable {
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
}
