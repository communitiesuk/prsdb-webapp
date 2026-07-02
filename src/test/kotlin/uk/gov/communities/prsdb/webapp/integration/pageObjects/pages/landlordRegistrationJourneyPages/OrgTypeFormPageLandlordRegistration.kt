package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgTypeFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}") {
    val form = OrgTypeForm(page)

    fun selectCompany() = form.orgTypeCheckboxes.checkCheckbox(OrgType.COMPANY.toString())

    fun selectCharity() = form.orgTypeCheckboxes.checkCheckbox(OrgType.CHARITY.toString())

    fun selectTrust() = form.orgTypeCheckboxes.checkCheckbox(OrgType.TRUST.toString())

    fun selectNoneOfThese() = form.orgTypeCheckboxes.checkCheckbox(OrgType.NONE.toString())

    class OrgTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val orgTypeCheckboxes = Checkboxes(locator)
    }
}
