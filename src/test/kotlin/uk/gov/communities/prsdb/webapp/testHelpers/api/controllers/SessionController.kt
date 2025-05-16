package uk.gov.communities.prsdb.webapp.testHelpers.api.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.StoreInvitationTokenRequestModel

@Profile("local")
@RestController
@RequestMapping("/local")
class SessionController(
    private val session: HttpSession,
    private val invitationService: LocalAuthorityInvitationService,
) {
    @PostMapping("/$SET_JOURNEY_DATA_PATH_SEGMENT", consumes = ["application/json"])
    fun setJourneyData(
        @RequestBody requestBody: SetJourneyDataRequestModel,
    ) {
        session.setAttribute(requestBody.journeyDataKey, requestBody.getJourneyData())
    }

    @PostMapping("/$STORE_INVITATION_TOKEN_PATH_SEGMENT", consumes = ["application/json"])
    fun storeInvitationToken(
        @RequestBody requestBody: StoreInvitationTokenRequestModel,
    ) {
        invitationService.storeTokenInSession(requestBody.token)
    }

    companion object {
        const val SET_JOURNEY_DATA_PATH_SEGMENT = "set-journey-data"
        const val SET_JOURNEY_DATA_ROUTE = "local/$SET_JOURNEY_DATA_PATH_SEGMENT"

        const val STORE_INVITATION_TOKEN_PATH_SEGMENT = "store-token"
        const val STORE_INVITATION_TOKEN_ROUTE = "local/$STORE_INVITATION_TOKEN_PATH_SEGMENT"
    }
}
