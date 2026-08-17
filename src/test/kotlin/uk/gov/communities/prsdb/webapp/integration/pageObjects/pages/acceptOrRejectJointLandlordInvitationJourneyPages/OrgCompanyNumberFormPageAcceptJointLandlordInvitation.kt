package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCompanyNumberFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

class OrgCompanyNumberFormPageAcceptJointLandlordInvitation(
    page: Page,
) : OrgCompanyNumberFormBasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${OrgCompanyNumberStep.ROUTE_SEGMENT}",
    )
