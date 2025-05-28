package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository

@Service
class FormContextService(
    private val formContextRepository: FormContextRepository,
) {
    fun createEmptyFormContext(
        journeyType: JourneyType,
        baseUser: OneLoginUser,
    ) = formContextRepository.save(FormContext(journeyType, baseUser))

    fun deleteFormContext(formContext: FormContext) {
        formContextRepository.delete(formContext)
    }
}
