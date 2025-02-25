package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage

class SelectAddressFormPageUpdateLandlordDetails(
    page: Page,
    // QQ - use constants
) : SelectAddressFormPage(page, "${LandlordDetailsController.UPDATE_ROUTE}/${UpdateDetailsStepId.SelectEnglandAndWalesAddress}")
