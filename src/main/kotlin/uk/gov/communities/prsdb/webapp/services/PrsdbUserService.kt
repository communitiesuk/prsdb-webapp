package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository

@PrsdbWebService
class PrsdbUserService(
    private val prsdbUserRepository: PrsdbUserRepository,
) {
    @Transactional
    fun findOrCreatePrsdbUser(baseUserId: String): PrsdbUser =
        prsdbUserRepository.findByIdOrNull(baseUserId) ?: prsdbUserRepository.save(PrsdbUser(baseUserId))
}
