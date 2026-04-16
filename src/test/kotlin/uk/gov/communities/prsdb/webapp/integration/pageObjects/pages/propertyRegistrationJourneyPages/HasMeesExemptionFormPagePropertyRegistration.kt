package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep

class HasMeesExemptionFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HasMeesExemptionStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = HasMeesExemptionForm(page)
    val sectionHeader = SectionHeader(page.locator("html"))

    fun submitHasMeesExemption() {
        form.hasMeesExemptionRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNoMeesExemption() {
        form.hasMeesExemptionRadios.selectValue("false")
        form.submit()
    }

    class HasMeesExemptionForm(
        page: Page,
    ) : PostForm(page) {
        val hasMeesExemptionRadios = Radios(locator)
    }
}
