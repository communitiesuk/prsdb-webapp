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
    @Value("\${plausible.domain-id}") private val domainId: String,
) {
    fun getCompletionRates(period: ReportingPeriod): JourneyCompletionRatesDataModel =
        try {
            val response = plausibleClient.query(buildQuery(period))
            val metricsByPage =
                response.results
                    .filter { it.dimensions.isNotEmpty() && it.metrics.size >= METRICS.size }
                    .associate { it.dimensions.first() to it.metrics }
            val visitorsByPage = metricsByPage.mapValues { it.value[VISITORS_INDEX] }
            val pageViewsByPage = metricsByPage.mapValues { it.value[PAGE_VIEWS_INDEX] }
            JourneyCompletionRatesDataModel(
                landlordRegistration =
                    completionRate(visitorsByPage, LANDLORD_REGISTRATION_START_PAGE_ROUTE, LANDLORD_REGISTRATION_CONFIRMATION_ROUTE),
                propertyRegistration =
                    completionRate(pageViewsByPage, PROPERTY_REGISTRATION_ROUTE, PROPERTY_REGISTRATION_CONFIRMATION_ROUTE),
                localCouncilUserRegistration =
                    completionRate(
                        visitorsByPage,
                        LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE,
                        LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE,
                    ),
            )
        } catch (e: Exception) {
            println("Failed to fetch journey completion rates from Plausible: ${e.message}")
            JourneyCompletionRatesDataModel(null, null, null)
        }

    // TODO PDJB-1055: Add pagination handling for Plausible queries. This query is currently bounded to a fixed set of
    //  pages, but pagination will need to be addressed if a query ever returns more than 10,000 entries from Plausible.
    private fun buildQuery(period: ReportingPeriod): PlausibleQuery =
        PlausibleQuery(
            siteId = domainId,
            dateRange = listOf(period.start.toUkDate(), period.end.toUkDate()),
            metrics = METRICS,
            dimensions = listOf("event:page"),
            filters = listOf(listOf("is", "event:page", ALL_PAGES)),
        )

    private fun completionRate(
        countsByPage: Map<String, Double>,
        startPage: String,
        confirmationPage: String,
    ): Double? {
        val startCount = countsByPage[startPage] ?: return null
        if (startCount == 0.0) return null
        val confirmationCount = countsByPage[confirmationPage] ?: 0.0
        return BigDecimal
            .valueOf(confirmationCount / startCount * 100)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    private fun Instant.toUkDate(): String = DateTimeFormatter.ISO_LOCAL_DATE.format(this.atZone(UK_ZONE).toLocalDate())

    companion object {
        private val UK_ZONE = ZoneId.of("Europe/London")

        // Visitors are used for one-off-per-user journeys (landlord and local council registration); page views are
        // used for property registration, where a single landlord may register multiple properties.
        private val METRICS = listOf("visitors", "pageviews")
        private const val VISITORS_INDEX = 0
        private const val PAGE_VIEWS_INDEX = 1

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
