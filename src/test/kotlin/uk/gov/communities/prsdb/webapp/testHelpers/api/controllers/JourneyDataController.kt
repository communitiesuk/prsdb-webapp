package uk.gov.communities.prsdb.webapp.testHelpers.api.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel

@Profile("local")
@RestController
@RequestMapping("/${JourneyDataController.SET_JOURNEY_DATA_ROUTE}")
class JourneyDataController(
    private val session: HttpSession,
) {
    @PostMapping(consumes = ["application/json"])
    fun index(
        @RequestBody requestBody: SetJourneyDataRequestModel,
    ) {
        session.setAttribute(requestBody.journeyDataKey, requestBody.journeyData)
    }

    companion object {
        const val SET_JOURNEY_DATA_ROUTE = "local/set-journey-data"
    }
}
