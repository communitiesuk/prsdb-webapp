package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalCertUploadsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEicUploadsStep

class CheckEicUploadsFormPagePropertyRegistration(
    page: Page,
) : CheckElectricalCertUploadsFormBasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckEicUploadsStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("main"))
}
