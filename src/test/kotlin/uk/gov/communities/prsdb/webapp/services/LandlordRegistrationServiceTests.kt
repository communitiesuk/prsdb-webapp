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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationLandlord
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LandlordRegistrationServiceTests {
    @Mock
    private lateinit var mockLandlordService: LandlordService

    private lateinit var landlordRegistrationService: LandlordRegistrationService

    private val orgAddress = AddressDataModel(singleLineAddress = "1 Org St", postcode = "SW1A 1AA")
    private val trusteeAddress = AddressDataModel(singleLineAddress = "2 Trustee Rd", postcode = "W1 1AA")

    @BeforeEach
    fun setup() {
        landlordRegistrationService = LandlordRegistrationService(mockLandlordService)
    }

    private fun verifyCreatedOrganisationLandlord(
        baseUserId: () -> String = { any() },
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
        governingBodyMembers: () -> List<GoverningBodyMemberDataModel> = { any() },
    ) {
        verify(mockLandlordService).createOrganisationLandlord(
            baseUserId(),
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
            governingBodyMembers(),
        )
    }

    private fun registerOrganisationLandlord(
        baseUserId: String = "user-123",
        organisationTypes: List<OrgType> = listOf(OrgType.NONE),
        organisationHasCompanyNumber: Boolean = false,
        organisationHasCharityNumber: Boolean = false,
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
        landlordRegistrationService.registerLandlord(
            baseUserId = baseUserId,
            landlordType = LandlordType.ORGANISATION,
            organisationTypes = organisationTypes,
            organisationHasCompanyNumber = organisationHasCompanyNumber,
            organisationHasCharityNumber = organisationHasCharityNumber,
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
        @Test
        fun `registerLandlord calls createIndividualLandlord for INDIVIDUAL type`() {
            val individualAddress = AddressDataModel(singleLineAddress = "3 Home Lane", postcode = "E1 1AA")
            whenever(
                mockLandlordService.createIndividualLandlord(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(IndividualLandlord())

            landlordRegistrationService.registerLandlord(
                baseUserId = "user-123",
                landlordType = LandlordType.INDIVIDUAL,
                individualName = "John Smith",
                individualEmail = "john@test.com",
                individualPhoneNumber = "07123456789",
                individualAddress = individualAddress,
                individualDateOfBirth = LocalDate.of(1990, 1, 1),
                individualCountryOfResidence = "England",
                individualIsVerified = true,
                individualHasAcceptedPrivacyNotice = true,
            )

            verify(mockLandlordService).createIndividualLandlord(
                baseUserId = eq("user-123"),
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
    }

    @Nested
    inner class OrganisationLandlordRegistration {
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
                    any(),
                ),
            ).thenReturn(OrganisationLandlord())
        }

        @Test
        fun `registerLandlord passes company number when hasCompanyNumber is true`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.COMPANY),
                organisationHasCompanyNumber = true,
                organisationCompanyNumber = "12345678",
            )

            verifyCreatedOrganisationLandlord(companyNumber = { eq("12345678") })
        }

        @Test
        fun `registerLandlord passes null company number when hasCompanyNumber is false`() {
            registerOrganisationLandlord(
                organisationHasCompanyNumber = false,
                organisationCompanyNumber = "stale-data",
            )

            verifyCreatedOrganisationLandlord(companyNumber = { isNull() })
        }

        @Test
        fun `registerLandlord passes charity number when hasCharityNumber is true and regulator is not NONE`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.CHARITY),
                organisationHasCharityNumber = true,
                organisationCharityRegisteredWith = CharityRegulator.ENGLAND_AND_WALES,
                organisationCharityNumber = "1234567",
            )

            verifyCreatedOrganisationLandlord(
                charityRegisteredWith = { eq(CharityRegulator.ENGLAND_AND_WALES) },
                charityNumber = { eq("1234567") },
            )
        }

        @Test
        fun `registerLandlord passes null charity number when hasCharityNumber is true but regulator is NONE`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.CHARITY),
                organisationHasCharityNumber = true,
                organisationCharityRegisteredWith = CharityRegulator.NONE,
                organisationCharityNumber = "stale-data",
            )

            verifyCreatedOrganisationLandlord(
                charityRegisteredWith = { eq(CharityRegulator.NONE) },
                charityNumber = { isNull() },
            )
        }

        @Test
        fun `registerLandlord passes null charity fields when hasCharityNumber is false`() {
            registerOrganisationLandlord(
                organisationHasCharityNumber = false,
            )

            verifyCreatedOrganisationLandlord(
                charityRegisteredWith = { isNull() },
                charityNumber = { isNull() },
            )
        }

        @Test
        fun `registerLandlord passes lead trustee fields when org type includes TRUST`() {
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
        fun `registerLandlord passes null lead trustee fields when org type does not include TRUST`() {
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
        fun `registerLandlord passes governing body members when hasCompanyNumber is false`() {
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

            verifyCreatedOrganisationLandlord(governingBodyMembers = { eq(members) })
        }

        @Test
        fun `registerLandlord passes empty governing body members when hasCompanyNumber is true`() {
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

            verifyCreatedOrganisationLandlord(governingBodyMembers = { eq(emptyList()) })
        }

        @Test
        fun `registerLandlord derives isCompany, isCharity and isTrust from organisationTypes`() {
            registerOrganisationLandlord(
                organisationTypes = listOf(OrgType.COMPANY, OrgType.CHARITY),
            )

            verifyCreatedOrganisationLandlord(
                isCompany = { eq(true) },
                isCharity = { eq(true) },
                isTrust = { eq(false) },
            )
        }
    }
}
