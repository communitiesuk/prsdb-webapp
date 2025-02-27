package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage

class SelectAddressFormPageUpdateLandlordDetails(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${LandlordDetailsController.UPDATE_ROUTE}/${UpdateLandlordDetailsStepId.SelectEnglandAndWalesAddress.urlPathSegment}",
    )
