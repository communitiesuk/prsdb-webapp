package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DELETE_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeleteLaUserBasePage

class DeleteLaAdminPage(
    page: Page,
    urlArguments: Map<String, String>,
) : DeleteLaUserBasePage(page, "$SYSTEM_OPERATOR_ROUTE/$DELETE_ADMIN_PATH_SEGMENT/${urlArguments["laAdminId"]}") {
    val backLink = BackLink.default(page)
}
