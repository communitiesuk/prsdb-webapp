package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonRemoveConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask

// TODO PDJB-1970: replace this placeholder with the real remove freeholder confirmation page
class RemoveFreeholderFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${FreeholdersTask.ROUTE_SEGMENT}/" +
            PersonRemoveConfirmationStep.ROUTE_SEGMENT,
    ) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)

    fun submit() = form.submit()
}
