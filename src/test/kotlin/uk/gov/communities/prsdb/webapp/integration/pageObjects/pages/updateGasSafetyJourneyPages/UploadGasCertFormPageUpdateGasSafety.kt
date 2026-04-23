package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.UploadGasCertStep
import java.nio.file.Path

class UploadGasCertFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${UploadGasCertStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)

    fun uploadGasCertificate(filePath: Path) {
        page.setInputFiles("input[type=\"file\"]", filePath)
        form.submit()
    }
}
