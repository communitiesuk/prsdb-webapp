package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep

// TODO PDJB-662: Implement EPC Search page object
class EpcSearchFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EpcSearchStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = EpcSearchForm(page)

    fun submitCurrentEpcFound() {
        form.epcSearchModeRadios.selectValue(EpcSearchMode.CURRENT_EPC_FOUND)
        form.submit()
    }

    fun submitSupersededEpcFound() {
        form.epcSearchModeRadios.selectValue(EpcSearchMode.SUPERSEDED_EPC_FOUND)
        form.submit()
    }

    fun submitNotFound() {
        form.epcSearchModeRadios.selectValue(EpcSearchMode.NOT_FOUND)
        form.submit()
    }

    class EpcSearchForm(
        page: Page,
    ) : Form(page) {
        val epcSearchModeRadios = Radios(locator)
    }
}
