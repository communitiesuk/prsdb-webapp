package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckGasCertUploadsFormPagePropertyRegistration.CheckUploadsForm
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep

class CheckGasCertUploadsFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LandlordUpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${CheckGasCertUploadsStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading: String
        get() = table.rows.count().let { "You’ve uploaded $it gas safety certificate file${if (it == 1) "" else "s"}" }

    val table = Table(page)
    val form = CheckUploadsForm(page)
}
