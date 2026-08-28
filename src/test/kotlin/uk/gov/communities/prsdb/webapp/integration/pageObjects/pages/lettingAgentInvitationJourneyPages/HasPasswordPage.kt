package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.HasPasswordStep

// TODO PDJB-1658: Remove when check password set step is implemented
class HasPasswordPage(
    page: Page,
) : BasePage(page, "/${HasPasswordStep.ROUTE_SEGMENT}") {
    val form = FormWithRadios(page)

    fun submitHasPassword() {
        form.radios.selectValue("true")
        form.submit()
    }

    fun submitNoPassword() {
        form.radios.selectValue("false")
        form.submit()
    }
}
