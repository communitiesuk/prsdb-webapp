package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ErrorBasePage

class InvalidLinkPageLaUserRegistration(
    page: Page,
) : ErrorBasePage(page, RegisterLocalCouncilUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
