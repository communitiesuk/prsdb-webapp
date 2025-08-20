package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordWithListedPropertyCountRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.LandlordSearchResultViewModel
import java.time.LocalDate
import kotlin.String

@PrsdbWebService
class LandlordService(
    private val landlordRepository: LandlordRepository,
    private val oneLoginUserService: OneLoginUserService,
    private val landlordWithListedPropertyCountRepository: LandlordWithListedPropertyCountRepository,
    private val addressService: AddressService,
    private val registrationNumberService: RegistrationNumberService,
    private val backLinkService: BackUrlStorageService,
    private val updateConfirmationSender: EmailNotificationService<LandlordUpdateConfirmation>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val registrationConfirmationSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>,
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
        isVerified: Boolean,
        hasAcceptedPrivacyNotice: Boolean,
        nonEnglandOrWalesAddress: String? = null,
        dateOfBirth: LocalDate? = null,
    ): Landlord {
        val baseUser = oneLoginUserService.findOrCreate1LUser(baseUserId)
        val address = addressService.findOrCreateAddress(addressDataModel)
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        val landlord =
            landlordRepository.save(
                Landlord(
                    baseUser,
                    name,
                    email,
                    phoneNumber,
                    address,
                    registrationNumber,
                    countryOfResidence,
                    isVerified,
                    hasAcceptedPrivacyNotice,
                    nonEnglandOrWalesAddress,
                    dateOfBirth,
                ),
            )

        registrationConfirmationSender.sendEmail(
            landlord.email,
            LandlordRegistrationConfirmationEmail(
                RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                absoluteUrlProvider.buildLandlordDashboardUri().toString(),
            ),
        )

        return landlord
    }

    @Transactional
    fun updateLandlordForBaseUserId(
        baseUserId: String,
        landlordUpdate: LandlordUpdateModel,
        checkUpdateIsValid: () -> Unit,
    ): Landlord {
        checkUpdateIsValid()
        val landlordEntity = retrieveLandlordByBaseUserId(baseUserId)!!

        val existingEmail = landlordEntity.email

        landlordUpdate.email?.let { landlordEntity.email = it }
        landlordUpdate.name?.let { landlordEntity.name = it }
        landlordUpdate.phoneNumber?.let { landlordEntity.phoneNumber = it }
        landlordUpdate.address?.let {
            landlordEntity.address = addressService.findOrCreateAddress(it)
        }
        landlordUpdate.dateOfBirth?.let { landlordEntity.dateOfBirth = it }

        sendUpdateConfirmationEmail(
            landlordUpdate,
            landlordEntity,
            existingEmail,
        )
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
                .map {
                    LandlordSearchResultViewModel.fromLandlordWithListedPropertyCount(
                        it,
                        backLinkService.storeCurrentUrlReturningKey(),
                    )
                },
            pageRequest,
            landlordPage.totalElements,
        )
    }

    fun getLandlordHasRegisteredProperties(baseUserId: String): Boolean {
        val landlordWithListedPropertyCount =
            landlordWithListedPropertyCountRepository.findByLandlord_BaseUser_Id(baseUserId)
                ?: throw EntityNotFoundException("Landlord with baseUserId $baseUserId not found")
        return landlordWithListedPropertyCount.listedPropertyCount > 0
    }

    private fun sendUpdateConfirmationEmail(
        landlordUpdate: LandlordUpdateModel,
        landlord: Landlord,
        oldEmail: String,
    ) {
        val updatedDetail =
            when {
                landlordUpdate.name != null -> "name"
                landlordUpdate.dateOfBirth != null -> "date of birth"
                landlordUpdate.email != null -> "email address"
                landlordUpdate.phoneNumber != null -> "telephone number"
                landlordUpdate.address != null -> "contact address"
                else -> null
            }

        val emails = listOf(landlord.email, oldEmail).distinct()

        updatedDetail?.let { detail ->
            emails.forEach { email ->
                updateConfirmationSender.sendEmail(
                    email,
                    LandlordUpdateConfirmation(
                        registrationNumber =
                            RegistrationNumberDataModel
                                .fromRegistrationNumber(landlord.registrationNumber)
                                .toString(),
                        dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
                        updatedDetail = detail,
                    ),
                )
            }
        }
    }
}
