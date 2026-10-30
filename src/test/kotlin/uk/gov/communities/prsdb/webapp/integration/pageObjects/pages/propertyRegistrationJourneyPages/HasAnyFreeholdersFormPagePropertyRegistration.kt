package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyFreeholdersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholdersTask

// TODO PDJB-1969: remove this placeholder page when the has any freeholders step becomes an internal step
class HasAnyFreeholdersFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${FreeholdersTask.ROUTE_SEGMENT}/" +
            HasAnyFreeholdersStep.ROUTE_SEGMENT,
    ) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)

    fun submit() = form.submit()
}
