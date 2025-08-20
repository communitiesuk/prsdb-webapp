package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.LandlordWithListedPropertyCount
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.Passcode
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import java.time.Instant
import java.time.LocalDate

class MockLandlordData {
    companion object {
        fun createAddress(
            singleLineAddress: String = "1 Example Road, EG1 2AB",
            localAuthority: LocalAuthority? = createLocalAuthority(),
            uprn: Long? = null,
        ) = Address(AddressDataModel(singleLineAddress = singleLineAddress, uprn = uprn), localAuthority)

        fun createOneLoginUser(id: String = "") = OneLoginUser(id)

        fun createLandlord(
            baseUser: OneLoginUser = createOneLoginUser(),
            name: String = "name",
            email: String = "example@email.com",
            phoneNumber: String = "07123456789",
            address: Address = createAddress(),
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 0L),
            countryOfResidence: String = ENGLAND_OR_WALES,
            isVerified: Boolean = true,
            hasAcceptedPrivacyNotice: Boolean = true,
            nonEnglandOrWalesAddress: String? = null,
            dateOfBirth: LocalDate? = null,
            createdDate: Instant = Instant.now(),
        ): Landlord {
            val landlord =
                Landlord(
                    baseUser = baseUser,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    address = address,
                    registrationNumber = registrationNumber,
                    countryOfResidence = countryOfResidence,
                    isVerified = isVerified,
                    hasAcceptedPrivacyNotice = hasAcceptedPrivacyNotice,
                    nonEnglandOrWalesAddress = nonEnglandOrWalesAddress,
                    dateOfBirth = dateOfBirth,
                )

            ReflectionTestUtils.setField(landlord, "createdDate", createdDate)

            return landlord
        }

        fun createLandlordWithListedPropertyCount(listedPropertyCount: Int = 0): LandlordWithListedPropertyCount {
            val landlord = createLandlord()
            return LandlordWithListedPropertyCount(
                landlord.id,
                landlord,
                listedPropertyCount,
            )
        }

        fun createProperty(
            status: RegistrationStatus = RegistrationStatus.REGISTERED,
            propertyType: PropertyType = PropertyType.FLAT,
            address: Address = createAddress(),
            isActive: Boolean = true,
        ) = Property(
            status = status,
            propertyType = propertyType,
            address = address,
            isActive = isActive,
        )

        fun createPropertyOwnership(
            occupancyType: OccupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
            ownershipType: OwnershipType = OwnershipType.FREEHOLD,
            currentNumHouseholds: Int = 0,
            currentNumTenants: Int = 0,
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
            primaryLandlord: Landlord = createLandlord(),
            property: Property = createProperty(),
            license: License? = null,
            incompleteComplianceForm: FormContext? = FormContext(JourneyType.PROPERTY_COMPLIANCE, primaryLandlord.baseUser),
            id: Long = 1,
            createdDate: Instant = Instant.now(),
        ): PropertyOwnership {
            val propertyOwnership =
                PropertyOwnership(
                    occupancyType = occupancyType,
                    ownershipType = ownershipType,
                    currentNumHouseholds = currentNumHouseholds,
                    currentNumTenants = currentNumTenants,
                    registrationNumber = registrationNumber,
                    primaryLandlord = primaryLandlord,
                    property = property,
                    incompleteComplianceForm = incompleteComplianceForm,
                    license = license,
                )

            ReflectionTestUtils.setField(propertyOwnership, "id", id)
            ReflectionTestUtils.setField(propertyOwnership, "createdDate", createdDate)

            return propertyOwnership
        }

        fun createPropertyRegistrationFormContext(
            journeyType: JourneyType = JourneyType.PROPERTY_REGISTRATION,
            context: String =
                "{\"lookup-address\":{\"houseNameOrNumber\":\"73\",\"postcode\":\"WC2R 1LA\"}," +
                    "\"looked-up-addresses\":\"[{\\\"singleLineAddress\\\":\\\"2, Example Road, EG\\\"," +
                    "\\\"localAuthorityId\\\":241,\\\"uprn\\\":2123456,\\\"buildingNumber\\\":\\\"2\\\"," +
                    "\\\"postcode\\\":\\\"EG\\\"}]\",\"select-address\":{\"address\":\"2, Example Road, EG\"}}",
            user: OneLoginUser = createOneLoginUser(),
            createdDate: Instant = Instant.now(),
            id: Long = 0,
        ): FormContext {
            val formContext = FormContext(journeyType, context, user)

            ReflectionTestUtils.setField(formContext, "createdDate", createdDate)
            ReflectionTestUtils.setField(formContext, "id", id)
            return formContext
        }

        fun createPropertyComplianceFormContext(
            journeyType: JourneyType = JourneyType.PROPERTY_COMPLIANCE,
            context: String =
                "{\"gas-safety-certificate\":{\"hasCert\":true}," +
                    "\"gas-safety-certificate-issue-date\":{\"day\":\"28\",\"month\":\"2\",\"year\":\"1990\"}," +
                    "\"gas-safety-certificate-outdated\":{},\"eicr\":{\"hasCert\":false}," +
                    "\"eicr-exemption\":{\"hasExemption\":false},\"eicr-exemption-missing\":{}}",
            user: OneLoginUser = createOneLoginUser(),
            createdDate: Instant = Instant.now(),
            id: Long = 0,
        ): FormContext {
            val formContext = FormContext(journeyType, context, user)

            ReflectionTestUtils.setField(formContext, "createdDate", createdDate)
            ReflectionTestUtils.setField(formContext, "id", id)
            return formContext
        }

        fun createPasscode(
            code: String = "ABCDEF",
            localAuthority: LocalAuthority = createLocalAuthority(),
            baseUser: OneLoginUser? = createOneLoginUser(),
        ) = Passcode(code, localAuthority, baseUser)
    }
}
