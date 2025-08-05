package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ErrorBasePage

class InvalidLinkPageLaUserRegistration(
    page: Page,
) : ErrorBasePage(page, RegisterLAUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
