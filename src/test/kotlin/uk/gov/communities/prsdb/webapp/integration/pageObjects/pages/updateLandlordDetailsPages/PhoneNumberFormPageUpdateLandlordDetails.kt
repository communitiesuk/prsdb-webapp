package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL
import uk.gov.communities.prsdb.webapp.forms.steps.UpdateDetailsStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage

class PhoneNumberFormPageUpdateLandlordDetails(
    page: Page,
) : PhoneNumberFormPage(page, "$UPDATE_LANDLORD_DETAILS_URL/${UpdateDetailsStepId.UpdatePhoneNumber.urlPathSegment}")
