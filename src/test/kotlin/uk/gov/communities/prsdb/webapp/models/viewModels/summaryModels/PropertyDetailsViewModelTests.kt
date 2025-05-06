package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createProperty
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

class PropertyDetailsViewModelTests {
    @Test
    fun `Key details are in the correct order`() {
        // Arrange
        val propertyOwnership = createPropertyOwnership()

        val expectedHeaderList =
            listOf(
                "propertyDetails.keyDetails.registeredLandlord",
                "propertyDetails.keyDetails.isTenanted",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.keyDetails.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Property details are in the correct order`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                property =
                    createProperty(
                        address = createAddress(uprn = 1234.toLong()),
                    ),
                currentNumTenants = 2,
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.registrationDate",
                "propertyDetails.propertyRecord.registrationNumber",
                "propertyDetails.propertyRecord.address",
                "propertyDetails.propertyRecord.uprn",
                "propertyDetails.propertyRecord.localAuthority",
                "propertyDetails.propertyRecord.propertyType",
                "propertyDetails.propertyRecord.ownershipType",
                "propertyDetails.propertyRecord.licensingType",
                "propertyDetails.propertyRecord.occupied",
                "propertyDetails.propertyRecord.numberOfHouseholds",
                "propertyDetails.propertyRecord.numberOfPeople",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.propertyRecord.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `isTenantedKey returns the correct value in keyDetails and propertyRecord`() {
        val unoccupiedPropertyOwnership = createPropertyOwnership()
        val unoccupiedViewModel = PropertyDetailsViewModel(unoccupiedPropertyOwnership)
        val unoccupiedPropertyDetailsRow =
            unoccupiedViewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.occupied" }
        assertEquals("commonText.no", unoccupiedViewModel.isTenantedKey)
        assertEquals("commonText.no", unoccupiedPropertyDetailsRow.fieldValue)

        val occupiedPropertyOwnership = createPropertyOwnership(currentNumTenants = 2)
        val occupiedViewModel = PropertyDetailsViewModel(occupiedPropertyOwnership)
        val occupiedPropertyDetailsRow =
            occupiedViewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.occupied" }
        assertEquals("commonText.yes", occupiedViewModel.isTenantedKey)
        assertEquals("commonText.yes", occupiedPropertyDetailsRow.fieldValue)
    }

    @Test
    fun `Landlord name and details url are returned in keyDetails`() {
        val landlordName = "Firstname Surname"
        val landlordDetailsUrl = "landlord-details-url"
        val landlord = createLandlord(name = landlordName)
        val propertyOwnership =
            createPropertyOwnership(
                primaryLandlord = landlord,
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership, landlordDetailsUrl = landlordDetailsUrl)

        val keyDetailsLandlord =
            viewModel.keyDetails
                .single { it.fieldHeading == "propertyDetails.keyDetails.registeredLandlord" }

        assertEquals(landlordName, keyDetailsLandlord.fieldValue)
        assertEquals(landlordDetailsUrl, keyDetailsLandlord.changeUrl)
    }

    @Test
    fun `Tenancy details are returned in the propertyRecord for an occupied property`() {
        val propertyOwnership =
            createPropertyOwnership(
                currentNumTenants = 3,
                currentNumHouseholds = 2,
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        val propertyRecordTenants =
            viewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.numberOfPeople" }
        val propertyRecordHouseholds =
            viewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.numberOfHouseholds" }

        assertEquals(3, propertyRecordTenants.fieldValue)
        assertEquals(2, propertyRecordHouseholds.fieldValue)
    }

    @Test
    fun `Tenancy details are hidden in the propertyRecord for an unoccupied property`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        assertNull(viewModel.propertyRecord.firstOrNull { it.fieldHeading == "propertyDetails.propertyRecord.numberOfPeople" })
        assertNull(viewModel.propertyRecord.firstOrNull { it.fieldHeading == "propertyDetails.propertyRecord.numberOfHouseholds" })
    }

    @Test
    fun `License details are shown in the the propertyRecord if a license exists`() {
        val licenseNumber = "L1234"
        val propertyOwnership =
            createPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, licenseNumber),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        val propertyRecordLicenseDetails =
            (
                viewModel.propertyRecord
                    .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingType" }
                    .fieldValue as List<*>
            ).filterIsInstance<String>()

        assertEquals("forms.licensingType.radios.option.hmoMandatory.label", propertyRecordLicenseDetails[0])
        assertEquals("L1234", propertyRecordLicenseDetails[1])
    }

    @Test
    fun `License type is shown as no license if there is no license`() {
        val propertyOwnershipDeclaredNoLicense =
            createPropertyOwnership(
                license = License(LicensingType.NO_LICENSING, ""),
            )
        val viewModelDeclaredNoLicense = PropertyDetailsViewModel(propertyOwnershipDeclaredNoLicense)
        val propertyRecordDeclaredNoLicense =
            viewModelDeclaredNoLicense.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingType" }

        assertEquals(
            "forms.checkPropertyAnswers.propertyDetails.noLicensing",
            propertyRecordDeclaredNoLicense.fieldValue,
        )

        val propertyOwnershipNullLicense =
            createPropertyOwnership()
        val viewModelNullLicense = PropertyDetailsViewModel(propertyOwnershipNullLicense)
        val propertyRecordNullLicense =
            viewModelNullLicense.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingType" }

        assertEquals("forms.checkPropertyAnswers.propertyDetails.noLicensing", propertyRecordNullLicense.fieldValue)
    }

    @Test
    fun `Property details contains the property address and uprn if available`() {
        // Arrange
        val expectedUprn = 1234.toLong()
        val address = createAddress(uprn = expectedUprn)
        val propertyOwnership =
            createPropertyOwnership(
                property = createProperty(address = address),
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        // Assert
        val uprn =
            viewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.uprn" }
                .fieldValue

        assertEquals(address.singleLineAddress, viewModel.address)
        assertEquals(expectedUprn.toString(), uprn)
    }

    @Test
    fun `Property details hides null uprn if hideNullUprn is true`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel = PropertyDetailsViewModel(propertyOwnership, hideNullUprn = true)

        assertNull(viewModel.propertyRecord.firstOrNull { it.fieldHeading == "propertyDetails.propertyRecord.uprn" })
    }

    @Test
    fun `Property details declares null uprn unavailable if hideNullUprn is false`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel = PropertyDetailsViewModel(propertyOwnership, hideNullUprn = false)

        val uprnKey =
            viewModel.propertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.uprn" }
                .fieldValue

        assertEquals("propertyDetails.propertyRecord.uprn.unavailable", uprnKey)
    }

    @Test
    fun `Change links are included on the relevant rows if withChangeLinks is true`() {
        val propertyOwnership =
            createPropertyOwnership(
                currentNumTenants = 3,
                currentNumHouseholds = 2,
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                property =
                    createProperty(
                        address =
                            createAddress(
                                uprn = 1234.toLong(),
                            ),
                    ),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership, withChangeLinks = true)

        val changeLinkCount = viewModel.propertyRecord.count { it.changeUrl != null }

        // TODO PRSD-1107, PRSD-1108, PRSD-1109: Update expected count when tickets implemented
        assertEquals(0, changeLinkCount)
    }

    @Test
    fun `Change links are not included if withChangeLinks is false`() {
        val propertyOwnership =
            createPropertyOwnership(
                currentNumTenants = 3,
                currentNumHouseholds = 2,
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                property =
                    createProperty(
                        address =
                            createAddress(
                                uprn = 1234.toLong(),
                            ),
                    ),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership, withChangeLinks = false)

        val changeLinkCount = viewModel.propertyRecord.count { it.changeUrl != null }

        assertEquals(0, changeLinkCount)
    }
}
