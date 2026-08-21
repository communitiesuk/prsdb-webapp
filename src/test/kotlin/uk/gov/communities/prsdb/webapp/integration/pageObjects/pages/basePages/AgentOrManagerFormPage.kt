package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsMode

abstract class AgentOrManagerFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = AgentOrManagerForm(page)

    fun submitLandlord() {
        form.agentOrManagerRadios.selectValue(WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES)
        form.submit()
    }

    fun submitLettingAgent() {
        form.agentOrManagerRadios.selectValue(WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES)
        form.submit()
    }

    class AgentOrManagerForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val agentOrManagerRadios = Radios(locator)
    }
}
