package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep

class OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT}") {
    val form = CharityNumberForm(page)

    fun submitCharityNumber(charityNumber: String) {
        form.charityNumberInput.fill(charityNumber)
        form.submit()
    }

    class CharityNumberForm(
        page: Page,
    ) : PostForm(page) {
        val charityNumberInput = TextInput.textByFieldName(locator, "charityNumber")
    }
}
