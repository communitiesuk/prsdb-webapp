package uk.gov.communities.prsdb.webapp.services

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinInstant
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant

@PrsdbWebService
class LegacyIncompletePropertyFormContextService(
    private val formContextRepository: FormContextRepository,
) {
    fun getIncompletePropertyFormContextForLandlordOrThrowNotFound(
        contextId: Long,
        principalName: String,
    ): FormContext =
        formContextRepository.findByIdAndUser_IdAndJourneyType(contextId, principalName, JourneyType.PROPERTY_REGISTRATION)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Form context with ID: $contextId and journey type: " +
                    "${JourneyType.PROPERTY_REGISTRATION.name} not found for base user: $principalName",
            )

    companion object {
        fun getIncompletePropertyCompleteByDate(createdDate: Instant): LocalDate {
            val createdDateInUk = DateTimeHelper.Companion.getDateInUK(createdDate.toKotlinInstant())
            return DateTimeHelper.Companion.get28DaysFromDate(createdDateInUk)
        }
    }

    fun getAllInDateIncompletePropertiesForLandlord(principalName: String): List<FormContext> =
        formContextRepository
            .findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION)
            .filter { formContext ->
                val completeByDate = getIncompletePropertyCompleteByDate(formContext.createdDate)
                !DateTimeHelper().isDateInPast(completeByDate)
            }

    fun getIncompletePropertyFormContextForLandlordIfNotExpired(
        contextId: Long,
        principalName: String,
    ): FormContext {
        val formContext = getIncompletePropertyFormContextForLandlordOrThrowNotFound(contextId, principalName)
        val completeByDate = getIncompletePropertyCompleteByDate(formContext.createdDate)

        if (DateTimeHelper().isDateInPast(completeByDate)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Complete by date for form context with ID: $contextId is in the past",
            )
        }
        return formContext
    }

    fun getFormContext(id: Long): FormContext? = formContextRepository.findById(id).orElse(null)

    fun deleteFormContext(formContext: FormContext) = formContextRepository.delete(formContext)
}
