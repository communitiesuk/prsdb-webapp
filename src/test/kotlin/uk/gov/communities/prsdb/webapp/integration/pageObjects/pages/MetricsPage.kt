package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.MetricsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ErrorSummary
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class MetricsPage(
    page: Page,
) : BasePage(page, MetricsController.METRICS_URL) {
    val form = MetricsForm(page)
    val errorSummary = ErrorSummary(page)
    val refreshButton = Button.byText(page, "Refresh")
    val metricsList = MetricsSummaryList(page)

    fun submitDateRange(
        fromDay: String,
        fromMonth: String,
        fromYear: String,
        toDay: String,
        toMonth: String,
        toYear: String,
    ) {
        form.fromDay.fill(fromDay)
        form.fromMonth.fill(fromMonth)
        form.fromYear.fill(fromYear)
        form.toDay.fill(toDay)
        form.toMonth.fill(toMonth)
        form.toYear.fill(toYear)
        form.submit()
    }

    class MetricsForm(
        page: Page,
    ) : PostForm(page) {
        val fromDay = TextInput.textByFieldName(locator, "fromDay")
        val fromMonth = TextInput.textByFieldName(locator, "fromMonth")
        val fromYear = TextInput.textByFieldName(locator, "fromYear")
        val toDay = TextInput.textByFieldName(locator, "toDay")
        val toMonth = TextInput.textByFieldName(locator, "toMonth")
        val toYear = TextInput.textByFieldName(locator, "toYear")
    }

    class MetricsSummaryList(
        page: Page,
    ) : SummaryList(page) {
        fun rowKey(index: Int) = getRow(index).key

        fun rowValue(index: Int) = getRow(index).value
    }
}
