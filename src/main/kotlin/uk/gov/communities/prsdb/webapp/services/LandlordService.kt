package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

@Service
class LandlordService(
    val landlordRepository: LandlordRepository,
) {
    fun retrieveLandlordByRegNum(regNum: RegistrationNumberDataModel): Landlord? {
        if (regNum.type != RegistrationNumberType.LANDLORD) {
            throw IllegalArgumentException("Invalid registration number type")
        }
        return landlordRepository.findByRegistrationNumber_Number(regNum.number)
    }

    fun searchForLandlords(
        searchTerm: String,
        limit: Int = DEFAULT_LANDLORD_SEARCH_LIMIT,
    ): List<Landlord> = landlordRepository.searchMatching(searchTerm, limit)

    companion object {
        private const val DEFAULT_LANDLORD_SEARCH_LIMIT = 3
    }
}
