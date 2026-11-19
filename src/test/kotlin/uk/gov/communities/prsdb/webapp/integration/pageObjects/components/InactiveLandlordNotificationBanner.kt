package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Page

class InactiveLandlordNotificationBanner(
    page: Page,
) : NotificationBanner(page) {
    val reactivateAPropertyLink =
        Link.byText(page, "reactivate a property", selectorOrLocator = ".govuk-notification-banner__link")
    val completeADraftLink =
        Link.byText(page, "complete a draft", selectorOrLocator = ".govuk-notification-banner__link")
    val registerAPropertyLink =
        Link.byText(page, "register a property", selectorOrLocator = ".govuk-notification-banner__link")
}
