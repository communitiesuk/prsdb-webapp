package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.EDIT_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EditLocalCouncilUserBasePage

class EditLocalCouncilAdminPage(
    page: Page,
    urlArguments: Map<String, String>,
) : EditLocalCouncilUserBasePage(page, "$SYSTEM_OPERATOR_ROUTE/$EDIT_ADMIN_PATH_SEGMENT/${urlArguments["laAdminId"]}") {
    val backLink = BackLink.default(page)
}
