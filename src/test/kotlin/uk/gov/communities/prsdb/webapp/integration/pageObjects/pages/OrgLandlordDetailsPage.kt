package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordEmailController.Companion.UPDATE_ORG_EMAIL_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordPhoneNumberController.Companion.UPDATE_ORG_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController.Companion.UPDATE_ORG_TYPE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgLandlordDetailsBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgLandlordDetailsPage(
    page: Page,
) : OrgLandlordDetailsBasePage(page, LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) {
    private val organisationNameChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_NAME_ROUTE/${OrgNameStep.ROUTE_SEGMENT}']"))
    private val companiesHouseChangeLink =
        Link(page.locator("a[href='$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}']"))
    private val organisationTypeChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_TYPE_ROUTE/${OrgTypeStep.ROUTE_SEGMENT}']"))
    private val organisationEmailChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_EMAIL_ROUTE/${OrgEmailStep.ROUTE_SEGMENT}']"))
    private val organisationCharityChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_CHARITY_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}']"))
    private val organisationPhoneNumberChangeLink =
        Link(page.locator("a[href='$UPDATE_ORG_PHONE_NUMBER_ROUTE/${OrgPhoneNumberStep.ROUTE_SEGMENT}']"))

    fun clickOrganisationNameChangeLinkAndWait() = organisationNameChangeLink.clickAndWait()

    fun clickCompaniesHouseChangeLinkAndWait() = companiesHouseChangeLink.clickAndWait()

    fun clickOrganisationTypeChangeLinkAndWait() = organisationTypeChangeLink.clickAndWait()

    fun clickOrganisationEmailChangeLinkAndWait() = organisationEmailChangeLink.clickAndWait()

    fun clickMainContactChangeLinkAndWait() = mainContactCard.getAction("Change").link.clickAndWait()

    fun clickOrganisationCharityChangeLinkAndWait() = organisationCharityChangeLink.clickAndWait()

    fun clickOrganisationPhoneNumberChangeLinkAndWait() = organisationPhoneNumberChangeLink.clickAndWait()
}
