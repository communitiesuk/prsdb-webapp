package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.util.Date

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

    @Transactional
    fun findOrCreateLandlordAndReturnRegistrationNumber(
        baseUserId: String,
        name: String,
        email: String,
        phoneNumber: String,
        addressDataModel: AddressDataModel,
        internationalAddress: String? = null,
        dateOfBirth: Date? = null,
    ): String {
        val existingLandlord = landlordRepository.findByBaseUser_Id(baseUserId)
        if (existingLandlord != null) {
            return RegistrationNumberDataModel.toString(existingLandlord.registrationNumber)
        }

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

        return RegistrationNumberDataModel.toString(registrationNumber)
    }

    fun searchForLandlords(
        searchTerm: String,
        limit: Int = DEFAULT_LANDLORD_SEARCH_LIMIT,
    ): List<Landlord> = landlordRepository.searchMatching(searchTerm, limit)

    companion object {
        private const val DEFAULT_LANDLORD_SEARCH_LIMIT = 3
    }
}
