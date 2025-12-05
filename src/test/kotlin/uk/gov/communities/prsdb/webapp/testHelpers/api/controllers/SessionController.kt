package uk.gov.communities.prsdb.webapp.testHelpers.api.controllers

import jakarta.servlet.http.HttpSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.journeys.JourneyMetadata
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyDataRequestModel
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyStateRequestModel
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
        session.setAttribute(requestBody.journeyDataKey, requestBody.getJourneyData())
    }

    @PostMapping("/$SET_JOURNEY_STATE_PATH_SEGMENT", consumes = ["application/json"])
    fun setJourneyState(
        @RequestBody requestBody: SetJourneyStateRequestModel,
    ) {
        val keyToUse = "test-journey-key"
        session.setAttribute("journeyStateKeyStore", Json.encodeToString(mapOf(requestBody.journeyId to JourneyMetadata(keyToUse))))
        val state = requestBody.getJourneyState()
        session.setAttribute(keyToUse, state)

        if (JourneyStateService(session, requestBody.journeyId).journeyMetadata.dataKey != keyToUse) {
            throw IllegalStateException(
                "Failed to set journey state correctly - this is likely because the attribute keys have changed in JourneyStateService",
            )
        }
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
