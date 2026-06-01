package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.acceptOrRejectJointLandlordInvitationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep

class CountryOfResidenceFormPageAcceptJointLandlordInvitation(
    page: Page,
) : BasePage(
        page,
        "$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${CountryOfResidenceStep.ROUTE_SEGMENT}",
    ) {
    val form = CountryOfResidenceForm(page)

    fun submitUk() {
        form.selectUk()
        form.submit()
    }

    fun submitNonUk() {
        form.selectNonUk()
        form.submit()
    }

    class CountryOfResidenceForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val residentInUkRadios = Radios(locator)

        fun selectUk() = residentInUkRadios.selectValue("true")

        fun selectNonUk() = residentInUkRadios.selectValue("false")
    }
}
