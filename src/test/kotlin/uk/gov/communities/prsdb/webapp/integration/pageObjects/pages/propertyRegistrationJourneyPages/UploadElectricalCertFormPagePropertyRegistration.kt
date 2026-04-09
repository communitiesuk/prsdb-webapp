package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadElectricalCertStep
import java.nio.file.Path

class UploadElectricalCertFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${UploadElectricalCertStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = Form(page)

    fun uploadElectricalCertificate(filePath: Path) {
        page.setInputFiles("input[type=\"file\"]", filePath)
        form.submit()
    }
}
