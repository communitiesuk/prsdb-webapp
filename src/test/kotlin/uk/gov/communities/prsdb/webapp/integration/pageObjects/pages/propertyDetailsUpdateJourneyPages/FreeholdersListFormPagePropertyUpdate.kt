package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep

class FreeholdersListFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateFreeholdersController.getUpdateFreeholdersFirstStepRoute(urlArguments["propertyOwnershipId"]!!.toLong()),
    ) {
    val form = PostForm(page)
    val addAnotherButton: Locator = page.locator("a.govuk-button--secondary")

    fun getSummaryCard(index: Int) = SummaryCard(page, index)

    fun getSummaryCard(title: String) = SummaryCard(page, title)

    val removeActionCount: Int
        get() = page.locator(".govuk-summary-card__action:has-text('Remove')").count()

    fun submit() = form.submit()
}

class FreeholderProvideDetailsFormPagePropertyUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        "${UpdateFreeholdersController.UPDATE_FREEHOLDERS_ROUTE.replace(
            "{propertyOwnershipId}",
            urlArguments["propertyOwnershipId"]!!,
        )}/${ProvideFreeholderDetailsStep.ROUTE_SEGMENT}",
    )
