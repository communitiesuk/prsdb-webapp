package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class PageNotFoundPage(
    page: Page,
) : BasePage(page, "/error")
