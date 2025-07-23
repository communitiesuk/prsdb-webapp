package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page

class EpcLookupBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment)
