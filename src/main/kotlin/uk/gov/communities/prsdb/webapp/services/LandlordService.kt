package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordSearchResultDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.time.LocalDate

@Service
class LandlordService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserRepository: OneLoginUserRepository,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
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
        currentPageNumber: Int = 0,
        pageSize: Int = MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE,
    ): Page<LandlordSearchResultDataModel> {
        RegistrationNumberDataModel.parseOrNull(searchTerm)?.let { registrationNumber ->
            if (registrationNumber.isType(RegistrationNumberType.LANDLORD)) {
                retrieveLandlordByRegNum(registrationNumber)?.let { landlord ->
                    return PageImpl(
                        listOf(
                            LandlordSearchResultDataModel.fromLandlordWithListedProperties(
                                landlord,
                                propertyOwnershipRepository.countByPrimaryLandlord(landlord),
                            ),
                        ),
                    )
                }
            }
        }

        val pageRequest = PageRequest.of(currentPageNumber, pageSize)

        val landlords =
            landlordRepository
                .searchMatching(searchTerm, pageRequest)
        val listedPropertyCounts = propertyOwnershipRepository.countListedProperties(landlords.content.map { it.id })

        return landlordRepository
            .searchMatching(searchTerm, pageRequest)
            .map {
                LandlordSearchResultDataModel.fromLandlordWithListedProperties(
                    it,
                    propertyOwnershipRepository.countByPrimaryLandlord(it),
                )
            }
    }
}
