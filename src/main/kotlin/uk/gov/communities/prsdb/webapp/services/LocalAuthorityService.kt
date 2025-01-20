package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository

@Service
class LocalAuthorityService(
    private val localAuthorityRepository: LocalAuthorityRepository,
) {
    fun retrieveLocalAuthorityById(id: Int) = localAuthorityRepository.getReferenceById(id)

    fun retrieveLocalAuthorityByCustodianCode(custodianCode: String) = localAuthorityRepository.findByCustodianCode(custodianCode)

    fun retrieveAllLocalAuthorities(): List<LocalAuthority> = localAuthorityRepository.findAll()
}
