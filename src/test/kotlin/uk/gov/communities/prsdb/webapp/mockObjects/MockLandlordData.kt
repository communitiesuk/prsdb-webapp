package uk.gov.communities.prsdb.webapp.mockObjects

import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.LocalDate
import java.time.OffsetDateTime

class MockLandlordData {
    companion object {
        fun createAddress(
            singleLineAddress: String = "1 Example Road, EG1 2AB",
            custodianCode: String = "1045",
        ) = Address(AddressDataModel(singleLineAddress, custodianCode))

        fun createFiveDifferentAddresses(): List<Address> =
            listOf(
                createAddress(singleLineAddress = "11 Example Road, EG1 2AB"),
                createAddress(singleLineAddress = "12 Example Road, EG1 2AB"),
                createAddress(singleLineAddress = "13 Example Road, EG1 2AB"),
                createAddress(singleLineAddress = "14 Example Road, EG1 2AB"),
                createAddress(singleLineAddress = "15 Example Road, EG1 2AB"),
            )

        fun createLandlord(
            baseUser: OneLoginUser = OneLoginUser(),
            name: String = "name",
            email: String = "example@email.com",
            phoneNumber: String = "07123456789",
            address: Address = createAddress(),
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 0L),
            internationalAddress: String? = null,
            dateOfBirth: LocalDate? = null,
        ) = Landlord(
            baseUser = baseUser,
            name = name,
            email = email,
            phoneNumber = phoneNumber,
            address = address,
            registrationNumber = registrationNumber,
            internationalAddress = internationalAddress,
            dateOfBirth = dateOfBirth,
        )

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

        fun createFiveDifferentProperties(): List<Property> {
            val addresses = createFiveDifferentAddresses()

            val properties =
                listOf(
                    createProperty(address = addresses[0]),
                    createProperty(address = addresses[1]),
                    createProperty(address = addresses[2]),
                    createProperty(address = addresses[3]),
                    createProperty(address = addresses[4]),
                )

            return properties
        }

        fun createPropertyOwnership(
            occupancyType: OccupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
            landlordType: LandlordType = LandlordType.SOLE,
            ownershipType: OwnershipType = OwnershipType.FREEHOLD,
            currentNumHouseholds: Int = 0,
            currentNumTenants: Int = 0,
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
            primaryLandlord: Landlord = createLandlord(),
            property: Property = createProperty(),
            license: License? = null,
        ) = PropertyOwnership(
            occupancyType = occupancyType,
            landlordType = landlordType,
            ownershipType = ownershipType,
            currentNumHouseholds = currentNumHouseholds,
            currentNumTenants = currentNumTenants,
            registrationNumber = registrationNumber,
            primaryLandlord = primaryLandlord,
            property = property,
            license = license,
        )

        fun createMockNonUkLandlord(): Landlord {
            val landlord: Landlord = mock()
            whenever(landlord.baseUser).thenReturn(OneLoginUser())
            whenever(landlord.name).thenReturn("name")
            whenever(landlord.email).thenReturn("example@email.com")
            whenever(landlord.phoneNumber).thenReturn("07123456789")
            whenever(landlord.address).thenReturn(createAddress())
            whenever(landlord.registrationNumber).thenReturn(RegistrationNumber(RegistrationNumberType.LANDLORD, 0L))
            whenever(landlord.internationalAddress).thenReturn("1600 Pennsylvania Avenue, Washington DC, United States of America")
            whenever(landlord.dateOfBirth).thenReturn(LocalDate.now())
            whenever(landlord.lastModifiedDate).thenReturn(OffsetDateTime.now())
            whenever(landlord.createdDate).thenReturn(OffsetDateTime.now())

            return landlord
        }

        fun createMockLandlord(): Landlord {
            val landlord: Landlord = mock()
            whenever(landlord.baseUser).thenReturn(OneLoginUser())
            whenever(landlord.name).thenReturn("name")
            whenever(landlord.email).thenReturn("example@email.com")
            whenever(landlord.phoneNumber).thenReturn("07123456789")
            whenever(landlord.address).thenReturn(createAddress())
            whenever(landlord.registrationNumber).thenReturn(RegistrationNumber(RegistrationNumberType.LANDLORD, 0L))
            whenever(landlord.internationalAddress).thenReturn(null)
            whenever(landlord.dateOfBirth).thenReturn(LocalDate.now())
            whenever(landlord.lastModifiedDate).thenReturn(OffsetDateTime.now())
            whenever(landlord.createdDate).thenReturn(OffsetDateTime.now())

            return landlord
        }
    }
}
