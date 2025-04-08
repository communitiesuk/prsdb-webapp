package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class DateFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = DateForm(page)

    fun submitDate(date: LocalDate) {
        submitDate(date.dayOfMonth.toString(), date.month.number.toString(), date.year.toString())
    }

    fun submitDate(
        day: String,
        month: String,
        year: String,
    ) {
        form.dayInput.fill(day)
        form.monthInput.fill(month)
        form.yearInput.fill(year)
        form.submit()
    }

    class DateForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val dayInput = TextInput.textByFieldName(locator, "day")
        val monthInput = TextInput.textByFieldName(locator, "month")
        val yearInput = TextInput.textByFieldName(locator, "year")
    }
}
