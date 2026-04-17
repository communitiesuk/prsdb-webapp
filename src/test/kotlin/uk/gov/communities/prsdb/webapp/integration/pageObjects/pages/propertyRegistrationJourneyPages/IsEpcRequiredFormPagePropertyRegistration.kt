package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep

class IsEpcRequiredFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${IsEpcRequiredStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = IsEpcRequiredForm(page)

    fun submitEpcRequired() {
        form.epcRequired.selectValue("true")
        form.submit()
    }

    fun submitEpcNotRequired() {
        form.epcRequired.selectValue("false")
        form.submit()
    }

    class IsEpcRequiredForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val epcRequired = Radios(locator)
    }
}
