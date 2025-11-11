package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityRepository

@PrsdbWebService
class LocalCouncilService(
    private val localAuthorityRepository: LocalAuthorityRepository,
) {
    fun retrieveLocalAuthorityById(id: Int) = localAuthorityRepository.getReferenceById(id)

    fun retrieveLocalAuthorityByCustodianCode(custodianCode: String) = localAuthorityRepository.findByCustodianCode(custodianCode)

    fun retrieveAllLocalAuthorities(): List<LocalCouncil> = localAuthorityRepository.findAllByOrderByNameAsc()
}
