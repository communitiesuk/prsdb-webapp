package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

class SubHeading(
    page: Page,
) : BaseComponent(page.locator("main header p"))
