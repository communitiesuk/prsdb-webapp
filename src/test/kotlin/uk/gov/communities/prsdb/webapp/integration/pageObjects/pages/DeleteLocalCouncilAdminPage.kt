package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DELETE_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeleteLocalCouncilUserBasePage

class DeleteLocalCouncilAdminPage(
    page: Page,
    urlArguments: Map<String, String>,
) : DeleteLocalCouncilUserBasePage(page, "$SYSTEM_OPERATOR_ROUTE/$DELETE_ADMIN_PATH_SEGMENT/${urlArguments["localCouncilAdminId"]}") {
    val backLink = BackLink.default(page)
}
