package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep

class AreYouSureFormPageLandlordDeregistration(
    page: Page,
) : BasePage(
        page,
        "${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/${AreYouSureStep.ROUTE_SEGMENT}",
    ) {
    val withPropertiesHeading = Heading(page.locator("h1"))
    val withPropertiesYesDeleteButton = Button.byText(page, "Yes, delete")
    val withPropertiesCancelLink = Link.byText(page, "Cancel and go back")
    val noPropertiesContinueButton = Button.byText(page, "Continue")
    val noPropertiesCancelLink = Link.byText(page, "Cancel")
    val noPropertiesForm = AreYouSureForm(page)
    val backLink = BackLink.default(page)

    fun submitYesDelete() {
        withPropertiesYesDeleteButton.clickAndWait()
    }

    fun submitWantsToProceed() {
        noPropertiesForm.areYouSureRadios.selectValue("true")
        noPropertiesForm.submit()
    }

    fun submitDoesNotWantToProceed() {
        noPropertiesForm.areYouSureRadios.selectValue("false")
        noPropertiesForm.submit()
    }

    class AreYouSureForm(
        page: Page,
    ) : PostForm(page) {
        val areYouSureRadios = Radios(locator)
    }
}
