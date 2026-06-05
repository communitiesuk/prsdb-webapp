package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageCancelJointLandlordInvitation(
    page: Page,
) : BasePage(
        page,
        "/$LANDLORD_PATH_SEGMENT/$CANCEL_JOINT_LANDLORD_INVITATION_JOURNEY_URL/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)

    val goBackToPropertyRecordLink = Link.byText(page, "Go back to property record")
}
