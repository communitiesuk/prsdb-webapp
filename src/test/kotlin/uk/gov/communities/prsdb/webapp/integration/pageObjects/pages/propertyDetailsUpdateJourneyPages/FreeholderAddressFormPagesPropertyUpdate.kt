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

class FreeholderLookupAddressFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : LookupAddressFormPage(
        page,
        "${UpdateFreeholdersController.UPDATE_FREEHOLDERS_ROUTE.replace(
            "{propertyOwnershipId}",
            urlArguments["propertyOwnershipId"]!!,
        )}/${FreeholderAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
    )

class FreeholderSelectAddressFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SelectAddressFormPage(
        page,
        "${UpdateFreeholdersController.UPDATE_FREEHOLDERS_ROUTE.replace(
            "{propertyOwnershipId}",
            urlArguments["propertyOwnershipId"]!!,
        )}/${FreeholderAddressTask.ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}",
    )

class FreeholderManualAddressFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : ManualAddressFormPage(
        page,
        "${UpdateFreeholdersController.UPDATE_FREEHOLDERS_ROUTE.replace(
            "{propertyOwnershipId}",
            urlArguments["propertyOwnershipId"]!!,
        )}/${FreeholderAddressTask.ROUTE_SEGMENT}/${ManualAddressStep.ROUTE_SEGMENT}",
    )
