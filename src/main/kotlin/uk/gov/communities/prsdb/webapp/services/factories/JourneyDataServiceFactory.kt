package uk.gov.communities.prsdb.webapp.services.factories

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpSession
import org.springframework.web.context.annotation.RequestScope
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@PrsdbWebService
@RequestScope
class JourneyDataServiceFactory(
    private val session: HttpSession,
    private val formContextRepository: FormContextRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val objectMapper: ObjectMapper,
) {
    fun create(journeyDataKey: String) =
        JourneyDataService(session, formContextRepository, oneLoginUserRepository, objectMapper, journeyDataKey)
}
