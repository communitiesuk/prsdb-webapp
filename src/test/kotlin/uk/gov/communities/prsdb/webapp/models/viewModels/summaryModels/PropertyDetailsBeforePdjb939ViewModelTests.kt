package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.math.BigDecimal

class PropertyDetailsBeforePdjb939ViewModelTests {
    private val mockMessageSource = MockMessageSource()

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
                "propertyDetails.propertyRecord.beforePdjb939.address",
                "propertyDetails.propertyRecord.beforePdjb939.uprn",
                "propertyDetails.propertyRecord.localCouncil",
                "propertyDetails.propertyRecord.propertyType",
                "propertyDetails.propertyRecord.beforePdjb939.ownershipType",
            )

        // Act
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )
        val headerList = viewModel.beforePdjb939PropertyRecord.map { it.fieldHeading }

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
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )
        val headerList = viewModel.beforePdjb939LicensingInformation.map { it.fieldHeading }

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
                "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            )

        // Act
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )
        val headerList = viewModel.beforePdjb939TenancyAndRentalInformation.map { it.fieldHeading }

        // Assert
        assertEquals(expectedHeaderList, headerList)
    }

    @Test
    fun `Bedrooms row shows the not added message key when no bedroom count was entered`() {
        // Arrange
        val propertyOwnership = createOccupiedPropertyOwnership()
        propertyOwnership.numBedrooms = null

        // Act
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )
        val bedroomsRow =
            viewModel.beforePdjb939TenancyAndRentalInformation.single {
                it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms"
            }

        // Assert
        assertEquals(
            "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms.notAdded",
            bedroomsRow.fieldValue,
        )
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
                "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            )

        // Act
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )
        val headerList = viewModel.beforePdjb939TenancyAndRentalInformation.map { it.fieldHeading }

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
                "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied",
            )

        // Act
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )
        val headerList = viewModel.beforePdjb939TenancyAndRentalInformation.map { it.fieldHeading }

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

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        assertNull(
            viewModel.beforePdjb939LicensingInformation.firstOrNull {
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

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        assertNull(
            viewModel.beforePdjb939LicensingInformation.firstOrNull {
                it.fieldHeading ==
                    "propertyDetails.propertyRecord.licensingInformation.licensingNumber"
            },
        )
    }

    @Test
    fun `the correct message keys are returned for occupancy tab and occupancy row value when property is occupied`() {
        val occupiedPropertyOwnership = createOccupiedPropertyOwnership()
        val occupiedViewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                occupiedPropertyOwnership,
                messageSource = mockMessageSource,
            )
        val occupiedPropertyDetailsRow =
            occupiedViewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied" }
        assertEquals("propertyDetails.occupationStatus.occupied", occupiedViewModel.isOccupiedKey)
        assertEquals("commonText.yes", occupiedPropertyDetailsRow.fieldValue)
    }

    @Test
    fun `the correct message key are returned for occupancy tab and occupancy row value when property is unoccupied`() {
        val unoccupiedPropertyOwnership = createUnoccupiedPropertyOwnership()
        val unoccupiedViewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                unoccupiedPropertyOwnership,
                messageSource = mockMessageSource,
            )
        val unoccupiedPropertyDetailsRow =
            unoccupiedViewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied" }
        assertEquals("propertyDetails.occupationStatus.unoccupied", unoccupiedViewModel.isOccupiedKey)
        assertEquals("commonText.no", unoccupiedPropertyDetailsRow.fieldValue)
    }

    @Test
    fun `an occupied provide-later property is rendered as unoccupied with no tenancy details`() {
        // Property 39 scenario: state created while the flag was on, so it is occupied but has no tenancy
        // details. The flag-off view must render it as unoccupied rather than throwing.
        val occupiedProvideLaterPropertyOwnership =
            createPropertyOwnership(
                currentNumTenants = 2,
                isOccupied = true,
                tenancyProvideLater = true,
                furnishedStatus = null,
                rentFrequency = null,
                rentAmount = null,
            )
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                occupiedProvideLaterPropertyOwnership,
                messageSource = mockMessageSource,
            )

        val occupiedRow =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied" }

        assertEquals(false, viewModel.effectivelyOccupied)
        assertEquals("propertyDetails.occupationStatus.unoccupied", viewModel.effectiveIsOccupiedKey)
        assertEquals("commonText.no", occupiedRow.fieldValue)
        assertEquals(1, viewModel.beforePdjb939TenancyAndRentalInformation.size)
    }

    @Test
    fun `Tenancy details are returned on the propertyRecord for an occupied property`() {
        val numberOfPeople = 3
        val numberOfHouseholds = 2
        val numberOfBedrooms = 2
        val furnishedStatus = FurnishedStatus.FURNISHED
        val rentFrequency = RentFrequency.MONTHLY
        val rentAmount = BigDecimal(200)
        val expectedRentAmount = "£$rentAmount"

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

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        val propertyRecordNumberOfPeople =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople" }
        val propertyRecordNumberOfHouseholds =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName" }
        val propertyRecordNumberOfBedrooms =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms" }
        val propertyRecordRentIncludesBills =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName" }
        val propertyRecordFurnishedStatus =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus" }
        val propertyRecordRentFrequency =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName" }
        val propertyRecordRentAmount =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount" }

        assertEquals(numberOfPeople, propertyRecordNumberOfPeople.fieldValue)
        assertEquals(numberOfHouseholds, propertyRecordNumberOfHouseholds.fieldValue)
        assertEquals(numberOfBedrooms, propertyRecordNumberOfBedrooms.fieldValue)
        assertEquals("commonText.no", propertyRecordRentIncludesBills.fieldValue)
        assertEquals("forms.furnishedStatus.radios.options.furnished.label", propertyRecordFurnishedStatus.fieldValue)
        assertEquals("forms.rentFrequency.radios.option.monthly.label", propertyRecordRentFrequency.fieldValue)
        assertEquals(expectedRentAmount, propertyRecordRentAmount.fieldValue)
    }

    @Test
    fun `Tenancy details are returned with conditional and custom values on the propertyRecord for an occupied property`() {
        val billsIncludedList = "ELECTRICITY,WATER,SOMETHING_ELSE"
        val customBillsIncluded = "cat sitting"
        val rentFrequency = RentFrequency.OTHER
        val customRentFrequency = "fortnightly"
        val rentAmount = BigDecimal(200)
        val expectedRentAmount = "£$rentAmount Message for forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix"
        val expectedBillsIncluded =
            "Message for forms.billsIncluded.checkbox.electricity, Message for forms.billsIncluded.checkbox.water, Cat sitting"

        val propertyOwnership =
            createOccupiedPropertyOwnership(
                billsIncludedList = billsIncludedList,
                customBillsIncluded = customBillsIncluded,
                rentFrequency = rentFrequency,
                customRentFrequency = customRentFrequency,
                rentAmount = rentAmount,
            )

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        val propertyRecordRentIncludesBills =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName" }
        val propertyRecordBillsIncluded =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded" }
        val propertyRecordFurnishedStatus =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus" }
        val propertyRecordRentFrequency =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName" }
        val propertyRecordRentAmount =
            viewModel.beforePdjb939TenancyAndRentalInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount" }

        assertEquals("commonText.yes", propertyRecordRentIncludesBills.fieldValue)
        assertEquals(expectedBillsIncluded, propertyRecordBillsIncluded.fieldValue)
        assertEquals("forms.furnishedStatus.radios.options.furnished.label", propertyRecordFurnishedStatus.fieldValue)
        assertEquals("Fortnightly", propertyRecordRentFrequency.fieldValue)
        assertEquals(expectedRentAmount, propertyRecordRentAmount.fieldValue)
    }

    @Test
    fun `License details are shown in the the propertyRecord if a license exists`() {
        val licenseNumber = "L1234"
        val propertyOwnership =
            createPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, licenseNumber),
            )

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        val propertyRecordLicenseType =
            viewModel.beforePdjb939LicensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingType" }

        val propertyRecordLicenseNumber =
            viewModel.beforePdjb939LicensingInformation
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
        val viewModelDeclaredNoLicense =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnershipDeclaredNoLicense,
                messageSource = mockMessageSource,
            )
        val propertyRecordDeclaredNoLicense =
            viewModelDeclaredNoLicense.beforePdjb939LicensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingType" }

        assertEquals(
            "forms.checkPropertyAnswers.propertyDetails.noLicensing",
            propertyRecordDeclaredNoLicense.fieldValue,
        )

        val propertyOwnershipNullLicense =
            createPropertyOwnership()
        val viewModelNullLicense =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnershipNullLicense,
                messageSource = mockMessageSource,
            )
        val propertyRecordNullLicense =
            viewModelNullLicense.beforePdjb939LicensingInformation
                .single { it.fieldHeading == "propertyDetails.propertyRecord.licensingInformation.licensingType" }

        assertEquals("forms.checkPropertyAnswers.propertyDetails.noLicensing", propertyRecordNullLicense.fieldValue)
    }

    @Test
    fun `Property type row displays the custom property type when it is set`() {
        val customType = "End terrace"
        val propertyOwnership =
            createPropertyOwnership(
                propertyBuildType = PropertyType.OTHER,
                customPropertyType = customType,
            )

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        val propertyTypeRow =
            viewModel.beforePdjb939PropertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.propertyType" }

        assertEquals(customType, propertyTypeRow.fieldValue)
    }

    @Test
    fun `Property type row displays the message key when custom property type is not set`() {
        val propertyOwnership =
            createPropertyOwnership(
                propertyBuildType = PropertyType.SEMI_DETACHED_HOUSE,
                customPropertyType = null,
            )

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        val propertyTypeRow =
            viewModel.beforePdjb939PropertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.propertyType" }

        assertEquals("forms.propertyType.radios.option.semiDetachedHouse.label", propertyTypeRow.fieldValue)
    }

    @Test
    fun `Property details contains the property address and uprn if available`() {
        // Arrange
        val expectedUprn = 1234.toLong()
        val address = createAddress(uprn = expectedUprn)
        val propertyOwnership = createPropertyOwnership(address = address)

        // Act
        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                messageSource = mockMessageSource,
            )

        // Assert
        val uprn =
            viewModel.beforePdjb939PropertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.uprn" }
                .fieldValue

        assertEquals(address.singleLineAddress, viewModel.address)
        assertEquals(expectedUprn.toString(), uprn)
    }

    @Test
    fun `Property details hides null uprn for a landlord`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                isLandlordView = true,
                messageSource = mockMessageSource,
            )

        assertNull(
            viewModel.beforePdjb939PropertyRecord.firstOrNull {
                it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.uprn"
            },
        )
    }

    @Test
    fun `Property details declares null uprn unavailable for a council`() {
        val propertyOwnership = createPropertyOwnership()

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                isLandlordView = false,
                messageSource = mockMessageSource,
            )

        val uprnKey =
            viewModel.beforePdjb939PropertyRecord
                .single { it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.uprn" }
                .fieldValue

        assertEquals("propertyDetails.propertyRecord.beforePdjb939.uprn.unavailable", uprnKey)
    }

    @Test
    fun `Change links are included on the relevant rows for a landlord`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                address = createAddress(uprn = 1234.toLong()),
            )

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                isLandlordView = true,
                messageSource = mockMessageSource,
            )

        val propertyRecordChangeLinkCount = viewModel.beforePdjb939PropertyRecord.count { it.actions.isNotEmpty() }

        val licensingInformationChangeLinkCount = viewModel.beforePdjb939LicensingInformation.count { it.actions.isNotEmpty() }

        val tenancyInformationChangeLinkCount = viewModel.beforePdjb939TenancyAndRentalInformation.count { it.actions.isNotEmpty() }

        val totalChangeLinkCount =
            propertyRecordChangeLinkCount + licensingInformationChangeLinkCount + tenancyInformationChangeLinkCount

        assertEquals(8, totalChangeLinkCount)
    }

    @Test
    fun `Change links are not included for a council`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
                address = createAddress(uprn = 1234.toLong()),
            )

        val viewModel =
            PropertyDetailsBeforePdjb939ViewModel(
                propertyOwnership,
                isLandlordView = false,
                messageSource = mockMessageSource,
            )

        val propertyRecordChangeLinkCount = viewModel.beforePdjb939PropertyRecord.count { it.actions.isNotEmpty() }

        val licensingInformationChangeLinkCount = viewModel.beforePdjb939LicensingInformation.count { it.actions.isNotEmpty() }

        val tenancyInformationChangeLinkCount = viewModel.beforePdjb939TenancyAndRentalInformation.count { it.actions.isNotEmpty() }

        val totalChangeLinkCount =
            propertyRecordChangeLinkCount + licensingInformationChangeLinkCount + tenancyInformationChangeLinkCount

        assertEquals(0, totalChangeLinkCount)
    }
}
