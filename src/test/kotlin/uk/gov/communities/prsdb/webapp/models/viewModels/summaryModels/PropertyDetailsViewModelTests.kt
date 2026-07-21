package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
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
import java.time.LocalDate

class PropertyDetailsViewModelTests {
    private val mockMessageSource = MockMessageSource()

    private val featureFlagManager =
        mock<FeatureFlagManager> {
            on { checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING) } doReturn true
        }

    @Nested
    inner class BeforePdjb939Layout {
        private val featureFlagManager =
            mock<FeatureFlagManager> {
                on { checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING) } doReturn false
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
                    "propertyDetails.propertyRecord.beforePdjb939.address",
                    "propertyDetails.propertyRecord.beforePdjb939.uprn",
                    "propertyDetails.propertyRecord.localCouncil",
                    "propertyDetails.propertyRecord.propertyType",
                    "propertyDetails.propertyRecord.beforePdjb939.ownershipType",
                )

            // Act
            val viewModel =
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
                )
            val headerList = viewModel.beforePdjb939TenancyAndRentalInformation.map { it.fieldHeading }

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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    occupiedPropertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    unoccupiedPropertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
                )
            val unoccupiedPropertyDetailsRow =
                unoccupiedViewModel.beforePdjb939TenancyAndRentalInformation
                    .single { it.fieldHeading == "propertyDetails.propertyRecord.beforePdjb939.tenancyAndRentalInformation.occupied" }
            assertEquals("propertyDetails.occupationStatus.unoccupied", unoccupiedViewModel.isOccupiedKey)
            assertEquals("commonText.no", unoccupiedPropertyDetailsRow.fieldValue)
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnershipDeclaredNoLicense,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnershipNullLicense,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    isLandlordView = true,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    isLandlordView = false,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    isLandlordView = true,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
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
                PropertyDetailsViewModel(
                    propertyOwnership,
                    isLandlordView = false,
                    messageSource = mockMessageSource,
                    featureFlagManager = featureFlagManager,
                )

            val propertyRecordChangeLinkCount = viewModel.beforePdjb939PropertyRecord.count { it.actions.isNotEmpty() }

            val licensingInformationChangeLinkCount = viewModel.beforePdjb939LicensingInformation.count { it.actions.isNotEmpty() }

            val tenancyInformationChangeLinkCount = viewModel.beforePdjb939TenancyAndRentalInformation.count { it.actions.isNotEmpty() }

            val totalChangeLinkCount =
                propertyRecordChangeLinkCount + licensingInformationChangeLinkCount + tenancyInformationChangeLinkCount

            assertEquals(0, totalChangeLinkCount)
        }
    }

    @Test
    fun `property details section is in the correct order`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                address = createAddress(uprn = 1234.toLong()),
                numberOfBedrooms = 2,
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.propertyDetails.address",
                "propertyDetails.propertyRecord.localCouncil",
                "propertyDetails.propertyRecord.propertyType",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfBedrooms",
            )

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource, featureFlagManager = featureFlagManager)

        assertEquals(expectedHeaderList, viewModel.propertyDetailsSection.map { it.fieldHeading })
    }

    @Test
    fun `property details section renders the address as multiple lines`() {
        val propertyOwnership = createOccupiedPropertyOwnership(address = createAddress(uprn = 1234.toLong()))

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource, featureFlagManager = featureFlagManager)

        val addressValue =
            viewModel.propertyDetailsSection
                .single { it.fieldHeading == "propertyDetails.propertyRecord.propertyDetails.address" }
                .fieldValue

        assertEquals(propertyOwnership.address.toMultiLineAddress().split("\n"), addressValue)
    }

    @Test
    fun `registration, ownership and occupation sections contain the expected rows`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource, featureFlagManager = featureFlagManager)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.registrationNumber",
                "propertyDetails.propertyRecord.registrationDate",
            ),
            viewModel.registrationDetails.map { it.fieldHeading },
        )
        assertEquals(
            listOf("propertyDetails.propertyRecord.ownership.ownershipType"),
            viewModel.ownershipSection.map { it.fieldHeading },
        )
        assertEquals(
            listOf("propertyDetails.propertyRecord.occupation.isOccupied"),
            viewModel.occupiedSection.map { it.fieldHeading },
        )
    }

    @Test
    fun `shows a licensing provide-later row for a landlord when licensing is skipped on an occupied property`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                lastOccupiedDate = LocalDate.of(2025, 1, 1),
                licenseProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = true,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        assertEquals(
            listOf("propertyDetails.propertyRecord.licensing.rowName"),
            viewModel.licensingSection.map { it.fieldHeading },
        )
        assertNull(viewModel.licensingProvideLaterParagraph)
    }

    @Test
    fun `shows a licensing deadline paragraph for a council when licensing is skipped on an occupied property`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                lastOccupiedDate = LocalDate.of(2025, 1, 1),
                licenseProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = false,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        assertTrue(viewModel.licensingSection.isEmpty())
        assertEquals(
            "Message for propertyDetails.propertyRecord.licensing.councilOccupied",
            viewModel.licensingProvideLaterParagraph,
        )
    }

    @Test
    fun `throws when building a council licensing paragraph for an occupied property with no occupied date`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = null,
                lastOccupiedDate = null,
                licenseProvideLater = true,
            )

        assertThrows<IllegalStateException> {
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = false,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )
        }
    }

    @Test
    fun `shows a licensing not-provided paragraph for a local council when licensing is skipped on an unoccupied property`() {
        val propertyOwnership = createPropertyOwnership(license = null, licenseProvideLater = true)

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = false,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        assertEquals(
            "Message for propertyDetails.propertyRecord.licensing.councilNotProvided",
            viewModel.licensingProvideLaterParagraph,
        )
    }

    @Test
    fun `shows a no-deadline licensing provide-later row for a landlord when licensing is skipped on an unoccupied property`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership(licenseProvideLater = true)

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = true,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        val licensingRow = viewModel.licensingSection.single()
        assertEquals("propertyDetails.propertyRecord.licensing.rowName", licensingRow.fieldHeading)
        assertEquals("propertyDetails.propertyRecord.licensing.provideLaterUnoccupied", licensingRow.fieldValue)
        assertNull(viewModel.licensingProvideLaterParagraph)
    }

    @Test
    fun `shows the licensing type row when a license is present`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                license = License(LicensingType.HMO_MANDATORY_LICENCE, "L1234"),
            )

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource, featureFlagManager = featureFlagManager)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.licensingInformation.licensingType",
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
            ),
            viewModel.licensingSection.map { it.fieldHeading },
        )
        assertNull(viewModel.licensingProvideLaterParagraph)
    }

    @Test
    fun `hides the licensing number row and shows a no-licensing type for a no-licensing record or no license`() {
        val declaredNoLicenseViewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(license = License(LicensingType.NO_LICENSING, "")),
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )
        val nullLicenseViewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(license = null),
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        listOf(declaredNoLicenseViewModel, nullLicenseViewModel).forEach { viewModel ->
            assertEquals(
                listOf("propertyDetails.propertyRecord.licensingInformation.licensingType"),
                viewModel.licensingSection.map { it.fieldHeading },
            )
            assertEquals(
                "forms.checkPropertyAnswers.propertyDetails.noLicensing",
                viewModel.licensingSection.single().fieldValue,
            )
        }
    }

    @Test
    fun `shows a tenancy provide-later row for a landlord when tenancy is skipped on an occupied property`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                currentNumHouseholds = 0,
                lastOccupiedDate = LocalDate.of(2025, 1, 1),
                tenancyProvideLater = true,
            )

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = true,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        assertEquals("propertyDetails.propertyRecord.tenancy.heading", viewModel.tenancyHeadingKey)
        assertEquals(
            listOf("propertyDetails.propertyRecord.tenancy.rowName"),
            viewModel.tenancySection.map { it.fieldHeading },
        )
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `hides the tenancy section for a landlord when the property is unoccupied`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = true,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        assertTrue(viewModel.tenancySection.isEmpty())
    }

    @Test
    fun `hides the tenancy section for a council when the property is unoccupied`() {
        val propertyOwnership = createUnoccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(
                propertyOwnership,
                isLandlordView = false,
                messageSource = mockMessageSource,
                featureFlagManager = featureFlagManager,
            )

        assertTrue(viewModel.tenancySection.isEmpty())
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `shows the full tenancy rows when tenancy details are provided`() {
        val propertyOwnership =
            createOccupiedPropertyOwnership(
                billsIncludedList = null,
                customBillsIncluded = null,
                rentFrequency = RentFrequency.MONTHLY,
                customRentFrequency = null,
            )

        val expectedHeaderList =
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            )

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource, featureFlagManager = featureFlagManager)

        assertEquals(expectedHeaderList, viewModel.tenancySection.map { it.fieldHeading })
        assertEquals("propertyDetails.propertyRecord.tenancy.heading", viewModel.tenancyHeadingKey)
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `shows the bills-included row with conditional and custom tenancy values when they are provided`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource, featureFlagManager = featureFlagManager)

        assertEquals(
            listOf(
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfHouseholds.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.numberOfPeople",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.furnishedStatus",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded",
                "propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount",
            ),
            viewModel.tenancySection.map { it.fieldHeading },
        )

        fun rowValue(heading: String) = viewModel.tenancySection.single { it.fieldHeading == heading }.fieldValue

        assertEquals(
            "commonText.yes",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentIncludesBills.rowName"),
        )
        assertEquals(
            "Message for forms.billsIncluded.checkbox.electricity, Message for forms.billsIncluded.checkbox.water, Cat sitting",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.billsIncluded"),
        )
        assertEquals(
            "Fortnightly",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentFrequency.rowName"),
        )
        assertEquals(
            "£200 Message for forms.checkPropertyAnswers.tenancyDetails.customFrequencyRentAmountSuffix",
            rowValue("propertyDetails.propertyRecord.tenancyAndRentalInformation.rentAmount"),
        )
    }
}
