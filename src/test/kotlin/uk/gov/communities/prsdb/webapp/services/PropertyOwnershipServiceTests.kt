package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordType
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.OccupancyType
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationStatus
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.database.entity.Property
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createFiveDifferentProperties
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.RegisteredPropertyDataModel

@ExtendWith(MockitoExtension::class)
class PropertyOwnershipServiceTests {
    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockRegistrationNumberService: RegistrationNumberService

    @InjectMocks
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Test
    fun `createPropertyOwnership creates a property ownership`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val landlordType = LandlordType.SOLE
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val landlord = Landlord()
        val property = Property()
        val license = License()

        val expectedPropertyOwnership =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = license,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockPropertyOwnershipRepository.save(any(PropertyOwnership::class.java))).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            landlordType,
            ownershipType,
            households,
            tenants,
            landlord,
            property,
            license,
        )

        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(mockPropertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyOwnership).matches(propertyOwnershipCaptor.value))
    }

    @Test
    fun `createPropertyOwnership can create a property ownership with no license`() {
        val registrationNumber = RegistrationNumber(RegistrationNumberType.PROPERTY, 1233456)
        val landlordType = LandlordType.SOLE
        val ownershipType = OwnershipType.FREEHOLD
        val households = 1
        val tenants = 2
        val landlord = Landlord()
        val property = Property()

        val expectedPropertyOwnership =
            PropertyOwnership(
                occupancyType = OccupancyType.SINGLE_FAMILY_DWELLING,
                landlordType = landlordType,
                ownershipType = ownershipType,
                currentNumHouseholds = households,
                currentNumTenants = tenants,
                registrationNumber = registrationNumber,
                primaryLandlord = landlord,
                property = property,
                license = null,
            )

        whenever(mockRegistrationNumberService.createRegistrationNumber(RegistrationNumberType.PROPERTY)).thenReturn(
            registrationNumber,
        )
        whenever(mockPropertyOwnershipRepository.save(any(PropertyOwnership::class.java))).thenReturn(
            expectedPropertyOwnership,
        )

        propertyOwnershipService.createPropertyOwnership(
            landlordType,
            ownershipType,
            households,
            tenants,
            landlord,
            property,
        )

        val propertyOwnershipCaptor = captor<PropertyOwnership>()
        verify(mockPropertyOwnershipRepository).save(propertyOwnershipCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyOwnership).matches(propertyOwnershipCaptor.value))
    }

    @Nested
    inner class GetLandlordRegisteredPropertiesDetails {
        val landlord = createLandlord()

        val properties = createFiveDifferentProperties()

        @Test
        fun `Returns a list of Landlords properties  in correctly formatted data model`() {
            val propertyOwnership1 = createPropertyOwnership(primaryLandlord = landlord, property = properties[0])
            val propertyOwnership2 = createPropertyOwnership(primaryLandlord = landlord, property = properties[1])

            val landlordsProperties: List<PropertyOwnership> =
                listOf(propertyOwnership1, propertyOwnership2)

            val expectedResults: List<RegisteredPropertyDataModel> =
                listOf(
                    RegisteredPropertyDataModel(
                        "11 Example Road, EG1 2AB",
                        "P-CCCF-ZHXX",
                        "DERBYSHIRE DALES DISTRICT COUNCIL",
                        "Not Licenced",
                        "commonText.no",
                    ),
                    RegisteredPropertyDataModel(
                        "12 Example Road, EG1 2AB",
                        "P-CCCF-ZHXX",
                        "DERBYSHIRE DALES DISTRICT COUNCIL",
                        "Not Licenced",
                        "commonText.no",
                    ),
                )

            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    "landlord",
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(landlordsProperties)

            val result = propertyOwnershipService.getLandlordRegisteredPropertiesDetails("landlord")

            assertTrue(result.size == 2)
            assertEquals(expectedResults, result)
        }

        @Test
        fun `Returns correct isTenanted message key`() {
            val propertyOwnership1 = createPropertyOwnership(primaryLandlord = landlord, property = properties[0], currentNumTenants = 0)
            val propertyOwnership2 = createPropertyOwnership(primaryLandlord = landlord, property = properties[1], currentNumTenants = 1)
            val propertyOwnership3 = createPropertyOwnership(primaryLandlord = landlord, property = properties[2], currentNumTenants = 2)

            val landlordsProperties: List<PropertyOwnership> =
                listOf(propertyOwnership1, propertyOwnership2, propertyOwnership3)

            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    "landlord",
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(landlordsProperties)

            val result = propertyOwnershipService.getLandlordRegisteredPropertiesDetails("landlord")

            assertEquals(result[0].isTenanted, "commonText.no")
            assertEquals(result[1].isTenanted, "commonText.yes")
            assertEquals(result[2].isTenanted, "commonText.yes")
        }

        @Test
        fun `Returns correct licensing message key`() {
            val selectiveLicence = License(LicensingType.SELECTIVE_LICENCE, "testLicenseNumber")
            val hmoMandatoryLicence = License(LicensingType.HMO_MANDATORY_LICENCE, "testLicenseNumber")
            val hmoAdditionalLicence = License(LicensingType.HMO_ADDITIONAL_LICENCE, "testLicenseNumber")
            val noLicensingLicence = License(LicensingType.NO_LICENSING, "testLicenseNumber")
            val propertyOwnership1 =
                createPropertyOwnership(primaryLandlord = landlord, property = properties[0], license = selectiveLicence)
            val propertyOwnership2 =
                createPropertyOwnership(primaryLandlord = landlord, property = properties[1], license = hmoMandatoryLicence)
            val propertyOwnership3 =
                createPropertyOwnership(primaryLandlord = landlord, property = properties[2], license = hmoAdditionalLicence)
            val propertyOwnership4 =
                createPropertyOwnership(primaryLandlord = landlord, property = properties[3], license = noLicensingLicence)
            val propertyOwnership5 = createPropertyOwnership(primaryLandlord = landlord, property = properties[4], license = null)

            val landlordsProperties: List<PropertyOwnership> =
                listOf(propertyOwnership1, propertyOwnership2, propertyOwnership3, propertyOwnership4, propertyOwnership5)

            whenever(
                mockPropertyOwnershipRepository.findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status(
                    "landlord",
                    RegistrationStatus.REGISTERED,
                ),
            ).thenReturn(landlordsProperties)

            val result = propertyOwnershipService.getLandlordRegisteredPropertiesDetails("landlord")

            assertEquals(result[0].propertyLicence, "Selective licence")
            assertEquals(result[1].propertyLicence, "HMO licence")
            assertEquals(result[2].propertyLicence, "Additional licence")
            assertEquals(result[3].propertyLicence, "Not Licenced")
            assertEquals(result[4].propertyLicence, "Not Licenced")
        }
    }
}
