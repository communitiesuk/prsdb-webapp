package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ReasonPagePropertyDeregistration(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        "/$DEREGISTER_PROPERTY_JOURNEY_URL/${urlArguments["propertyOwnershipId"]}/${DeregisterPropertyStepId.Reason.urlPathSegment}",
    ) {
    val form = DeregistrationReasonForm(page)

    fun submitReason(reason: String) {
        form.textAreaInput.fill(reason)
        form.submit()
    }

    class DeregistrationReasonForm(
        page: Page,
    ) : Form(page) {
        val textAreaInput = TextArea.default(locator)
    }
}
