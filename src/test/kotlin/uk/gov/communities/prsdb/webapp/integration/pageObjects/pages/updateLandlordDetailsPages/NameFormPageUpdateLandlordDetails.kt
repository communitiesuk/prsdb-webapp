package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordNameController.Companion.UPDATE_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NameFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep

class NameFormPageUpdateLandlordDetails(
    page: Page,
) : NameFormPage(page, "$UPDATE_NAME_ROUTE/${NameStep.ROUTE_SEGMENT}")
