package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LocalAuthorityDashboardPage(
    page: Page,
) : BasePage(page, LOCAL_AUTHORITY_DASHBOARD_URL)
