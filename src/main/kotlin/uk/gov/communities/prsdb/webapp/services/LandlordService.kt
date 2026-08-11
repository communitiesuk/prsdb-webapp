package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.dao.QueryTimeoutException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationLandlordRepository
import uk.gov.communities.prsdb.webapp.exceptions.RepositoryQueryTimeoutException
import uk.gov.communities.prsdb.webapp.helpers.extensions.StringExtensions.Companion.toNormalizedEmail
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.IndividualLandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.OrganisationLandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.IndividualLandlordUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationalLandlordUpdateConfirmation
import uk.gov.communities.prsdb.webapp.models.viewModels.searchResultModels.LandlordSearchResultViewModel
import java.time.LocalDate
import kotlin.String

/**
 * Given you have a reference to a landlord, perform actions on it
 */
@PrsdbWebService
class LandlordService(
    private val individualLandlordRepository: IndividualLandlordRepository,
    private val organisationLandlordRepository: OrganisationLandlordRepository,
    private val landlordRepository: LandlordRepository,
    private val userToLandlordService: UserToLandlordService,
    private val addressService: AddressService,
    private val registrationNumberService: RegistrationNumberService,
    private val backLinkService: BackUrlStorageService,
    private val individualUpdateConfirmationSender: EmailNotificationService<IndividualLandlordUpdateConfirmation>,
    private val orgUpdateConfirmationSender: EmailNotificationService<OrganisationalLandlordUpdateConfirmation>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val organisationGoverningBodyMemberService: OrganisationGoverningBodyMemberService,
) {
    fun retrieveLandlordById(id: Long): Landlord? = landlordRepository.findById(id).orElse(null)

    @Transactional
    fun createIndividualLandlord(
        baseUser: PrsdbUser,
        name: String,
        email: String,
        phoneNumber: String,
        addressDataModel: AddressDataModel,
        countryOfResidence: String,
        isVerified: Boolean,
        hasAcceptedPrivacyNotice: Boolean,
        nonEnglandOrWalesAddress: String? = null,
        dateOfBirth: LocalDate,
    ): IndividualLandlord {
        val address = addressService.findOrCreateAddress(addressDataModel)
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        return individualLandlordRepository.save(
            IndividualLandlord(
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
    }

    @Transactional
    fun createOrganisationLandlord(
        organisationName: String,
        organisationAddress: AddressDataModel,
        organisationEmail: String,
        organisationPhoneNumber: String,
        isCompany: Boolean,
        isCharity: Boolean,
        isTrust: Boolean,
        companyNumber: String?,
        charityRegisteredWith: CharityRegulator?,
        charityNumber: String?,
        leadTrusteeName: String?,
        leadTrusteeDateOfBirth: LocalDate?,
        leadTrusteeEmail: String?,
        leadTrusteePhoneNumber: String?,
        leadTrusteeAddress: AddressDataModel?,
        mainContactName: String,
        mainContactEmail: String,
        mainContactPhoneNumber: String,
        registrantName: String,
        registrantDateOfBirth: LocalDate,
        registrantEmail: String,
        registrantPhoneNumber: String,
    ): OrganisationalLandlord {
        val orgAddress = addressService.findOrCreateAddress(organisationAddress)
        val trusteeAddress = leadTrusteeAddress?.let { addressService.findOrCreateAddress(it) }
        val registrationNumber = registrationNumberService.createRegistrationNumber(RegistrationNumberType.LANDLORD)

        val landlord =
            OrganisationalLandlord(
                registrationNumber = registrationNumber,
                name = organisationName,
                address = orgAddress,
                email = organisationEmail,
                phoneNumber = organisationPhoneNumber,
                registrantName = registrantName,
                registrantDateOfBirth = registrantDateOfBirth,
                registrantEmail = registrantEmail,
                registrantPhoneNumber = registrantPhoneNumber,
                isCompany = isCompany,
                isCharity = isCharity,
                isTrust = isTrust,
                companyNumber = companyNumber,
                charityRegisteredWith = charityRegisteredWith,
                charityNumber = charityNumber,
                leadTrusteeName = leadTrusteeName,
                leadTrusteeDateOfBirth = leadTrusteeDateOfBirth,
                leadTrusteeEmail = leadTrusteeEmail,
                leadTrusteePhone = leadTrusteePhoneNumber,
                leadTrusteeAddress = trusteeAddress,
                mainContactName = mainContactName,
                mainContactEmail = mainContactEmail,
                mainContactPhone = mainContactPhoneNumber,
            )

        return organisationLandlordRepository.save(landlord)
    }

    @Transactional
    fun updateIndividualLandlordForUser(
        landlordUpdate: IndividualLandlordUpdateModel,
        checkUpdateIsValid: () -> Unit,
    ): Landlord {
        checkUpdateIsValid()
        val landlordEntity = userToLandlordService.getCurrentLandlordForUser()
        check(landlordEntity is IndividualLandlord)

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

    @Transactional
    fun updateLandlordEmail(email: String) {
        updateIndividualLandlordForUser(
            IndividualLandlordUpdateModel(email = email),
        ) {}
    }

    @Transactional
    fun updateLandlordPhoneNumber(phoneNumber: String) {
        updateIndividualLandlordForUser(
            IndividualLandlordUpdateModel(phoneNumber = phoneNumber),
        ) {}
    }

    @Transactional
    fun updateLandlordName(name: String) {
        updateIndividualLandlordForUser(
            IndividualLandlordUpdateModel(name = name),
        ) {}
    }

    @Transactional
    fun updateLandlordAddress(address: AddressDataModel) {
        updateIndividualLandlordForUser(
            IndividualLandlordUpdateModel(address = address),
        ) {}
    }

    @Transactional
    fun updateLandlordDateOfBirth(dateOfBirth: LocalDate) {
        updateIndividualLandlordForUser(
            IndividualLandlordUpdateModel(
                email = null,
                name = null,
                phoneNumber = null,
                address = null,
                dateOfBirth = dateOfBirth,
            ),
        ) {}
    }

    @Transactional
    fun updateOrganisationLandlordForUser(orgLandlordUpdate: OrganisationLandlordUpdateModel): Landlord {
        val landlordEntity = userToLandlordService.getCurrentOrganisationLandlordForUser()

        orgLandlordUpdate.name?.let { landlordEntity.name = it }
        orgLandlordUpdate.isCompany?.let { landlordEntity.isCompany = it }
        orgLandlordUpdate.isCharity?.let { landlordEntity.isCharity = it }
        orgLandlordUpdate.isTrust?.let { isTrust ->
            landlordEntity.isTrust = isTrust
            if (!isTrust) {
                landlordEntity.leadTrusteeName = null
                landlordEntity.leadTrusteeDateOfBirth = null
                landlordEntity.leadTrusteeEmail = null
                landlordEntity.leadTrusteePhone = null
                landlordEntity.leadTrusteeAddress = null
            }
        }

        orgLandlordUpdate.leadTrusteeName?.let { landlordEntity.leadTrusteeName = it }
        orgLandlordUpdate.leadTrusteeDateOfBirth?.let { landlordEntity.leadTrusteeDateOfBirth = it }
        orgLandlordUpdate.leadTrusteeEmail?.let { landlordEntity.leadTrusteeEmail = it }
        orgLandlordUpdate.leadTrusteePhone?.let { landlordEntity.leadTrusteePhone = it }
        orgLandlordUpdate.leadTrusteeAddress?.let {
            landlordEntity.leadTrusteeAddress = addressService.findOrCreateAddress(it)
        }

        orgLandlordUpdate.mainContactName?.let { landlordEntity.mainContactName = it }
        orgLandlordUpdate.mainContactEmail?.let { landlordEntity.mainContactEmail = it }
        orgLandlordUpdate.mainContactPhone?.let { landlordEntity.mainContactPhone = it }

        return landlordEntity
    }

    @Transactional
    fun updateOrganisationLandlordName(orgName: String) {
        val landlord =
            updateOrganisationLandlordForUser(
                OrganisationLandlordUpdateModel(name = orgName),
            )
        sendOrgUpdateConfirmationEmail(landlord.email, "organisation name")
    }

    @Transactional
    fun updateOrganisationalLandlordToRegisteredCompany(companyNumber: String) {
        val landlordEntity = userToLandlordService.getCurrentOrganisationLandlordForUser()
        landlordEntity.companyNumber = companyNumber
        organisationGoverningBodyMemberService.clearGoverningBodyMembers(landlordEntity)
        sendOrgUpdateConfirmationEmail(landlordEntity.email, "company registration information")
    }

    @Transactional
    fun updateOrganisationalLandlordToNonRegisteredCompany(governingBodyMembers: List<GoverningBodyMemberDataModel>) {
        val landlordEntity = userToLandlordService.getCurrentOrganisationLandlordForUser()
        landlordEntity.companyNumber = null
        organisationGoverningBodyMemberService.createGoverningBodyMembers(landlordEntity, governingBodyMembers)
        sendOrgUpdateConfirmationEmail(landlordEntity.email, "company registration information and governing body details")
    }

    @Transactional
    fun updateOrganisationLandlordMainContact(
        name: String,
        email: String,
        phone: String,
    ) {
        val landlord =
            updateOrganisationLandlordForUser(
                OrganisationLandlordUpdateModel(
                    mainContactName = name,
                    mainContactEmail = email,
                    mainContactPhone = phone,
                ),
            )

        sendOrgUpdateConfirmationEmail(landlord.email, "main contact")
    }

    @Transactional
    fun updateOrganisationLandlordType(
        isCompany: Boolean,
        isCharity: Boolean,
        isTrust: Boolean,
    ) {
        val landlord =
            updateOrganisationLandlordForUser(
                OrganisationLandlordUpdateModel(
                    isCompany = isCompany,
                    isCharity = isCharity,
                    isTrust = isTrust,
                ),
            )

        sendOrgUpdateConfirmationEmail(landlord.email, "organisation type")
    }

    @Transactional
    fun updateOrganisationLandlordTypeAndLeadTrustee(
        isCompany: Boolean,
        isCharity: Boolean,
        isTrust: Boolean,
        leadTrusteeName: String? = null,
        leadTrusteeDateOfBirth: LocalDate? = null,
        leadTrusteeEmail: String? = null,
        leadTrusteePhone: String? = null,
        leadTrusteeAddress: AddressDataModel? = null,
    ) {
        val landlord =
            updateOrganisationLandlordForUser(
                OrganisationLandlordUpdateModel(
                    isCompany = isCompany,
                    isCharity = isCharity,
                    isTrust = isTrust,
                    leadTrusteeName = leadTrusteeName,
                    leadTrusteeDateOfBirth = leadTrusteeDateOfBirth,
                    leadTrusteeEmail = leadTrusteeEmail,
                    leadTrusteePhone = leadTrusteePhone,
                    leadTrusteeAddress = leadTrusteeAddress,
                ),
            )

        sendOrgUpdateConfirmationEmail(landlord.email, "organisation type and lead trustee details")
    }

    @Transactional
    fun updateOrganisationLandlordLeadTrustee(
        name: String,
        dateOfBirth: LocalDate,
        email: String,
        phone: String,
        addressDataModel: AddressDataModel,
    ) {
        val landlord =
            updateOrganisationLandlordForUser(
                OrganisationLandlordUpdateModel(
                    leadTrusteeName = name,
                    leadTrusteeDateOfBirth = dateOfBirth,
                    leadTrusteeEmail = email,
                    leadTrusteePhone = phone,
                    leadTrusteeAddress = addressDataModel,
                ),
            )

        sendOrgUpdateConfirmationEmail(landlord.email, "lead trustee details")
    }

    fun searchForLandlords(
        searchTerm: String,
        localCouncilBaseUserId: String,
        restrictToLocalCouncil: Boolean = false,
        requestedPageIndex: Int = 0,
        pageSize: Int = MAX_ENTRIES_IN_LANDLORDS_SEARCH_PAGE,
    ): Page<LandlordSearchResultViewModel> {
        val lrn = RegistrationNumberDataModel.parseTypeOrNull(searchTerm, RegistrationNumberType.LANDLORD)
        val pageRequest = PageRequest.of(requestedPageIndex, pageSize)

        val landlordPage =
            try {
                if (lrn == null) {
                    individualLandlordRepository.searchMatching(
                        searchTerm,
                        localCouncilBaseUserId,
                        restrictToLocalCouncil,
                        pageRequest,
                    )
                } else {
                    individualLandlordRepository.searchMatchingLRN(
                        lrn.number,
                        localCouncilBaseUserId,
                        restrictToLocalCouncil,
                        pageRequest,
                    )
                }
            } catch (_: QueryTimeoutException) {
                throw RepositoryQueryTimeoutException("Landlord search with query '$searchTerm' timed out")
            }

        return landlordPage.map {
            LandlordSearchResultViewModel.fromDataModel(
                it,
                backLinkService.storeCurrentUrlReturningKey(),
            )
        }
    }

    private fun sendUpdateConfirmationEmail(
        landlordUpdate: IndividualLandlordUpdateModel,
        landlord: IndividualLandlord,
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

        val emails = listOf(landlord.email, oldEmail).distinctBy { it.toNormalizedEmail() }

        updatedDetail?.let { detail ->
            emails.forEach { email ->
                individualUpdateConfirmationSender.sendEmail(
                    email,
                    IndividualLandlordUpdateConfirmation(
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

    private fun sendOrgUpdateConfirmationEmail(
        emailAddress: String,
        updatedDetail: String,
    ) {
        orgUpdateConfirmationSender.sendEmail(
            emailAddress,
            OrganisationalLandlordUpdateConfirmation(
                dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
                updatedDetail = "The $updatedDetail.",
            ),
        )
    }
}
