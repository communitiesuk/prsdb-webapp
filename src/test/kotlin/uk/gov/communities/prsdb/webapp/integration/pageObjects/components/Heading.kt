package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

class Heading(
    page: Page,
) : BaseComponent(page.locator("main header h1"))
