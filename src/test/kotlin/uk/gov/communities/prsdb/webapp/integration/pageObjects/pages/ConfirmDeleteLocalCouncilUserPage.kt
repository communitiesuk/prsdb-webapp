package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DELETE_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeleteLocalCouncilUserBasePage

class ConfirmDeleteLocalCouncilUserPage(
    page: Page,
) : DeleteLocalCouncilUserBasePage(page, "/$DELETE_USER_PATH_SEGMENT/")
