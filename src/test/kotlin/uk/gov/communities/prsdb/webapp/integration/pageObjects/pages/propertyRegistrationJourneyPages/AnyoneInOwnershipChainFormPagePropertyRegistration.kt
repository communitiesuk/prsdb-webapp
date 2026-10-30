package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// TODO PDJB-1971: replace this placeholder with the real "is there anyone else in the ownership chain" page
class AnyoneInOwnershipChainFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${SuperiorLandlordsTask.ROUTE_SEGMENT}/" +
            AnyoneInOwnershipChainStep.ROUTE_SEGMENT,
    ) {
    val form = AnyoneInOwnershipChainForm(page)

    fun submitAnyoneInOwnershipChain(anyoneInOwnershipChain: YesOrNo) {
        form.radios.selectValue(anyoneInOwnershipChain)
        form.submit()
    }

    class AnyoneInOwnershipChainForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
