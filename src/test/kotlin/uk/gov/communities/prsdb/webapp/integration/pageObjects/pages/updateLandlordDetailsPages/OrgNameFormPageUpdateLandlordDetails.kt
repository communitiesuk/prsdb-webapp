package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep

class OrgNameFormPageUpdateLandlordDetails(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}") {
    val form = OrgNameForm(page)

    fun submitName(name: String) {
        form.orgNameInput.fill(name)
        form.submit()
    }

    class OrgNameForm(
        page: Page,
    ) : PostForm(page) {
        val orgNameInput = TextInput.textByFieldName(locator, "orgName")
    }
}
