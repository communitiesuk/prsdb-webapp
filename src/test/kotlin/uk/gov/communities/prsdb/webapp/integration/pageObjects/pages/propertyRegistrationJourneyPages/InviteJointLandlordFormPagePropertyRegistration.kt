package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage

class InviteJointLandlordFormPagePropertyRegistration(
    page: Page,
) : EmailFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.InviteJointLandlord.urlPathSegment}",
    ) {
    val heading = Heading(page.locator("h1"))
}

class InviteAnotherJointLandlordFormPagePropertyRegistration(
    page: Page,
) : EmailFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/invite-another-joint-landlord",
    ) {
    val heading = Heading(page.locator("h1"))
}
