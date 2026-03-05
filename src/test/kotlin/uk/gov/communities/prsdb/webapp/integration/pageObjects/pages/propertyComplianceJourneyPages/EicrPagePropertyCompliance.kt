package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CertificateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps.EicrStep

class EicrPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : CertificateFormPage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${EicrStep.ROUTE_SEGMENT}",
    )
