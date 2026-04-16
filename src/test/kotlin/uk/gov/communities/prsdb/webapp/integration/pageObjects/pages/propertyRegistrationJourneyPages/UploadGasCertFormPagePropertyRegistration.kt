package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import java.nio.file.Path

class UploadGasCertFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${UploadGasCertStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = FormWithSectionHeader(page)

    fun uploadGasCertificate(filePath: Path) {
        page.setInputFiles("input[type=\"file\"]", filePath)
        form.submit()
    }
}
