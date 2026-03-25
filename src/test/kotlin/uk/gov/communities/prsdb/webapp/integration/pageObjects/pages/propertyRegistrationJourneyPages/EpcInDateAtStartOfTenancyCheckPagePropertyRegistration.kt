package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep

// TODO PDJB-665: Implement EPC Expiry Check page object
class EpcInDateAtStartOfTenancyCheckPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = EpcInDateAtStartOfTenancyCheckForm(page)

    fun submitEpcInDate() {
        form.epcInDateAtStartOfTenancyRadios.selectValue("true")
        form.submit()
    }

    fun submitEpcExpired() {
        form.epcInDateAtStartOfTenancyRadios.selectValue("false")
        form.submit()
    }

    class EpcInDateAtStartOfTenancyCheckForm(
        page: Page,
    ) : Form(page) {
        val epcInDateAtStartOfTenancyRadios = Radios(locator)
    }
}
