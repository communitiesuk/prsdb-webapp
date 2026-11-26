package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsCouncilViewBasePage

class PropertyDetailsPageLocalCouncilView(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyDetailsCouncilViewBasePage(
        page,
        PropertyDetailsController.getPropertyDetailsForLocalCouncilUserPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    )
