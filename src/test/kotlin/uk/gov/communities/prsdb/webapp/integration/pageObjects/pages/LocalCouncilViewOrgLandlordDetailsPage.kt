package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgLandlordDetailsBasePage

class LocalCouncilViewOrgLandlordDetailsPage(
    page: Page,
    urlArguments: Map<String, String>,
) : OrgLandlordDetailsBasePage(
        page,
        LandlordDetailsController.getLandlordDetailsForLocalCouncilUserPath(urlArguments["id"]!!.toLong()),
    )
