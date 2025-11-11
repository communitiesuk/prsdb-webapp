package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage

class EmailFormPageLocalCouncilUserRegistration(
    page: Page,
) : EmailFormPage(
        page,
        "${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLocalCouncilUserStepId.Email.urlPathSegment}",
    )
