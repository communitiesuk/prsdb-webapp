package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.EDIT_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EditLocalCouncilUserBasePage

class EditLocalCouncilUserPage(
    page: Page,
) : EditLocalCouncilUserBasePage(page, "/$EDIT_USER_PATH_SEGMENT/")
