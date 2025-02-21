package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class FilterPanel(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".moj-filter-layout")) {
    constructor(page: Page) : this(page.locator("html"))

    val panel: Locator = locator.locator(".moj-filter")

    val closeFilterPanelButton = Button.byText(locator, "Close filters panel")

    val showFilterPanelButton = Button.byText(locator, "Show filters panel")

    val clearFiltersLink = Link.byText(locator, "Clear filters")

    val selectedHeadings: Locator = locator.locator(".moj-filter__selected >> h3")

    val noFiltersSelectedTextNode: Locator =
        locator.locator(
            ".moj-filter__selected",
            Locator.LocatorOptions().setHasText("No filters selected"),
        )

    private val form = FilterPanelForm(locator)

    fun clickApplyFiltersButton() = form.submit()

    fun getFilterCheckboxes(label: String? = null) = form.getFilterCheckboxes(label)

    fun getRemoveFilterTag(filterOption: String) = FilterTag.byFilterOptionName(locator, filterOption)

    class FilterPanelForm(
        locator: Locator,
    ) : Form(locator) {
        fun getFilterCheckboxes(label: String? = null) = Checkboxes(locator, label)
    }

    class FilterTag(
        override val locator: Locator,
    ) : BaseComponent(locator),
        ClickAndWaitable {
        companion object {
            fun byFilterOptionName(
                parentLocator: Locator,
                filterOption: String,
            ) = FilterTag(parentLocator.locator(".moj-filter__tag", Locator.LocatorOptions().setHasText(filterOption)))
        }
    }
}
