package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

class SecondaryButton(
    parentLocator: Locator,
) : Button(parentLocator.locator("css=.govuk-button--secondary"))
