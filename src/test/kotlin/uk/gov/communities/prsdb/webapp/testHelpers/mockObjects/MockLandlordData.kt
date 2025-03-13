package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.constants.ENGLAND_OR_WALES
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
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
                    nonEnglandOrWalesAddress = nonEnglandOrWalesAddress,
                    dateOfBirth = dateOfBirth,
                )

            ReflectionTestUtils.setField(landlord, "createdDate", createdDate)

            return landlord
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
            id: Long = 1,
            occupancyType: OccupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
            ownershipType: OwnershipType = OwnershipType.FREEHOLD,
            currentNumHouseholds: Int = 0,
            currentNumTenants: Int = 0,
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
            primaryLandlord: Landlord = createLandlord(),
            property: Property = createProperty(),
            license: License? = null,
            createdDate: Instant = Instant.now(),
        ): PropertyOwnership {
            val propertyOwnership =
                PropertyOwnership(
                    id = id,
                    occupancyType = occupancyType,
                    ownershipType = ownershipType,
                    currentNumHouseholds = currentNumHouseholds,
                    currentNumTenants = currentNumTenants,
                    registrationNumber = registrationNumber,
                    primaryLandlord = primaryLandlord,
                    property = property,
                    license = license,
                )

            ReflectionTestUtils.setField(propertyOwnership, "createdDate", createdDate)

            return propertyOwnership
        }
    }
}
