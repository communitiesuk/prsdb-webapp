package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLandlordRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    val submitButton = BaseComponent.getButton(page)

    val heading = BaseComponent.getHeading(page)

    fun getByTextExactly(text: String) = page.getByText(text, Page.GetByTextOptions().setExact(true))

    fun getLink(urlPathSegment: String) =
        page
            .locator(
                "[href=\"/$REGISTER_LANDLORD_JOURNEY_URL/$urlPathSegment\"]",
            )

    fun getLinks(urlPathSegment: String) =
        page
            .locator(
                "[href=\"/$REGISTER_LANDLORD_JOURNEY_URL/$urlPathSegment\"]",
            )

    fun getAllChangeLinks() = BaseComponent.getAllLinks(page, "Change")

    fun submit(): Page {
        submitButton.click()
        return page
    }
}
