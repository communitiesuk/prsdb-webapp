package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyComplianceJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.PropertyComplianceController
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.JourneyForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GasSafeEngineerNumPagePropertyCompliance(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        PropertyComplianceController.getPropertyCompliancePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyComplianceStepId.GasSafetyEngineerNum.urlPathSegment}",
    ) {
    val form = GasSafeEngineerNumForm(page)

    fun submitEngineerNum(engineerNum: String) {
        form.engineerNumberInput.fill(engineerNum)
        form.submit()
    }

    class GasSafeEngineerNumForm(
        page: Page,
    ) : JourneyForm(page) {
        val engineerNumberInput = TextInput.textByFieldName(locator, "engineerNumber")
    }
}
