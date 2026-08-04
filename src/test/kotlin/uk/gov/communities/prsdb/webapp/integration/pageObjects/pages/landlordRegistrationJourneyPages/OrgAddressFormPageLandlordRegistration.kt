package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.OrgAddressTask

class OrgAddressFormPageLandlordRegistration(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
    )
