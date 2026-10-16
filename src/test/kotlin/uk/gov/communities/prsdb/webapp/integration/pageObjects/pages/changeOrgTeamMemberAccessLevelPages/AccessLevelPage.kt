package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController.Companion.CHANGE_ACCESS_LEVEL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ErrorSummary
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep

class AccessLevelPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(page, "/$CHANGE_ACCESS_LEVEL_PATH_SEGMENT/${AccessLevelStep.ROUTE_SEGMENT}") {
    val form = AccessLevelForm(page)

    val heading = Heading(page.locator("h1.govuk-heading-l"))

    val errorSummary = ErrorSummary(page)

    val currentLevelHint: Locator = page.locator("main p.govuk-body").first()

    override val expectedTitleHeading = "Change access level for ${urlArguments.getValue("memberName")}"

    fun submitAccessLevel(role: String) {
        form.accessLevelRadios.selectValue(role)
        form.submit()
    }

    fun submitWithoutSelectingAccessLevel() {
        form.submit()
    }

    class AccessLevelForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val accessLevelRadios = Radios(locator)
    }
}
