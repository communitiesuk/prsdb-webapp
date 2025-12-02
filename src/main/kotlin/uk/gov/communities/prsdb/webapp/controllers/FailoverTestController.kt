package uk.gov.communities.prsdb.webapp.controllers

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbController
import uk.gov.communities.prsdb.webapp.constants.FAILOVER_TEST_ENDPOINTS
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_BASE_URL

@PrsdbController
@RequestMapping
class FailoverTestController {
    @AvailableWhenFeatureEnabled(FAILOVER_TEST_ENDPOINTS)
    @GetMapping(ERROR_501_URL_ROUTE)
    fun failover501(): Unit =
        throw ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Endpoint should throw 501 error",
        )

    @AvailableWhenFeatureEnabled(FAILOVER_TEST_ENDPOINTS)
    @GetMapping(ERROR_502_URL_ROUTE)
    fun failover502(): Unit =
        throw ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "Endpoint should throw 502 error",
        )

    @AvailableWhenFeatureEnabled(FAILOVER_TEST_ENDPOINTS)
    @GetMapping(ERROR_503_URL_ROUTE)
    fun failover503(): Unit =
        throw ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Endpoint should throw 503 error",
        )

    @AvailableWhenFeatureEnabled(FAILOVER_TEST_ENDPOINTS)
    @GetMapping(ERROR_504_URL_ROUTE)
    fun failover504(): Unit =
        throw ResponseStatusException(
            HttpStatus.GATEWAY_TIMEOUT,
            "Endpoint should throw 504 error",
        )

    companion object {
        const val ERROR_501_URL_ROUTE = "${LANDLORD_BASE_URL}/throw-501"
        const val ERROR_502_URL_ROUTE = "${LANDLORD_BASE_URL}/throw-502"
        const val ERROR_503_URL_ROUTE = "${LANDLORD_BASE_URL}/throw-503"
        const val ERROR_504_URL_ROUTE = "${LANDLORD_BASE_URL}/throw-504"
    }
}
