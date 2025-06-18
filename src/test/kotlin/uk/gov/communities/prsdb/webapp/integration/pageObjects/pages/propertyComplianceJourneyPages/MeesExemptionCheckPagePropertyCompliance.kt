package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class MeesExemptionCheckPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.MeesExemptionCheck.urlPathSegment}",
    ) {
    val form = MeesExemptionCheckForm(page)

    fun submitHasExemption() {
        form.propertyHasExemption.selectValue("true")
        form.submit()
    }

    fun submitDoesNotHaveExemption() {
        form.propertyHasExemption.selectValue("false")
        form.submit()
    }

    class MeesExemptionCheckForm(
        page: Page,
    ) : PostForm(page) {
        val propertyHasExemption = Radios(locator)
    }
}
