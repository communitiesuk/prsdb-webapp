package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrganisationTypePageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}") {
    val form = OrganisationTypeForm(page)

    fun selectCompany() = form.orgTypeCheckboxes.checkCheckbox(OrgType.COMPANY.toString())

    fun selectNoneOfThese() = form.orgTypeCheckboxes.checkCheckbox(OrgType.NONE.toString())

    class OrganisationTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val orgTypeCheckboxes = Checkboxes(locator)
    }
}
