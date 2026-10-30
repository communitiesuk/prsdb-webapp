package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveSuperiorLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask

// TODO PDJB-1978: remove this placeholder page when the save superior landlord step becomes an internal step
class SaveSuperiorLandlordFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${SuperiorLandlordsTask.ROUTE_SEGMENT}/" +
            SaveSuperiorLandlordStep.ROUTE_SEGMENT,
    ) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)

    fun submit() = form.submit()
}
