package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository

@PrsdbWebService
class OneLoginUserService(
    private val oneLoginUserRepository: OneLoginUserRepository,
) {
    @Transactional
    fun findOrCreate1LUser(baseUserId: String): OneLoginUser =
        oneLoginUserRepository.findByIdOrNull(baseUserId) ?: oneLoginUserRepository.save(OneLoginUser(baseUserId))
}
