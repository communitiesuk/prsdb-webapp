package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep

class UploadGasCertFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadCertFormBasePage(
        page,
        LandlordUpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${UploadGasCertStep.ROUTE_SEGMENT}",
    )
