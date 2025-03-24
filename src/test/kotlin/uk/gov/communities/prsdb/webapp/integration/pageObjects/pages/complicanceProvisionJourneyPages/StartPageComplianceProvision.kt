package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.complicanceProvisionJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ProvideComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class StartPageComplianceProvision(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ProvideComplianceController.getProvideCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()),
    )
