package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.steps.DeregisterLandlordStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ReasonFormPage

class ReasonFormPageLandlordDeregistration(
    page: Page,
) : ReasonFormPage(page, "${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/${DeregisterLandlordStepId.Reason.urlPathSegment}")
