package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DEREGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

// TODO: PRSD-696 - can we make it so we can pass the property ownership id into here? Currently
class AreYouSureFormPagePropertyDeregistration(
    page: Page,
) : BasePage(page, "/$DEREGISTER_PROPERTY_JOURNEY_URL/1/${DeregisterPropertyStepId.AreYouSure.urlPathSegment}") {
    val form = AreYouSureForm(page)

    val backLink = BackLink.default(page)

    fun submitWantsToProceed() {
        form.areYouSureRadios.selectValue("true")
        form.submit()
    }

    fun submitDoesNotWantToProceed() {
        form.areYouSureRadios.selectValue("false")
        form.submit()
    }

    class AreYouSureForm(
        page: Page,
    ) : Form(page) {
        val areYouSureRadios = Radios(locator)
    }
}
