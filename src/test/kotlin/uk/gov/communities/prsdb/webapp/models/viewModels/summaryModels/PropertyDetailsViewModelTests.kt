package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import java.math.BigDecimal

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
                address = createAddress(uprn = 1234.toLong()),
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.registrationDate",
                "propertyDetails.propertyRecord.registrationNumber",
                "propertyDetails.propertyRecord.address",
                "propertyDetails.propertyRecord.uprn",
                "propertyDetails.propertyRecord.localCouncil",
                "propertyDetails.propertyRecord.propertyType",
                "propertyDetails.propertyRecord.ownershipType",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.propertyRecord.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `licensing information details are in the correct order`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.licensingInformation.licensingType",
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.licensingInformation.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Tenancy details are in the correct order when property is occupied`() {
        // Arrange
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                address = createAddress(uprn = 1234.toLong()),
                billsIncludedList = null,
                customBillsIncluded = null,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.tenancyAndRentalInformation.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Tenancy details are in the correct order when property is occupied and all conditional and custom fields are filled`() {
        // Arrange
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                address = createAddress(uprn = 1234.toLong()),
                billsIncludedList = "ELECTRICITY,WATER,SOMETHING_ELSE",
                customBillsIncluded = "cat sitting",
                rentFrequency = RentFrequency.OTHER,
                customRentFrequency = "Fortnightly",
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.tenancyAndRentalInformation.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Occupied is the only row in tenancy details when property is NOT occupied`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                address = createAddress(uprn = 1234.toLong()),
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied",
            )

        // Act
        val viewModel = PropertyDetailsViewModel(propertyOwnership)
        val headerList = viewModel.tenancyAndRentalInformation.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Licensing number row is hidden when the property has a license record with NOLICENSING type`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                license = License(LicensingType.NO_LICENSING, ""),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        assertNull(
            viewModel.licensingInformation.firstOrNull {
                it.fieldHeading ==
                    "propertyDetails.propertyRecord.licensingInformation.licensingNumber"
            },
        )
    }

    @Test
    fun `Licensing number row is hidden when the property has no license`() {
        // Arrange
        val propertyOwnership =
            createPropertyOwnership(
                license = null,
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        assertNull(
            viewModel.licensingInformation.firstOrNull {
                it.fieldHeading ==
                    "propertyDetails.propertyRecord.licensingInformation.licensingNumber"
            },
        )
    }

    @Test
    fun `isTenantedKey returns the correct value in keyDetails and tenancyAndRentalInformation`() {
        val unoccupiedPropertyOwnership = createPropertyOwnership()
        val unoccupiedViewModel = PropertyDetailsViewModel(unoccupiedPropertyOwnership)
        val unoccupiedPropertyDetailsRow =
            unoccupiedViewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied" }
        assertEquals("commonText.no", unoccupiedViewModel.isTenantedKey)
        assertEquals("commonText.no", unoccupiedPropertyDetailsRow.fieldValue)

        val occupiedPropertyOwnership = createOccupiedPropertyOwnership(currentNumTenants = 2)
        val occupiedViewModel = PropertyDetailsViewModel(occupiedPropertyOwnership)
        val occupiedPropertyDetailsRow =
            occupiedViewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.occupied" }
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
        assertEquals(landlordDetailsUrl, keyDetailsLandlord.valueUrl)
    }

    @Test
    fun `Tenancy details are returned on the propertyRecord for an occupied property`() {
        val numberOfPeople = 3
        val numberOfHouseholds = 2
        val numberOfBedrooms = 2
        val furnishedStatus = FurnishedStatus.FURNISHED
        val rentFrequency = RentFrequency.MONTHLY
        val rentAmount = BigDecimal(200)

        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumTenants = numberOfPeople,
                currentNumHouseholds = numberOfHouseholds,
                numberOfBedrooms = numberOfBedrooms,
                billsIncludedList = null,
                customBillsIncluded = null,
                furnishedStatus = furnishedStatus,
                rentFrequency = rentFrequency,
                customRentFrequency = null,
                rentAmount = rentAmount,
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        val propertyRecordNumberOfPeople =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople" }
        val propertyRecordNumberOfHouseholds =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds" }
        val propertyRecordNumberOfBedrooms =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms" }
        val propertyRecordRentIncludesBills =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills" }
        val propertyRecordFurnishedStatus =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus" }
        val propertyRecordRentFrequency =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency" }
        val propertyRecordRentAmount =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount" }

        assertEquals(numberOfPeople, propertyRecordNumberOfPeople.fieldValue)
        assertEquals(numberOfHouseholds, propertyRecordNumberOfHouseholds.fieldValue)
        assertEquals(numberOfBedrooms, propertyRecordNumberOfBedrooms.fieldValue)
        assertEquals("commonText.no", propertyRecordRentIncludesBills.fieldValue)
        assertEquals("forms.furnishedStatus.radios.options.furnished.label", propertyRecordFurnishedStatus.fieldValue)
        assertEquals("forms.rentFrequency.radios.option.monthly.label", propertyRecordRentFrequency.fieldValue)
        assertEquals(listOf("commonText.poundSign", "200"), propertyRecordRentAmount.singleLineFormattedStringValue?.listOfValues)
    }

    @Test
    fun `Tenancy details are returned with conditional and custom values on the propertyRecord for an occupied property`() {
        val billsIncludedList = "ELECTRICITY,WATER,SOMETHING_ELSE"
        val customBillsIncluded = "cat sitting"
        val rentFrequency = RentFrequency.OTHER
        val customRentFrequency = "fortnightly"
        val rentAmount = BigDecimal(200)

        val propertyOwnership =
            createOccupiedPropertyOwnership(
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        val propertyRecordRentIncludesBills =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills" }
        val propertyRecordBillsIncluded =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded" }
        val propertyRecordFurnishedStatus =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus" }
        val propertyRecordRentFrequency =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency" }
        val propertyRecordRentAmount =
            viewModel.tenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount" }

        assertEquals("commonText.yes", propertyRecordRentIncludesBills.fieldValue)
        assertEquals(
            listOf<String>("forms.billsIncluded.checkbox.electricity", "forms.billsIncluded.checkbox.water", "Cat sitting"),
            propertyRecordBillsIncluded.singleLineFormattedStringValue?.listOfValues,
        )
        assertEquals("forms.furnishedStatus.radios.options.furnished.label", propertyRecordFurnishedStatus.fieldValue)
        assertEquals("Fortnightly", propertyRecordRentFrequency.fieldValue)
        assertEquals(
            listOf("commonText.poundSign", "200", " ", "forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix"),
            propertyRecordRentAmount.singleLineFormattedStringValue?.listOfValues,
        )
    }

    @Test
    fun `License details are shown in the the propertyRecord if a license exists`() {
        val licenseNumber = "L1234"
        val propertyOwnership =
            createPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, licenseNumber),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership)

        val propertyRecordLicenseType =
            viewModel.licensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingType" }

        val propertyRecordLicenseNumber =
            viewModel.licensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingNumber" }

        assertEquals("forms.licensingType.radios.option.hmoMandatory.label", propertyRecordLicenseType.fieldValue)
        assertEquals("L1234", propertyRecordLicenseNumber.fieldValue)
    }

    @Test
    fun `License type is shown as no license if there is no license`() {
        val propertyOwnershipDeclaredNoLicense =
            createPropertyOwnership(
                license = License(LicensingType.NO_LICENSING, ""),
            )
        val viewModelDeclaredNoLicense = PropertyDetailsViewModel(propertyOwnershipDeclaredNoLicense)
        val propertyRecordDeclaredNoLicense =
            viewModelDeclaredNoLicense.licensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingType" }

        assertEquals(
            "forms.checkPropertyAnswers.propertyDetails.noLicensing",
            propertyRecordDeclaredNoLicense.fieldValue,
        )

        val propertyOwnershipNullLicense =
            createPropertyOwnership()
        val viewModelNullLicense = PropertyDetailsViewModel(propertyOwnershipNullLicense)
        val propertyRecordNullLicense =
            viewModelNullLicense.licensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingType" }

        assertEquals("forms.checkPropertyAnswers.propertyDetails.noLicensing", propertyRecordNullLicense.fieldValue)
    }

    @Test
    fun `Property details contains the property address and uprn if available`() {
        // Arrange
        val expectedUprn = 1234.toLong()
        val address = createAddress(uprn = expectedUprn)
        val propertyOwnership = createPropertyOwnership(address = address)

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
            createOccupiedPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                address = createAddress(uprn = 1234.toLong()),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership, withChangeLinks = true)

        val propertyRecordChangeLinkCount = viewModel.propertyRecord.count { it.action != null }

        val licensingInformationChangeLinkCount = viewModel.licensingInformation.count { it.action != null }

        val tenancyInformationChangeLinkCount = viewModel.tenancyAndRentalInformation.count { it.action != null }

        val totalChangeLinkCount = propertyRecordChangeLinkCount + licensingInformationChangeLinkCount + tenancyInformationChangeLinkCount

        assertEquals(5, totalChangeLinkCount)
    }

    @Test
    fun `Change links are not included if withChangeLinks is false`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                address = createAddress(uprn = 1234.toLong()),
            )

        val viewModel = PropertyDetailsViewModel(propertyOwnership, withChangeLinks = false)

        val propertyRecordChangeLinkCount = viewModel.propertyRecord.count { it.action != null }

        val licensingInformationChangeLinkCount = viewModel.licensingInformation.count { it.action != null }

        val tenancyInformationChangeLinkCount = viewModel.tenancyAndRentalInformation.count { it.action != null }

        val totalChangeLinkCount = propertyRecordChangeLinkCount + licensingInformationChangeLinkCount + tenancyInformationChangeLinkCount

        assertEquals(0, totalChangeLinkCount)
    }
}
