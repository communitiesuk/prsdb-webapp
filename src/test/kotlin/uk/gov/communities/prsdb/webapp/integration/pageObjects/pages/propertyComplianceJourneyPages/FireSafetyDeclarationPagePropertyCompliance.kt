package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class FireSafetyDeclarationPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.FireSafetyDeclaration.urlPathSegment}",
    ) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))

    val form = FireSafetyDeclarationForm(page)

    fun submitHasDeclaredFireSafety() {
        form.hasDeclaredRadios.selectValue("true")
        form.submit()
    }

    fun submitHasNotDeclaredFireSafety() {
        form.hasDeclaredRadios.selectValue("false")
        form.submit()
    }

    class FireSafetyDeclarationForm(
        page: Page,
    ) : Form(page) {
        val fieldHeading: Locator = page.locator("p.govuk-body-l")

        val hasDeclaredRadios = Radios(locator)
    }
}
