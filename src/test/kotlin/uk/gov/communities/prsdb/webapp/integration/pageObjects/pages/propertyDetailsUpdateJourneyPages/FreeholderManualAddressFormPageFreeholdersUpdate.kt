package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep

class FreeholderManualAddressFormPageFreeholdersUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : ManualAddressFormPage(
        page,
        urlPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    companion object {
        fun urlPath(propertyOwnershipId: Long): String =
            UpdateFreeholdersController.getUpdateFreeholdersRoute(propertyOwnershipId) +
                "/${FreeholderAddressTask.ROUTE_SEGMENT}/${ManualAddressStep.ROUTE_SEGMENT}"
    }
}
