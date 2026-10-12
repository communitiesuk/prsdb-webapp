package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController.Companion.CHANGE_ACCESS_LEVEL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep

class AccessLevelPage(
    page: Page,
) : BasePage(page, "/$CHANGE_ACCESS_LEVEL_PATH_SEGMENT/${AccessLevelStep.ROUTE_SEGMENT}") {
    val form = AccessLevelForm(page)

    val currentLevelHint: Locator = page.locator("main p.govuk-body").first()

    fun submitAccessLevel(role: String) {
        form.accessLevelRadios.selectValue(role)
        form.submit()
    }

    class AccessLevelForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val accessLevelRadios = Radios(locator)
    }
}
