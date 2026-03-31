package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

class TextArea(
    override val locator: Locator,
) : BaseComponent(locator),
    TextFillable {
    companion object {
        fun textByFieldName(
            parentLocator: Locator,
            fieldName: String,
        ) = TextArea(parentLocator.locator("textarea[name='$fieldName']"))

        fun default(parentLocator: Locator) = TextArea(parentLocator.locator("textarea"))
    }
}
