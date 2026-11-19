package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.FreeholderAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

private fun freeholderAddressUrlPath(
    propertyOwnershipId: Long,
    stepRouteSegment: String,
): String {
    val route =
        UpdateFreeholdersController.UPDATE_FREEHOLDERS_ROUTE.replace(
            "{propertyOwnershipId}",
            propertyOwnershipId.toString(),
        )
    return "$route/${FreeholderAddressTask.ROUTE_SEGMENT}/$stepRouteSegment"
}

class FreeholderLookupAddressFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LookupAddressFormPage(
        page,
        freeholderAddressUrlPath(urlArguments["propertyOwnershipId"]!!.toLong(), LookupAddressStep.ROUTE_SEGMENT),
    )

class FreeholderSelectAddressFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SelectAddressFormPage(
        page,
        freeholderAddressUrlPath(urlArguments["propertyOwnershipId"]!!.toLong(), SelectAddressStep.ROUTE_SEGMENT),
    )

class FreeholderManualAddressFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : ManualAddressFormPage(
        page,
        freeholderAddressUrlPath(urlArguments["propertyOwnershipId"]!!.toLong(), ManualAddressStep.ROUTE_SEGMENT),
    )
