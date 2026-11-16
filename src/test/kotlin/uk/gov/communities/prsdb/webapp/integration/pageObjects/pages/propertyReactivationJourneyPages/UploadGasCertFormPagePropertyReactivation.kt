package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep

class UploadGasCertFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadCertFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UploadGasCertStep.ROUTE_SEGMENT}",
    )
