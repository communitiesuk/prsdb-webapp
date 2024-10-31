package uk.gov.communities.prsdb.webapp.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.multipageforms.Journey
import uk.gov.communities.prsdb.webapp.multipageforms.JourneyData
import uk.gov.communities.prsdb.webapp.multipageforms.StepId
import java.security.Principal

@Service
class FormContextService(
    private val formContextRepository: FormContextRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
) {
    fun saveFormContext(
        contextId: Long?,
        journeyData: JourneyData,
        journey: Journey<StepId>,
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
                    journeyType = journey.journeyType,
                    context = objectMapper.writeValueAsString(journeyData),
                    user = oneLoginUserRepository.getReferenceById(principal.name),
                )
            }
        val savedFormContext = formContextRepository.save(formContext)
        return savedFormContext.id!!
    }
}
