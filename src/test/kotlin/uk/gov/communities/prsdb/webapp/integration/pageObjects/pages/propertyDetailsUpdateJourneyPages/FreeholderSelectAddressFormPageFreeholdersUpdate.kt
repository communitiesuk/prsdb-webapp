package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

class FreeholderSelectAddressFormPageFreeholdersUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SelectAddressFormPage(
        page,
        urlPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    companion object {
        fun urlPath(propertyOwnershipId: Long): String =
            UpdateFreeholdersController.getUpdateFreeholdersRoute(propertyOwnershipId) +
                "/${FreeholderAddressTask.ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}"
    }
}
