package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NameFormPage

class NameFormPageLaUserRegistration(
    page: Page,
) : NameFormPage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.Name.urlPathSegment}")
