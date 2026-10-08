package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteTeamMemberJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.InviteTeamMemberController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Paragraph
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteTeamMemberFormPage(
    page: Page,
) : BasePage(page, InviteTeamMemberController.INVITE_TEAM_MEMBER_PATH) {
    val heading = Heading(page.locator("main h1"))
    val introduction = Paragraph(page.locator("main p.govuk-body").first())
    val form = FormWithRadios(page)
    val emailInput = TextInput.emailByFieldName(page.locator("html"), "emailAddress")
    val accessLevelFormGroup: Locator = page.locator("main .govuk-form-group:has(> .govuk-fieldset)")

    fun submitInvitation(
        email: String,
        role: OrganisationalLandlordUserRole,
    ) {
        emailInput.fill(email)
        form.radios.selectValue(role)
        form.submit()
    }
}
