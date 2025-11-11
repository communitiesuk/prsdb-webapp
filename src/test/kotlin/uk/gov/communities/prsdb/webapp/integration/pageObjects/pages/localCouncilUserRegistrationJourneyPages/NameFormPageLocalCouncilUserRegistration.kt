package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NameFormPage

class NameFormPageLocalCouncilUserRegistration(
    page: Page,
) : NameFormPage(
        page,
        "${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLocalCouncilUserStepId.Name.urlPathSegment}",
    )
