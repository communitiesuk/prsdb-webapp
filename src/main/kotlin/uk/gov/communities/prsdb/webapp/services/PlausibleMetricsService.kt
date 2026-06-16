package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.PlausibleClient
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PRIVACY_NOTICE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController.Companion.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.models.dataModels.JourneyCompletionRatesDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.ReportingPeriod
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQuery
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@PrsdbWebService
class PlausibleMetricsService(
    private val plausibleClient: PlausibleClient,
    @Value("\${plausible.site-id}") private val siteId: String,
) {
    fun getCompletionRates(period: ReportingPeriod): JourneyCompletionRatesDataModel =
        try {
            val response = plausibleClient.query(buildQuery(period))
            val pageViewsByPage =
                response.results
                    .filter { it.dimensions.isNotEmpty() && it.metrics.isNotEmpty() }
                    .associate { it.dimensions.first() to it.metrics.first() }
            JourneyCompletionRatesDataModel(
                landlordRegistration =
                    completionRate(pageViewsByPage, LANDLORD_REGISTRATION_START_PAGE_ROUTE, LANDLORD_REGISTRATION_CONFIRMATION_ROUTE),
                propertyRegistration =
                    completionRate(pageViewsByPage, PROPERTY_REGISTRATION_ROUTE, PROPERTY_REGISTRATION_CONFIRMATION_ROUTE),
                localCouncilUserRegistration =
                    completionRate(
                        pageViewsByPage,
                        LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE,
                        LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE,
                    ),
            )
        } catch (e: Exception) {
            println("Failed to fetch journey completion rates from Plausible: ${e.message}")
            JourneyCompletionRatesDataModel(null, null, null)
        }

    private fun buildQuery(period: ReportingPeriod): PlausibleQuery =
        PlausibleQuery(
            siteId = siteId,
            dateRange = listOf(period.start.toUkDate(), period.end.toUkDate()),
            metrics = listOf("pageviews"),
            dimensions = listOf("event:page"),
            filters = listOf(listOf("is", "event:page", ALL_PAGES)),
        )

    private fun completionRate(
        pageViewsByPage: Map<String, Double>,
        startPage: String,
        confirmationPage: String,
    ): Double? {
        val startPageViews = pageViewsByPage[startPage] ?: return null
        if (startPageViews == 0.0) return null
        val confirmationPageViews = pageViewsByPage[confirmationPage] ?: 0.0
        return BigDecimal
            .valueOf(confirmationPageViews / startPageViews * 100)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
            .coerceAtMost(100.0)
    }

    private fun Instant.toUkDate(): String = DateTimeFormatter.ISO_LOCAL_DATE.format(this.atZone(UK_ZONE).toLocalDate())

    companion object {
        private val UK_ZONE = ZoneId.of("Europe/London")

        const val PROPERTY_REGISTRATION_CONFIRMATION_ROUTE = "$PROPERTY_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT"
        const val LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE =
            "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$PRIVACY_NOTICE_PATH_SEGMENT"
        const val LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE =
            "$LOCAL_COUNCIL_USER_REGISTRATION_ROUTE/$CONFIRMATION_PATH_SEGMENT"

        private val ALL_PAGES =
            listOf(
                LANDLORD_REGISTRATION_START_PAGE_ROUTE,
                LANDLORD_REGISTRATION_CONFIRMATION_ROUTE,
                PROPERTY_REGISTRATION_ROUTE,
                PROPERTY_REGISTRATION_CONFIRMATION_ROUTE,
                LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE,
                LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE,
            )
    }
}
