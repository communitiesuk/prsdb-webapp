package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.time.LocalDate

@Service
class LandlordService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val addressService: AddressService,
    private val registrationNumberService: RegistrationNumberService,
) {
    fun retrieveLandlordByRegNum(regNum: RegistrationNumberDataModel): Landlord? {
        if (regNum.type != RegistrationNumberType.LANDLORD) {
            throw IllegalArgumentException("Invalid registration number type")
        }
        return landlordRepository.findByRegistrationNumber_Number(regNum.number)
    }

    fun retrieveLandlordByBaseUserId(baseUserId: String): Landlord? = landlordRepository.findByBaseUser_Id(baseUserId)

    @Transactional
    fun createLandlord(
        baseUserId: String,
        name: String,
        email: String,
        phoneNumber: String,
        addressDataModel: AddressDataModel,
        internationalAddress: String? = null,
        dateOfBirth: LocalDate? = null,
    ) {
        val baseUser = oneLoginUserRepository.getReferenceById(baseUserId)
        val address = addressService.findOrCreateAddress(addressDataModel)
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        landlordRepository.save(
            Landlord(
                baseUser,
                name,
                email,
                phoneNumber,
                address,
                registrationNumber,
                internationalAddress,
                dateOfBirth,
            ),
        )
    }

    fun searchForLandlords(
        searchTerm: String,
        limit: Int = DEFAULT_LANDLORD_SEARCH_LIMIT,
    ): List<LandlordSearchResultDataModel> {
        RegistrationNumberDataModel.parseOrNull(searchTerm)?.let { registrationNumber ->
            if (registrationNumber.isType(RegistrationNumberType.LANDLORD)) {
                retrieveLandlordByRegNum(registrationNumber)?.let { landlord ->
                    return listOf(LandlordSearchResultDataModel.fromLandlord(landlord))
                }
            }
        }

        return landlordRepository
            .searchMatching(searchTerm, limit)
            .map { LandlordSearchResultDataModel.fromLandlord(it) }
    }

    // TODO PRSD-652: Change this once pagination has been implemented
    companion object {
        private const val DEFAULT_LANDLORD_SEARCH_LIMIT = 25
    }
}
