package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

open class ConfirmationBanner(
    page: Page,
) : BaseComponent(page.locator(".govuk-panel--confirmation"))
