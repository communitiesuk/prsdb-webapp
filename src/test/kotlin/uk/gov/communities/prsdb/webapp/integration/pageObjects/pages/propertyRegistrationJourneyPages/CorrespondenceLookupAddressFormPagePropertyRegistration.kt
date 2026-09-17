package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BulletPointList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Paragraph
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.CorrespondenceAddressTask

class CorrespondenceLookupAddressFormPagePropertyRegistration(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
            "${CorrespondenceAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val choosingAPostalAddressHeading = Heading(page.locator("h2.govuk-heading-m"))
    val insetText = InsetText(page)
    val bulletPointList = BulletPointList(page)
    val postcodeLabel: Locator = page.locator("label[for='postcode']")
    val houseNameOrNumberLabel: Locator = page.locator("label[for='houseNameOrNumber']")

    fun paragraph(text: String) = Paragraph.byText(page, text)
}
