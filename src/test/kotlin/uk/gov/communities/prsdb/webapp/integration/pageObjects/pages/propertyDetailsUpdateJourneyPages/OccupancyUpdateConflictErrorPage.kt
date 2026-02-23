package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.UPDATE_CONFLICT_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ErrorBasePage

class OccupancyUpdateConflictErrorPage(
    page: Page,
) : ErrorBasePage(
        page,
        UPDATE_CONFLICT_ERROR_ROUTE,
    )
