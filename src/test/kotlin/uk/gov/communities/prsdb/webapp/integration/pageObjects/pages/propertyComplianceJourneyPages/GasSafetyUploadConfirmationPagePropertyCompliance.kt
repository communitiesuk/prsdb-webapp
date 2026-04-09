package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadConfirmationFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.GasSafetyUploadConfirmationStep

class GasSafetyUploadConfirmationPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadConfirmationFormPage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${GasSafetyUploadConfirmationStep.ROUTE_SEGMENT}",
    )
