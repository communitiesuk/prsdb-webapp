package uk.gov.communities.prsdb.webapp.services

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.PageData
import uk.gov.communities.prsdb.webapp.forms.journeys.objectToStringKeyedMap
import java.security.Principal
import java.time.LocalDate

@Service
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class JourneyDataService(
    private val session: HttpSession,
    private val formContextRepository: FormContextRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
) {
    fun getJourneyDataFromSession(): JourneyData = objectToStringKeyedMap(session.getAttribute("journeyData")) ?: mutableMapOf()

    fun setJourneyData(journeyData: JourneyData) {
        session.setAttribute("journeyData", journeyData)
    }

    fun getContextId(): Long? = session.getAttribute("contextId") as? Long

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
        setContextId(savedFormContext.id!!)
        return savedFormContext.id
    }

    fun loadJourneyDataIntoSession(contextId: Long) {
        val formContext =
            formContextRepository
                .findById(contextId)
                .orElseThrow { IllegalStateException("FormContext with ID $contextId not found") }!!
        val loadedJourneyData =
            objectToStringKeyedMap(objectMapper.readValue(formContext.context, Any::class.java)) ?: mutableMapOf()
        setJourneyData(loadedJourneyData)
        setContextId(contextId)
    }

    fun deleteJourneyData() {
        val contextId = getContextId() ?: return
        formContextRepository.deleteById(contextId)

        session.removeAttribute("contextId")
        clearJourneyDataFromSession()
    }

    fun clearJourneyDataFromSession() {
        session.setAttribute("journeyData", null)
    }

    companion object {
        fun getPageData(
            journeyData: JourneyData,
            pageName: String,
            subPageNumber: Int? = null,
        ): PageData? {
            var pageData = objectToStringKeyedMap(journeyData[pageName])
            if (subPageNumber != null && pageData != null) {
                pageData = objectToStringKeyedMap(pageData[subPageNumber.toString()])
            }
            return pageData
        }

        fun getFieldStringValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): String? {
            val pageData = getPageData(journeyData, urlPathSegment, subPageNumber)
            return pageData?.get(fieldName)?.toString()
        }

        fun getFieldIntegerValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): Int? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString.toInt()
        }

        fun getFieldLocalDateValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): LocalDate? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString.let { LocalDate.parse(fieldAsString) }
        }

        fun getFieldBooleanValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): Boolean? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return fieldAsString == "true"
        }

        inline fun <reified E : Enum<E>> getFieldEnumValue(
            journeyData: JourneyData,
            urlPathSegment: String,
            fieldName: String,
            subPageNumber: Int? = null,
        ): E? {
            val fieldAsString =
                getFieldStringValue(journeyData, urlPathSegment, fieldName, subPageNumber) ?: return null
            return enumValueOf<E>(fieldAsString)
        }
    }
}
