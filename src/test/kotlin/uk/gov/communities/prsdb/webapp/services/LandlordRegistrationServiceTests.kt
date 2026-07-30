package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordRegistrationConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.OrganisationLandlordRegistrationConfirmationEmail
import java.net.URI
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LandlordRegistrationServiceTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    @Mock
    private lateinit var mockPrsdbUserService: PrsdbUserService

    @Mock
    private lateinit var mockOrganisationLandlordUserService: OrganisationLandlordUserService

    @Mock
    private lateinit var mockOrganisationGoverningBodyMemberService: OrganisationGoverningBodyMemberService

    @Mock
    private lateinit var mockRegistrationConfirmationSender: EmailNotificationService<LandlordRegistrationConfirmationEmail>

    @Mock
    private lateinit var mockOrgRegistrationConfirmationSender: EmailNotificationService<OrganisationLandlordRegistrationConfirmationEmail>

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    private lateinit var landlordRegistrationService: LandlordRegistrationService

    private val baseUser = PrsdbUser("user-123")

    private val orgAddress = AddressDataModel(singleLineAddress = "1 Org St", postcode = "SW1A 1AA")
    private val trusteeAddress = AddressDataModel(singleLineAddress = "2 Trustee Rd", postcode = "W1 1AA")

    @BeforeEach
    fun setup() {
        landlordRegistrationService =
            LandlordRegistrationService(
                mockLandlordService,
                mockPrsdbUserService,
                mockOrganisationLandlordUserService,
                mockOrganisationGoverningBodyMemberService,
                mockRegistrationConfirmationSender,
                mockOrgRegistrationConfirmationSender,
                mockAbsoluteUrlProvider,
            )
        whenever(mockPrsdbUserService.findOrCreatePrsdbUser("user-123")).thenReturn(baseUser)
    }

    private fun verifyCreatedOrganisationLandlord(
        organisationName: () -> String = { any() },
        organisationAddress: () -> AddressDataModel = { any() },
        organisationEmail: () -> String = { any() },
        organisationPhoneNumber: () -> String = { any() },
        isCompany: () -> Boolean = { any() },
        isCharity: () -> Boolean = { any() },
        isTrust: () -> Boolean = { any() },
        companyNumber: () -> String? = { anyOrNull() },
        charityRegisteredWith: () -> CharityRegulator? = { anyOrNull() },
        charityNumber: () -> String? = { anyOrNull() },
        leadTrusteeName: () -> String? = { anyOrNull() },
        leadTrusteeDateOfBirth: () -> LocalDate? = { anyOrNull() },
        leadTrusteeEmail: () -> String? = { anyOrNull() },
        leadTrusteePhoneNumber: () -> String? = { anyOrNull() },
        leadTrusteeAddress: () -> AddressDataModel? = { anyOrNull() },
        mainContactName: () -> String = { any() },
        mainContactEmail: () -> String = { any() },
        mainContactPhoneNumber: () -> String = { any() },
        registrantName: () -> String = { any() },
        registrantDateOfBirth: () -> LocalDate = { any() },
        registrantEmail: () -> String = { any() },
        registrantPhoneNumber: () -> String = { any() },
    ) {
        verify(mockLandlordService).createOrganisationLandlord(
            organisationName(),
            organisationAddress(),
            organisationEmail(),
            organisationPhoneNumber(),
            isCompany(),
            isCharity(),
            isTrust(),
            companyNumber(),
            charityRegisteredWith(),
            charityNumber(),
            leadTrusteeName(),
            leadTrusteeDateOfBirth(),
            leadTrusteeEmail(),
            leadTrusteePhoneNumber(),
            leadTrusteeAddress(),
            mainContactName(),
            mainContactEmail(),
            mainContactPhoneNumber(),
            registrantName(),
            registrantDateOfBirth(),
            registrantEmail(),
            registrantPhoneNumber(),
        )
    }

    private fun registerOrganisationLandlord(
        baseUserId: String = "user-123",
        organisationTypes: List<OrgType> = listOf(OrgType.NONE),
        organisationHasCompanyNumber: Boolean = false,
        orgIsRegisteredCharity: Boolean = false,
        organisationName: String = "Test Org",
        organisationAddress: AddressDataModel = orgAddress,
        organisationEmail: String = "org@test.com",
        organisationPhoneNumber: String = "020 1234 5678",
        organisationCompanyNumber: String? = null,
        organisationCharityRegisteredWith: CharityRegulator? = null,
        organisationCharityNumber: String? = null,
        organisationLeadTrusteeName: String? = null,
        organisationLeadTrusteeDateOfBirth: LocalDate? = null,
        organisationLeadTrusteeEmail: String? = null,
        organisationLeadTrusteePhoneNumber: String? = null,
        organisationLeadTrusteeAddress: AddressDataModel? = null,
        organisationMainContactName: String = "Bob",
        organisationMainContactEmail: String = "bob@test.com",
        organisationMainContactPhoneNumber: String = "071",
        organisationRegistrantName: String = "Alice",
        organisationRegistrantDateOfBirth: LocalDate = LocalDate.of(1990, 1, 1),
        organisationRegistrantEmail: String = "alice@test.com",
        organisationRegistrantPhoneNumber: String = "072",
        organisationGoverningBodyMembers: List<GoverningBodyMemberDataModel> = emptyList(),
    ) {
        landlordRegistrationService.registerOrganisationLandlord(
            baseUserId = baseUserId,
            organisationTypes = organisationTypes,
            organisationHasCompanyNumber = organisationHasCompanyNumber,
            orgIsRegisteredCharity = orgIsRegisteredCharity,
            organisationName = organisationName,
            organisationAddress = organisationAddress,
            organisationEmail = organisationEmail,
            organisationPhoneNumber = organisationPhoneNumber,
            organisationCompanyNumber = organisationCompanyNumber,
            organisationCharityRegisteredWith = organisationCharityRegisteredWith,
            organisationCharityNumber = organisationCharityNumber,
            organisationLeadTrusteeName = organisationLeadTrusteeName,
            organisationLeadTrusteeDateOfBirth = organisationLeadTrusteeDateOfBirth,
            organisationLeadTrusteeEmail = organisationLeadTrusteeEmail,
            organisationLeadTrusteePhoneNumber = organisationLeadTrusteePhoneNumber,
            organisationLeadTrusteeAddress = organisationLeadTrusteeAddress,
            organisationMainContactName = organisationMainContactName,
            organisationMainContactEmail = organisationMainContactEmail,
            organisationMainContactPhoneNumber = organisationMainContactPhoneNumber,
            organisationRegistrantName = organisationRegistrantName,
            organisationRegistrantDateOfBirth = organisationRegistrantDateOfBirth,
            organisationRegistrantEmail = organisationRegistrantEmail,
            organisationRegistrantPhoneNumber = organisationRegistrantPhoneNumber,
            organisationGoverningBodyMembers = organisationGoverningBodyMembers,
        )
    }

    @Nested
    inner class IndividualLandlordRegistration {
        private val registrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 1234567)
        private val individualLandlord =
            IndividualLandlord(
                baseUser = baseUser,
                name = "John Smith",
                email = "john@test.com",
                phoneNumber = "07123456789",
                address = Address(AddressDataModel(singleLineAddress = "3 Home Lane")),
                registrationNumber = registrationNumber,
                countryOfResidence = "England",
                isVerified = true,
                hasAcceptedPrivacyNotice = true,
                nonEnglandOrWalesAddress = null,
                dateOfBirth = LocalDate.of(1990, 1, 1),
            )
        private val dashboardUri = URI("http://example.com/landlord-dashboard")

        @BeforeEach
        fun stubIndividualLandlord() {
            whenever(
                mockLandlordService.createIndividualLandlord(
                    any(), any(), any(), any(), any(), any(), any(), any(), anyOrNull(), anyOrNull(),
                ),
            ).thenReturn(individualLandlord)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
        }

        @Test
        fun `registerIndividualLandlord calls createIndividualLandlord with resolved base user`() {
            val individualAddress = AddressDataModel(singleLineAddress = "3 Home Lane", postcode = "E1 1AA")

            landlordRegistrationService.registerIndividualLandlord(
                baseUserId = "user-123",
                name = "John Smith",
                email = "john@test.com",
                phoneNumber = "07123456789",
                address = individualAddress,
                dateOfBirth = LocalDate.of(1990, 1, 1),
                countryOfResidence = "England",
                isVerified = true,
                hasAcceptedPrivacyNotice = true,
            )

            verify(mockLandlordService).createIndividualLandlord(
                baseUser = eq(baseUser),
                name = eq("John Smith"),
                email = eq("john@test.com"),
                phoneNumber = eq("07123456789"),
                addressDataModel = eq(individualAddress),
                countryOfResidence = eq("England"),
                isVerified = eq(true),
                hasAcceptedPrivacyNotice = eq(true),
                nonEnglandOrWalesAddress = isNull(),
                dateOfBirth = eq(LocalDate.of(1990, 1, 1)),
            )
        }

        @Test
        fun `registerIndividualLandlord sends a registration confirmation email`() {
            landlordRegistrationService.registerIndividualLandlord(
                baseUserId = "user-123",
                name = "John Smith",
                email = "john@test.com",
                phoneNumber = "07123456789",
                address = AddressDataModel(singleLineAddress = "3 Home Lane", postcode = "E1 1AA"),
                dateOfBirth = LocalDate.of(1990, 1, 1),
                countryOfResidence = "England",
                isVerified = true,
                hasAcceptedPrivacyNotice = true,
            )

            verify(mockRegistrationConfirmationSender).sendEmail(
                eq("john@test.com"),
                eq(
                    LandlordRegistrationConfirmationEmail(
                        RegistrationNumberDataModel.fromRegistrationNumber(registrationNumber).toString(),
                        dashboardUri.toString(),
                    ),
                ),
            )
        }
    }

    @Nested
    inner class OrganisationLandlordRegistration {
        private val orgRegistrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 7654321)
        private val orgDashboardUri = URI("http://example.com/landlord-dashboard")

        private val organisationLandlord =
            OrganisationLandlord(
                registrationNumber = orgRegistrationNumber,
                name = "Test Org",
                address = Address(AddressDataModel(singleLineAddress = "1 Org St")),
                email = "org@test.com",
                phoneNumber = "020 1234 5678",
                registrantName = "Alice",
                registrantDateOfBirth = LocalDate.of(1990, 1, 1),
                registrantEmail = "alice@test.com",
                registrantPhoneNumber = "072",
                isCompany = false,
                isCharity = false,
                isTrust = false,
                companyNumber = null,
                charityRegisteredWith = null,
                charityNumber = null,
                leadTrusteeName = null,
                leadTrusteeDateOfBirth = null,
                leadTrusteeEmail = null,
                leadTrusteePhone = null,
                leadTrusteeAddress = null,
                mainContactName = "Bob",
                mainContactEmail = "bob@test.com",
                mainContactPhone = "071",
            )

        @BeforeEach
        fun stubOrganisationLandlord() {
            whenever(
                mockLandlordService.createOrganisationLandlord(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                ),
            ).thenReturn(organisationLandlord)
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(orgDashboardUri)
        }

        @Test
        fun `registerOrganisationLandlord passes company number when hasCompanyNumber is true`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.COMPANY),
                organisationHasCompanyNumber = true,
                organisationCompanyNumber = "12345678",
            )

            verifyCreatedOrganisationLandlord(companyNumber = { eq("12345678") })
        }

        @Test
        fun `registerOrganisationLandlord passes null company number when hasCompanyNumber is false`() {
            registerOrganisationLandlord(
                organisationHasCompanyNumber = false,
                organisationCompanyNumber = "stale-data",
            )

            verifyCreatedOrganisationLandlord(companyNumber = { isNull() })
        }

        @Test
        fun `registerOrganisationLandlord passes charity number when hasCharityNumber is true and regulator is not NONE`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.CHARITY),
                orgIsRegisteredCharity = true,
                organisationCharityRegisteredWith = CharityRegulator.ENGLAND_AND_WALES,
                organisationCharityNumber = "1234567",
            )

            verifyCreatedOrganisationLandlord(
                charityRegisteredWith = { eq(CharityRegulator.ENGLAND_AND_WALES) },
                charityNumber = { eq("1234567") },
            )
        }

        @Test
        fun `registerOrganisationLandlord passes null charity number when orgIsRegisteredCharity is true but regulator is NONE`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.CHARITY),
                orgIsRegisteredCharity = true,
                organisationCharityRegisteredWith = CharityRegulator.NONE,
                organisationCharityNumber = "stale-data",
            )

            verifyCreatedOrganisationLandlord(
                charityRegisteredWith = { eq(CharityRegulator.NONE) },
                charityNumber = { isNull() },
            )
        }

        @Test
        fun `registerOrganisationLandlord passes null charity fields when orgIsRegisteredCharity is false`() {
            registerOrganisationLandlord(
                orgIsRegisteredCharity = false,
            )

            verifyCreatedOrganisationLandlord(
                charityRegisteredWith = { isNull() },
                charityNumber = { isNull() },
            )
        }

        @Test
        fun `registerOrganisationLandlord passes lead trustee fields when org type includes TRUST`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.TRUST),
                organisationLeadTrusteeName = "Jane Trustee",
                organisationLeadTrusteeDateOfBirth = LocalDate.of(1980, 1, 1),
                organisationLeadTrusteeEmail = "trustee@test.com",
                organisationLeadTrusteePhoneNumber = "07999",
                organisationLeadTrusteeAddress = trusteeAddress,
            )

            verifyCreatedOrganisationLandlord(
                leadTrusteeName = { eq("Jane Trustee") },
                leadTrusteeDateOfBirth = { eq(LocalDate.of(1980, 1, 1)) },
                leadTrusteeEmail = { eq("trustee@test.com") },
                leadTrusteePhoneNumber = { eq("07999") },
                leadTrusteeAddress = { eq(trusteeAddress) },
            )
        }

        @Test
        fun `registerOrganisationLandlord passes null lead trustee fields when org type does not include TRUST`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.COMPANY),
                organisationHasCompanyNumber = true,
                organisationCompanyNumber = "12345678",
                organisationLeadTrusteeName = "stale trustee name",
                organisationLeadTrusteeEmail = "stale@test.com",
            )

            verifyCreatedOrganisationLandlord(
                leadTrusteeName = { isNull() },
                leadTrusteeDateOfBirth = { isNull() },
                leadTrusteeEmail = { isNull() },
                leadTrusteePhoneNumber = { isNull() },
                leadTrusteeAddress = { isNull() },
            )
        }

        @Test
        fun `registerOrganisationLandlord passes governing body members when hasCompanyNumber is false`() {
            val members =
                listOf(
                    GoverningBodyMemberDataModel(
                        name = "Director Dave",
                        type = GoverningBodyMemberType.DIRECTOR,
                        dateOfBirth = kotlinx.datetime.LocalDate(1970, 5, 12),
                        address = AddressDataModel(singleLineAddress = "20 Director Dr", postcode = "LS1 1AA"),
                    ),
                )
            registerOrganisationLandlord(
                organisationHasCompanyNumber = false,
                organisationGoverningBodyMembers = members,
            )

            verify(mockOrganisationGoverningBodyMemberService).createGoverningBodyMembers(any(), eq(members))
        }

        @Test
        fun `registerOrganisationLandlord does not create governing body members when hasCompanyNumber is true`() {
            val members =
                listOf(
                    GoverningBodyMemberDataModel(
                        name = "Stale Member",
                        type = GoverningBodyMemberType.DIRECTOR,
                        dateOfBirth = kotlinx.datetime.LocalDate(1970, 5, 12),
                        address = AddressDataModel(singleLineAddress = "20 Stale Dr", postcode = "LS1 1AA"),
                    ),
                )
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.COMPANY),
                organisationHasCompanyNumber = true,
                organisationCompanyNumber = "12345678",
                organisationGoverningBodyMembers = members,
            )

            verify(mockOrganisationGoverningBodyMemberService, never()).createGoverningBodyMembers(any(), any())
        }

        @Test
        fun `registerOrganisationLandlord derives isCompany, isCharity and isTrust from organisationTypes`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.COMPANY, OrgType.CHARITY),
            )

            verifyCreatedOrganisationLandlord(
                isCompany = { eq(true) },
                isCharity = { eq(true) },
                isTrust = { eq(false) },
            )
        }

        @Test
        fun `registerOrganisationLandlord creates an OrganisationLandlordUser`() {
            registerOrganisationLandlord()

            verify(mockOrganisationLandlordUserService).createOrganisationLandlordUser(any(), eq(baseUser))
        }

        @Test
        fun `registerOrganisationLandlord sends a registration confirmation email to the organisation email`() {
            registerOrganisationLandlord()

            verify(mockOrgRegistrationConfirmationSender).sendEmail(
                eq("org@test.com"),
                eq(
                    OrganisationLandlordRegistrationConfirmationEmail(
                        registrantName = "Alice",
                        organisationName = "Test Org",
                        lrn = RegistrationNumberDataModel.fromRegistrationNumber(orgRegistrationNumber).toString(),
                        prsdURL = orgDashboardUri.toString(),
                    ),
                ),
            )
        }
    }
}
