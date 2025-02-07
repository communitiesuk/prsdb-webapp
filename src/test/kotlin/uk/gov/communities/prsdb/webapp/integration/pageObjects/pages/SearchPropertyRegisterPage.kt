package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SearchPropertyRegisterPage(
    page: Page,
) : BasePage(page, "/search/property")
