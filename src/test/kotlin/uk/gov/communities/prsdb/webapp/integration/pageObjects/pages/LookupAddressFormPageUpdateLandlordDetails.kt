package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordDetailsUpdateStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage

class LookupAddressFormPageUpdateLandlordDetails(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${LandlordDetailsController.UPDATE_ROUTE}/${LandlordDetailsUpdateStepId.LookupEnglandAndWalesAddress.urlPathSegment}",
    )
