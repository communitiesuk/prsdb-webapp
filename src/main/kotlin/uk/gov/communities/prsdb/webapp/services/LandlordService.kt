package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordWithListedPropertyCountRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.LandlordSearchResultViewModel
import java.time.LocalDate

@Service
class LandlordService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserService: OneLoginUserService,
    private val landlordWithListedPropertyCountRepository: LandlordWithListedPropertyCountRepository,
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

    fun retrieveLandlordById(id: Long): Landlord? = landlordRepository.findByIdOrNull(id)

    @Transactional
    fun createLandlord(
        baseUserId: String,
        name: String,
        email: String,
        phoneNumber: String,
        addressDataModel: AddressDataModel,
        countryOfResidence: String,
        nonEnglandOrWalesAddress: String? = null,
        dateOfBirth: LocalDate? = null,
    ): Landlord {
        val baseUser = oneLoginUserService.findOrCreate1LUser(baseUserId)
        val address = addressService.findOrCreateAddress(addressDataModel)
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        return landlordRepository.save(
            Landlord(
                baseUser,
                name,
                email,
                phoneNumber,
                address,
                registrationNumber,
                countryOfResidence,
                nonEnglandOrWalesAddress,
                dateOfBirth,
            ),
        )
    }

    @Transactional
    fun updateLandlordForBaseUserId(
        baseUserId: String,
        landlordUpdate: LandlordUpdateModel,
    ): Landlord {
        val landlordEntity = retrieveLandlordByBaseUserId(baseUserId)!!

        landlordUpdate.email?.let { landlordEntity.email = it }

        return landlordEntity
    }

    fun searchForLandlords(
        searchTerm: String,
        laBaseUserId: String,
        restrictToLA: Boolean = false,
        requestedPageIndex: Int = 0,
        pageSize: Int = MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE,
    ): Page<LandlordSearchResultViewModel> {
        val lrn = RegistrationNumberDataModel.parseTypeOrNull(searchTerm, RegistrationNumberType.LANDLORD)
        val pageRequest = PageRequest.of(requestedPageIndex, pageSize)

        val landlordPage =
            if (lrn == null) {
                landlordRepository.searchMatching(searchTerm, laBaseUserId, restrictToLA, pageRequest)
            } else {
                landlordRepository.searchMatchingLRN(lrn.number, laBaseUserId, restrictToLA, pageRequest)
            }

        return PageImpl(
            landlordWithListedPropertyCountRepository
                .findByLandlordIdIn(landlordPage.content.map { it.id })
                .map { LandlordSearchResultViewModel.fromLandlordWithListedPropertyCount(it) },
            pageRequest,
            landlordPage.totalElements,
        )
    }
}
