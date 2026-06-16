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
        PlausibleQueryResponse(
            results =
                listOf(
                    row(LANDLORD_REGISTRATION_START_PAGE_ROUTE, 1000.0),
                    row(LANDLORD_REGISTRATION_CONFIRMATION_ROUTE, 732.0),
                    row(PROPERTY_REGISTRATION_ROUTE, 80.0),
                    row(PROPERTY_REGISTRATION_CONFIRMATION_ROUTE, 20.0),
                    row(LOCAL_COUNCIL_USER_REGISTRATION_PRIVACY_NOTICE_ROUTE, 3.0),
                    row(LOCAL_COUNCIL_USER_REGISTRATION_CONFIRMATION_ROUTE, 1.0),
                ),
        )

    private fun row(
        page: String,
        pageViews: Double,
    ) = PlausibleResultRow(metrics = listOf(pageViews), dimensions = listOf(page))
}
