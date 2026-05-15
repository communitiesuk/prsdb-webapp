package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController
import uk.gov.communities.prsdb.webapp.controllers.ManageUsersViewType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DeleteLocalCouncilUserSuccessBasePage

class DeleteLocalCouncilUserSuccessPage(
    page: Page,
    urlArguments: Map<String, String>,
) : DeleteLocalCouncilUserSuccessBasePage(
        page,
        ManageLocalCouncilUsersController.getDeleteUserSuccessRoute(
            urlArguments["localCouncilId"]!!.toInt(),
            urlArguments["deleteeId"]!!.toLong(),
            ManageUsersViewType.LocalAuthorityView,
        ),
    )
