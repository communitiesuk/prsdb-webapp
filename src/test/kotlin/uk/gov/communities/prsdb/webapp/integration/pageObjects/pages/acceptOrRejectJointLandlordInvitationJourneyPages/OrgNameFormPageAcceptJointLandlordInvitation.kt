package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgNameFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep

class OrgNameFormPageAcceptJointLandlordInvitation(
    page: Page,
) : OrgNameFormBasePage(page, "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgNameStep.ROUTE_SEGMENT}")
