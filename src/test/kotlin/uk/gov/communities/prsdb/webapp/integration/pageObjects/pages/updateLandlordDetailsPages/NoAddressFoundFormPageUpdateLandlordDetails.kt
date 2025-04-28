package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NoAddressFoundFormPage

class NoAddressFoundFormPageUpdateLandlordDetails(
    page: Page,
) : NoAddressFoundFormPage(
        page,
        "/$LANDLORD_DETAILS_PATH_SEGMENT/$UPDATE_PATH_SEGMENT/${UpdateLandlordDetailsStepId.NoAddressFound.urlPathSegment}",
    )
