package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class NumberOfPeopleFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val backLink = BackLink.default(page)

    val form = NumOfPeopleForm(page)

    fun submitNumOfPeople(num: Int) = submitNumOfPeople(num.toString())

    fun submitNumOfPeople(num: String) {
        form.peopleInput.fill(num)
        form.submit()
    }

    class NumOfPeopleForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val peopleInput = TextInput.textByFieldName(locator, "numberOfPeople")
    }
}
