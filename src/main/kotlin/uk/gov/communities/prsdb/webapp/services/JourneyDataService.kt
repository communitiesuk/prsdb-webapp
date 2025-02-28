package uk.gov.communities.prsdb.webapp.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import java.security.Principal

@Service
@RequestScope
class JourneyDataService(
    private val session: HttpSession,
    private val formContextRepository: FormContextRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
) {
    // This service can have class attributes as it is request scoped
    final lateinit var journeyDataKey: String private set

    fun getJourneyDataFromSession(journeyDataKey: String): JourneyData {
        if (this::journeyDataKey.isInitialized && journeyDataKey != this.journeyDataKey) {
            throw PrsdbWebException("journeyDataKey has already been set to ${this.journeyDataKey}")
        } else {
            this.journeyDataKey = journeyDataKey
        }
        return getJourneyDataFromSession()
    }

    fun getJourneyDataFromSession(): JourneyData = objectToStringKeyedMap(session.getAttribute(journeyDataKey)) ?: mapOf()

    fun setJourneyDataInSession(journeyData: JourneyData) {
        session.setAttribute(journeyDataKey, journeyData)
    }

    fun clearJourneyDataFromSession() {
        session.setAttribute(journeyDataKey, null)
    }

    fun getContextId(): Long? = session.getAttribute("contextId") as? Long

    fun getContextId(
        principalName: String,
        journeyType: JourneyType,
    ): Long? = formContextRepository.findByUser_IdAndJourneyType(principalName, journeyType)?.id

    fun setContextId(contextId: Long) {
        session.setAttribute("contextId", contextId)
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

    fun loadJourneyDataIntoSession(contextId: Long) {
        val formContext =
            formContextRepository
                .findById(contextId)
                .orElseThrow { IllegalStateException("FormContext with ID $contextId not found") }!!
        val loadedJourneyData =
            objectToStringKeyedMap(objectMapper.readValue(formContext.context, Any::class.java)) ?: mapOf()
        setJourneyDataInSession(loadedJourneyData)
        setContextId(contextId)
    }

    fun deleteJourneyData() {
        val contextId = getContextId() ?: return
        formContextRepository.deleteById(contextId)

        session.removeAttribute("contextId")
        clearJourneyDataFromSession()
    }
}
