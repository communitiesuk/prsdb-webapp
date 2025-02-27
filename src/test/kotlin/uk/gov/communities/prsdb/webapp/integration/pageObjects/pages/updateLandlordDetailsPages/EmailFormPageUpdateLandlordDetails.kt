package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateLandlordDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage

class EmailFormPageUpdateLandlordDetails(
    page: Page,
) : EmailFormPage(page, "$UPDATE_LANDLORD_DETAILS_URL/${UpdateLandlordDetailsStepId.UpdateEmail.urlPathSegment}")
