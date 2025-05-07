package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.UPDATE_ROUTE
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NoAddressFoundFormPage

class NoAddressFoundFormPageUpdateLandlordDetails(
    page: Page,
) : NoAddressFoundFormPage(
        page,
        "$UPDATE_ROUTE/${UpdateLandlordDetailsStepId.NoAddressFound.urlPathSegment}",
    )
