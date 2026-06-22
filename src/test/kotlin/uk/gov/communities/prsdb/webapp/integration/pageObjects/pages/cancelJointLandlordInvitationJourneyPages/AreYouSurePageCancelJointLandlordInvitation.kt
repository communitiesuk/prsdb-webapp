package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.AreYouSureFormBasePage

class AreYouSurePageCancelJointLandlordInvitation(
    page: Page,
) : AreYouSureFormBasePage(
        page,
        "/$LANDLORD_PATH_SEGMENT/$CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL",
    )
