package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLaUserRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    val heading = page.locator(".govuk-heading-l")
    val submitButton = page.locator("button[type=\"submit\"]")
    val changeNameLink =
        page.locator(
            "[href=\"/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/${RegisterLaUserStepId.Name.urlPathSegment}\"]",
        )
    val changeEmailLink =
        page.locator(
            "[href=\"/${JourneyType.LA_USER_REGISTRATION.urlPathSegment}/${RegisterLaUserStepId.Email.urlPathSegment}\"]",
        )

    fun changeName(): Page {
        changeNameLink.click()
        return page
    }

    fun changeEmail(): Page {
        changeEmailLink.click()
        return page
    }

    fun submit(): Page {
        submitButton.click()
        return page
    }
}
