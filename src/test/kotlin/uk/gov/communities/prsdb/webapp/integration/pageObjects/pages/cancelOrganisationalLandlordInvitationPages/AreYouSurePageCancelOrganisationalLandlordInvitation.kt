package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelOrganisationalLandlordInvitationPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.CancelOrganisationalLandlordInvitationController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class AreYouSurePageCancelOrganisationalLandlordInvitation(
    page: Page,
) : AreYouSureFormBasePage(page, CancelOrganisationalLandlordInvitationController.CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE)
