package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage

class EmailFormPageLaUserRegistration(
    page: Page,
) : EmailFormPage(page, "${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.Email.urlPathSegment}")
