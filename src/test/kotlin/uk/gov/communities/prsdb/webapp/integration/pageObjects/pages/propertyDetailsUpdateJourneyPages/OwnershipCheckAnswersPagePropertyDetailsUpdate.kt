package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership.UpdateOwnershipCheckYourAnswersStep

class OwnershipCheckAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateOwnershipCheckYourAnswersStep.ROUTE_SEGMENT}",
    ) {
    override val expectedTitleHeading = "Check your ownership answers"

    val form = PostForm(page)
    val warning = Warning.default(page)

    val summaryList = OwnershipCheckAnswersSummaryList(page)

    val summaryCards: Locator
        get() = page.locator(".govuk-summary-card")

    val insetTexts: Locator
        get() = page.locator(".govuk-inset-text")

    val changeFreeholdersLink = Link.byText(page, "Add, change or remove freeholders")
    val changeJointFreeholdersLink = Link.byText(page, "Add, change or remove joint freeholders")
    val changeSuperiorLandlordsLink = Link.byText(page, "Add, change or remove people in the ownership chain")

    fun getSummaryCard(index: Int) = SummaryCard(page, index, "h3")

    fun submit() = form.submit()

    class OwnershipCheckAnswersSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val ownsPropertyRow = getRow("Do you own this property?")
        val ownershipTypeRow = getRow("Ownership type")
        val freeholdTypeRow = getRow("Freehold type")
        val anyoneInOwnershipChainRow = getRow("Is there anyone else in the property’s ownership chain?")
    }
}
