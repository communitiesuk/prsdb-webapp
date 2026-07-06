package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController.Companion.PROPERTY_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQuery
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleQueryResponse
import uk.gov.communities.prsdb.webapp.models.dataModels.plausible.PlausibleResultRow
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService.Companion.LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService.Companion.LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE
import uk.gov.communities.prsdb.webapp.services.PlausibleMetricsService.Companion.PROPERTY_REGISTRATION_CONFIRMATION_ROUTE

@Profile("local")
@PrsdbRestController
@RequestMapping("/local/plausible")
class MockPlausibleController {
    @PostMapping("/api/v2/query")
    fun query(
        @RequestBody query: PlausibleQuery,
    ): PlausibleQueryResponse =
        when {
            query.dimensions.isNotEmpty() ->
                PlausibleQueryResponse(
                    results =
                        listOf(
                            row(LANDLORD_REGISTRATION_START_PAGE_ROUTE, visitors = 1000.0, pageViews = 1200.0),
                            row(LANDLORD_REGISTRATION_CONFIRMATION_ROUTE, visitors = 732.0, pageViews = 800.0),
                            row(PROPERTY_REGISTRATION_ROUTE, visitors = 60.0, pageViews = 80.0),
                            row(PROPERTY_REGISTRATION_CONFIRMATION_ROUTE, visitors = 18.0, pageViews = 20.0),
                            row(LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE, visitors = 3.0, pageViews = 4.0),
                            row(LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE, visitors = 1.0, pageViews = 2.0),
                        ),
                )
            // The Transaction-event count query filters on the custom event name.
            query.filters.toString().contains("event:name") ->
                PlausibleQueryResponse(
                    results = listOf(PlausibleResultRow(metrics = listOf(MOCK_TRANSACTION_EVENT_COUNT), dimensions = emptyList())),
                )
            // Otherwise this is the legacy Flow-event transaction-counts query (dimensionless, props filter).
            else ->
                PlausibleQueryResponse(
                    results = listOf(PlausibleResultRow(metrics = listOf(MOCK_FLOW_TRANSACTION_COUNT), dimensions = emptyList())),
                )
        }

    private fun row(
        page: String,
        visitors: Double,
        pageViews: Double,
    ) = PlausibleResultRow(metrics = listOf(visitors, pageViews), dimensions = listOf(page))

    companion object {
        private const val MOCK_FLOW_TRANSACTION_COUNT = 753.0
        private const val MOCK_TRANSACTION_EVENT_COUNT = 318.0
    }
}
