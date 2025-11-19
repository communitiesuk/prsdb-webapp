package uk.gov.communities.prsdb.webapp.testHelpers.api.controllers

import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.StoreInvitationTokenRequestModel

@Profile("local")
@PrsdbRestController
@RequestMapping("/local")
class SessionController(
    private val session: HttpSession,
    private val invitationService: LocalCouncilInvitationService,
) {
    @PostMapping("/$SET_JOURNEY_DATA_PATH_SEGMENT", consumes = ["application/json"])
    fun setJourneyData(
        @RequestBody requestBody: SetJourneyDataRequestModel,
    ) {
        session.setAttribute(requestBody.journeyDataKey, requestBody.getJourneyState())
    }

    @PostMapping("/$SET_JOURNEY_STATE_PATH_SEGMENT", consumes = ["application/json"])
    fun setJourneyState(
        @RequestBody requestBody: SetJourneyDataRequestModel,
    ) {
        val keyToUse = "Any-olf-key"
        session.setAttribute("journeyStateKeyStore", mapOf(requestBody.journeyDataKey to keyToUse))
        val state = requestBody.getJourneyState()
        session.setAttribute(keyToUse, state)
    }

    @PostMapping("/$STORE_INVITATION_TOKEN_PATH_SEGMENT", consumes = ["application/json"])
    fun storeInvitationToken(
        @RequestBody requestBody: StoreInvitationTokenRequestModel,
    ) {
        invitationService.storeTokenInSession(requestBody.token)
    }

    companion object {
        const val SET_JOURNEY_DATA_PATH_SEGMENT = "set-journey-data"
        const val SET_JOURNEY_STATE_PATH_SEGMENT = "set-journey-state"
        const val SET_JOURNEY_DATA_ROUTE = "local/$SET_JOURNEY_DATA_PATH_SEGMENT"
        const val SET_JOURNEY_STATE_ROUTE = "local/$SET_JOURNEY_STATE_PATH_SEGMENT"

        const val STORE_INVITATION_TOKEN_PATH_SEGMENT = "store-token"
        const val STORE_INVITATION_TOKEN_ROUTE = "local/$STORE_INVITATION_TOKEN_PATH_SEGMENT"
    }
}
