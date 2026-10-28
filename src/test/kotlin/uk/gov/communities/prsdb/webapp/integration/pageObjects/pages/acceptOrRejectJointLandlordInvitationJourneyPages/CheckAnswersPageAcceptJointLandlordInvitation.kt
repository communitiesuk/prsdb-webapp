package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import kotlin.test.fail

class CheckAnswersPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${AbstractCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    private val landlordTypeRow = SummaryList.SummaryListRow.byKey(page.locator("html"), "Landlord type")

    override val expectedTitleHeading
        get() =
            when (val landlordType = landlordTypeRow.value.textContent().trim()) {
                "Individual" -> "Check your landlord registration answers"
                "Organisation" -> "Check your organisation registration answers"
                else -> fail("Unexpected landlord type \"$landlordType\" on the check answers page")
            }

    val form = PostForm(page)

    fun confirmAndSubmit() = form.submit()
}
