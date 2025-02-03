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
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData.Companion.createLocalAuthority
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import java.time.Instant
import java.time.LocalDate

class MockLandlordData {
    companion object {
        fun createAddress(
            singleLineAddress: String = "1 Example Road, EG1 2AB",
            localAuthority: LocalAuthority? = createLocalAuthority(),
            uprn: Long? = null,
        ) = Address(AddressDataModel(singleLineAddress = singleLineAddress, uprn = uprn), localAuthority)

        fun createLandlord(
            baseUser: OneLoginUser = OneLoginUser(),
            name: String = "name",
            email: String = "example@email.com",
            phoneNumber: String = "07123456789",
            address: Address = createAddress(),
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.LANDLORD, 0L),
            internationalAddress: String? = null,
            dateOfBirth: LocalDate? = null,
            createdAt: Instant = Instant.now(),
        ): Landlord {
            val landlord: Landlord = mock()
            whenever(landlord.baseUser).thenReturn(baseUser)
            whenever(landlord.name).thenReturn(name)
            whenever(landlord.email).thenReturn(email)
            whenever(landlord.phoneNumber).thenReturn(phoneNumber)
            whenever(landlord.address).thenReturn(address)
            whenever(landlord.registrationNumber).thenReturn(registrationNumber)
            whenever(landlord.dateOfBirth).thenReturn(dateOfBirth)
            whenever(landlord.lastModifiedDate).thenReturn(Instant.now())
            whenever(landlord.internationalAddress).thenReturn(internationalAddress)
            whenever(landlord.createdDate).thenReturn(createdAt)

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
            occupancyType: OccupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
            landlordType: LandlordType = LandlordType.SOLE,
            ownershipType: OwnershipType = OwnershipType.FREEHOLD,
            currentNumHouseholds: Int = 0,
            currentNumTenants: Int = 0,
            registrationNumber: RegistrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456),
            primaryLandlord: Landlord = createLandlord(),
            property: Property = createProperty(),
            license: License? = null,
            createdDate: Instant = Instant.now(),
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
            createdDate = createdDate,
        )
    }
}
