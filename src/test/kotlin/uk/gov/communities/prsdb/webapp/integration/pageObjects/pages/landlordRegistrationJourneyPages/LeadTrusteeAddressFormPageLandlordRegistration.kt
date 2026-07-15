package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

class LeadTrusteeAddressFormPageLandlordRegistration(
    page: Page,
) : LookupAddressFormPage(
        page,
        "$LANDLORD_REGISTRATION_ROUTE/${TrusteeAddressTask.LEAD_TRUSTEE_ADDRESS_ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
    )
