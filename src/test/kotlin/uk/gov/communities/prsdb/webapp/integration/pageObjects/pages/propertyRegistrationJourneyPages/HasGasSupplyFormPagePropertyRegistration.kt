package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep

class HasGasSupplyFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HasGasSupplyStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = HasGasSupplyForm(page)

    fun submitHasGasSupply() {
        form.hasGasSupplyRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoGasSupply() {
        form.hasGasSupplyRadios.selectValue("false")
        form.submit()
    }

    class HasGasSupplyForm(
        page: Page,
    ) : PostForm(page) {
        val hasGasSupplyRadios = Radios(locator)
    }
}
