package uk.gov.communities.prsdb.webapp.journeys

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository

@PrsdbWebService
class JourneyStatePersistenceService(
    private val journeyRepository: SavedJourneyStateRepository,
    private val prsdbUserRepository: PrsdbUserRepository,
    private val objectMapper: ObjectMapper,
) {
    private val user: Authentication get() = SecurityContextHolder.getContext().authentication

    fun saveJourneyStateData(
        stateData: Any,
        journeyId: String,
    ): SavedJourneyState {
        val serializedState = objectMapper.writeValueAsString(stateData)
        val formContext =
            journeyRepository
                .findByJourneyIdAndUser_Id(journeyId, user.name)
                ?.also { it.serializedState = serializedState }
                ?: SavedJourneyState(
                    serializedState = serializedState,
                    user = prsdbUserRepository.getReferenceById(user.name),
                    journeyId = journeyId,
                )
        return journeyRepository.save(formContext)
    }

    fun retrieveJourneyStateData(journeyId: String): Any? =
        journeyRepository
            .findByJourneyIdAndUser_Id(journeyId, user.name)
            ?.let { objectMapper.readValue(it.serializedState, Any::class.java) }

    @Transactional
    fun deleteJourneyStateData(journeyId: String) {
        journeyRepository.deleteByJourneyIdAndUser_Id(journeyId, user.name)
    }
}
