package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep

// TODO PDJB-662: Implement EPC Search page object
class FindYourEpcFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${FindYourEpcStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = FindYourEpcForm(page)

    fun submitCurrentEpcFound() {
        form.epcSearchModeRadios.selectValue(FindYourEpcMode.CURRENT_EPC_FOUND)
        form.submit()
    }

    fun submitSupersededEpcFound() {
        form.epcSearchModeRadios.selectValue(FindYourEpcMode.SUPERSEDED_EPC_FOUND)
        form.submit()
    }

    fun submitNotFound() {
        form.epcSearchModeRadios.selectValue(FindYourEpcMode.NOT_FOUND)
        form.submit()
    }

    class FindYourEpcForm(
        page: Page,
    ) : Form(page) {
        val epcSearchModeRadios = Radios(locator)
    }
}
