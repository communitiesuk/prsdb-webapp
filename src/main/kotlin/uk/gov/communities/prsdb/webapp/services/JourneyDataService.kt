package uk.gov.communities.prsdb.webapp.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import uk.gov.communities.prsdb.webapp.constants.CONTEXT_ID
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.objectToStringKeyedMap
import java.security.Principal

class JourneyDataService(
    private val session: HttpSession,
    private val formContextRepository: FormContextRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
    val journeyDataKey: String,
) {
    fun getJourneyDataFromSession(): JourneyData = objectToStringKeyedMap(session.getAttribute(journeyDataKey)) ?: mapOf()

    fun setJourneyDataInSession(journeyData: JourneyData) {
        session.setAttribute(journeyDataKey, journeyData)
    }

    fun addToJourneyDataIntoSession(newJourneyData: JourneyData) {
        val existingData = getJourneyDataFromSession()
        val updatedData = existingData + newJourneyData
        setJourneyDataInSession(updatedData)
    }

    fun removeJourneyDataAndContextIdFromSession() {
        session.removeAttribute(CONTEXT_ID)
        session.removeAttribute(journeyDataKey)
    }

    fun getContextId(): Long? = session.getAttribute(CONTEXT_ID) as? Long

    fun setContextId(contextId: Long) {
        session.setAttribute(CONTEXT_ID, contextId)
    }

    fun saveJourneyData(
        contextId: Long?,
        journeyData: JourneyData,
        journeyType: JourneyType,
        principal: Principal,
    ): Long {
        val formContext =
            if (contextId != null) {
                // Update existing FormContext
                val formContext =
                    formContextRepository
                        .findById(contextId)
                        .orElseThrow { IllegalStateException("FormContext with ID $contextId not found") }!!
                formContext.context = objectMapper.writeValueAsString(journeyData)
                formContext
            } else {
                // Create a new FormContext if one does not exist
                FormContext(
                    journeyType = journeyType,
                    context = objectMapper.writeValueAsString(journeyData),
                    user = oneLoginUserRepository.getReferenceById(principal.name),
                )
            }
        val savedFormContext = formContextRepository.save(formContext)
        setContextId(savedFormContext.id)
        return savedFormContext.id
    }

    fun loadJourneyDataIntoSession(formContext: FormContext) {
        setJourneyDataInSession(formContext.toJourneyData())
        setContextId(formContext.id)
    }

    fun loadJourneyDataIntoSession(contextId: Long) {
        val formContext =
            formContextRepository
                .findById(contextId)
                .orElseThrow { IllegalStateException("FormContext with ID $contextId not found") }!!
        loadJourneyDataIntoSession(formContext)
    }

    fun deleteJourneyData() {
        val contextId = getContextId()
        contextId?.let { formContextRepository.deleteById(it) }

        removeJourneyDataAndContextIdFromSession()
    }
}
