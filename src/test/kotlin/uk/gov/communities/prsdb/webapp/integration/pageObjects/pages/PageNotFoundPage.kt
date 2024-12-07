package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PageNotFoundPage(
    page: Page,
) : BasePage(page, "/error")
