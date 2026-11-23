package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class FreeholderLookupAddressFormPageFreeholdersUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LookupAddressFormPage(
        page,
        urlPath(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    companion object {
        fun urlPath(propertyOwnershipId: Long): String =
            UpdateFreeholdersController.getUpdateFreeholdersRoute(propertyOwnershipId) +
                "/${FreeholderAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}"
    }
}
