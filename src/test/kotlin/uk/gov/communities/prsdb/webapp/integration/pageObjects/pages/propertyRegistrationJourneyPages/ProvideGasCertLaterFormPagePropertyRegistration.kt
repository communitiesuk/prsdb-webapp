package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideGasCertLaterStep

class ProvideGasCertLaterFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${ProvideGasCertLaterStep.ROUTE_SEGMENT}") {
    val form = Form(page)

    // This will only be populated for the occupied variant of this page
    val insetText = InsetText(page)
}
