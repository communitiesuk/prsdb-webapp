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
import uk.gov.communities.prsdb.webapp.controllers.UpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateEpcController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLicensingController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentIncludesBillsController
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

    fun getTransactionCounts(period: ReportingPeriod): Long =
        try {
            val response = plausibleClient.query(buildTransactionQuery(period))
            (response.results.firstOrNull()?.metrics?.firstOrNull() ?: 0.0).toLong()
        } catch (e: Exception) {
            println("Failed to fetch transaction counts from Plausible: ${e.message}")
            0L
        }

    private fun buildTransactionQuery(period: ReportingPeriod): PlausibleQuery =
        PlausibleQuery(
            siteId = domainId,
            dateRange = listOf(period.start.toUkDate(), period.end.toUkDate()),
            metrics = listOf("events"),
            dimensions = emptyList(),
            filters = listOf(transactionFilter()),
        )

    // A transaction is a completed journey, counted from the Plausible "Flow" custom event (which carries the
    // server-side referrer and currentUrl paths as props). Each completion has a distinct (referrer, currentUrl)
    // signature. The seven single-step update journeys are deliberately excluded: their completion and a "Back"
    // click produce identical Flow props, so they cannot be counted without double-counting.
    private fun transactionFilter(): List<Any> =
        listOf(
            "or",
            listOf(
                // Registrations land on a unique confirmation URL.
                listOf("is", "event:props:currentUrl", REGISTRATION_CONFIRMATION_PAGES),
                // Invite joint landlord lands on its own confirmation URL (contains a property id).
                listOf("matches", "event:props:currentUrl", listOf(INVITE_JOINT_LANDLORD_CONFIRMATION_REGEX)),
                // Check-answers property updates: final check-answers step (referrer) -> property record (currentUrl).
                listOf(
                    "and",
                    listOf(
                        listOf("matches", "event:props:referrer", PROPERTY_UPDATE_REFERRER_REGEXES),
                        listOf("matches", "event:props:currentUrl", listOf(PROPERTY_RECORD_REGEX)),
                    ),
                ),
            ),
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

        // The three registration journeys each finish on a fixed confirmation URL with no path variables.
        private val REGISTRATION_CONFIRMATION_PAGES =
            listOf(
                LANDLORD_REGISTRATION_CONFIRMATION_ROUTE,
                PROPERTY_REGISTRATION_CONFIRMATION_ROUTE,
                LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE,
            )

        // A landlord lands back on the property record (which contains a numeric property id) after completing an update.
        private const val PROPERTY_RECORD_REGEX = "^/landlord/property-details/\\d+$"

        // The invite-joint-landlord journey finishes on its own confirmation URL nested under the property record.
        private const val INVITE_JOINT_LANDLORD_CONFIRMATION_REGEX =
            "^/landlord/property-details/\\d+/invite-joint-landlord/confirmation$"

        // The eight check-answers property updates are detected by their final check-answers step as the Flow referrer.
        // Segment literals are kept inline to avoid coupling to each update controller's check-answers step package.
        private val PROPERTY_UPDATE_REFERRER_REGEXES =
            listOf(
                updateReferrerRegex(UpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE, "check-gas-safety-answers"),
                updateReferrerRegex(UpdateElectricalSafetyController.UPDATE_ELECTRICAL_SAFETY_ROUTE, "check-electrical-safety-answers"),
                updateReferrerRegex(UpdateEpcController.UPDATE_EPC_ROUTE, "check-epc-answers"),
                updateReferrerRegex(UpdateOccupancyController.UPDATE_OCCUPANCY_ROUTE, "occupancy-check-your-answers"),
                updateReferrerRegex(UpdateLicensingController.UPDATE_LICENSING_ROUTE, "check-licensing-answers"),
                updateReferrerRegex(
                    UpdateRentFrequencyAndAmountController.UPDATE_RENT_FREQUENCY_AND_AMOUNT_ROUTE,
                    "rent-frequency-and-amount-check-your-answers",
                ),
                updateReferrerRegex(
                    UpdateRentIncludesBillsController.UPDATE_RENT_INCLUDES_BILLS_ROUTE,
                    "rent-includes-bills-check-your-answers",
                ),
                updateReferrerRegex(
                    UpdateHouseholdsAndTenantsController.UPDATE_HOUSEHOLDS_AND_TENANTS_ROUTE,
                    "households-tenants-check-your-answers",
                ),
            )

        private fun updateReferrerRegex(
            routeTemplate: String,
            cyaSegment: String,
        ): String = "^" + routeTemplate.replace("{propertyOwnershipId}", "\\d+") + "/" + cyaSegment + "$"
    }
}
