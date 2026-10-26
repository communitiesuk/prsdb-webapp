package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.MaintenanceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class MaintenancePage(
    page: Page,
) : BasePage(page, MaintenanceController.MAINTENANCE_ROUTE)
