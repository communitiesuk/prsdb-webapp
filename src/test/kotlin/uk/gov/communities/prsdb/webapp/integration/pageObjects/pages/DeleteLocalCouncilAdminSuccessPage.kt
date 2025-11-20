package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.DELETE_ADMIN_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController.Companion.SYSTEM_OPERATOR_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeleteLocalCouncilUserSuccessBasePage

class DeleteLocalCouncilAdminSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : DeleteLocalCouncilUserSuccessBasePage(
        page,
        "$SYSTEM_OPERATOR_ROUTE/$DELETE_ADMIN_PATH_SEGMENT/${urlArguments["localCouncilAdminId"]}/$CONFIRMATION_PATH_SEGMENT",
    )
