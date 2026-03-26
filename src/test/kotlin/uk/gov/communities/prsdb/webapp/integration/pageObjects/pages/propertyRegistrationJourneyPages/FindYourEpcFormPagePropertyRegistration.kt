package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep

class FindYourEpcFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${FindYourEpcStep.ROUTE_SEGMENT}") {
    val form = FormWithRadios(page)

    fun submitLatestEpcFound() {
        form.radios.selectValue(FindYourEpcMode.LATEST_EPC_FOUND)
        form.submit()
    }

    fun submitSupersededEpcFound() {
        form.radios.selectValue(FindYourEpcMode.SUPERSEDED_EPC_FOUND)
        form.submit()
    }

    fun submitNotFound() {
        form.radios.selectValue(FindYourEpcMode.NOT_FOUND)
        form.submit()
    }
}
