package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class DateOfBirthFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = DateOfBirthForm(page)

    fun submitDateOfBirth(date: LocalDate) {
        submitDateOfBirth(date.dayOfMonth.toString(), date.month.number.toString(), date.year.toString())
    }

    fun submitDateOfBirth(
        day: String,
        month: String,
        year: String,
    ) {
        form.dayInput.fill(day)
        form.monthInput.fill(month)
        form.yearInput.fill(year)
        form.submit()
    }

    class DateOfBirthForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val dayInput = TextInput.textByFieldName(locator, "day")
        val monthInput = TextInput.textByFieldName(locator, "month")
        val yearInput = TextInput.textByFieldName(locator, "year")
    }
}
