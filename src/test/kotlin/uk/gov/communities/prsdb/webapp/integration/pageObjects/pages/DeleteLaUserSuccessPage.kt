package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeleteLaUserSuccessBasePage

class DeleteLaUserSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : DeleteLaUserSuccessBasePage(
        page,
        ManageLocalAuthorityUsersController.getLaDeleteUserSuccessRoute(
            urlArguments["localAuthorityId"]!!.toInt(),
            urlArguments["deleteeId"]!!.toLong(),
        ),
    )
