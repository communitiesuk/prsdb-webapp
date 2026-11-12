package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertificateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadEicStep

class UploadEicFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadCertificateFormPage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UploadEicStep.ROUTE_SEGMENT}",
    )
