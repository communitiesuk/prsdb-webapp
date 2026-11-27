package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsCouncilViewBasePage

class PropertyDetailsPageSystemOperatorView(
    page: Page,
    urlArguments: Map<String, String>,
) : PropertyDetailsCouncilViewBasePage(
        page,
        PropertyDetailsController.getPropertyDetailsForSystemOperatorPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    )
