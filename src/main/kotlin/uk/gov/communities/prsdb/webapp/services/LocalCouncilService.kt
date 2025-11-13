package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.database.repository.LocalCouncilRepository

@PrsdbWebService
class LocalCouncilService(
    private val localCouncilRepository: LocalCouncilRepository,
) {
    fun retrieveLocalCouncilById(id: Int) = localCouncilRepository.getReferenceById(id)

    fun retrieveLocalAuthorityByCustodianCode(custodianCode: String) = localCouncilRepository.findByCustodianCode(custodianCode)

    fun retrieveAllLocalCouncils(): List<LocalCouncil> = localCouncilRepository.findAllByOrderByNameAsc()
}
