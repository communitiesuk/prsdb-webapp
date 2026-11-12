package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ElectricalCertMissingFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertMissingStep

class ElectricalCertMissingFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : ElectricalCertMissingFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ElectricalCertMissingStep.ROUTE_SEGMENT}",
    )
