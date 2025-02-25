package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage

class ManualAddressFormPageUpdateLandlordDetails(
    page: Page,
) : ManualAddressFormPage(page, "${LandlordDetailsController.UPDATE_ROUTE}/${UpdateDetailsStepId.ManualEnglandAndWalesAddress}")
