package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep

// TODO PDJB-661: Implement Check Matched EPC page object
class CheckMatchedEpcFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckMatchedEpcStep.SEARCHED_ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = CheckMatchedEpcForm(page)

    fun submitEpcCompliant() {
        form.checkMatchedEpcModeRadios.selectValue(CheckMatchedEpcMode.EPC_COMPLIANT)
        form.submit()
    }

    fun submitEpcIncorrect() {
        form.checkMatchedEpcModeRadios.selectValue(CheckMatchedEpcMode.EPC_INCORRECT)
        form.submit()
    }

    fun submitEpcOlderThan10Years() {
        form.checkMatchedEpcModeRadios.selectValue(CheckMatchedEpcMode.EPC_OLDER_THAN_10_YEARS)
        form.submit()
    }

    fun submitEpcLowEnergyRating() {
        form.checkMatchedEpcModeRadios.selectValue(CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING)
        form.submit()
    }

    class CheckMatchedEpcForm(
        page: Page,
    ) : Form(page) {
        val checkMatchedEpcModeRadios = Radios(locator)
    }
}
