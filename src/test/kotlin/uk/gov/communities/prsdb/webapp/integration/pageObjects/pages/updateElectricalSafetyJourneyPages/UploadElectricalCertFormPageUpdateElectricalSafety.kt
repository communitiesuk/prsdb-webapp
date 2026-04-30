package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UploadCertificateFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep

class UploadElectricalCertFormPageUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : UploadCertificateFormPage(
        page,
        UpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${UploadElectricalCertStep.ROUTE_SEGMENT}",
    )
