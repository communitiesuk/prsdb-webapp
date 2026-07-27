package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.License
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createAddress
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createUnoccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.time.LocalDate

class PropertyDetailsViewModelTests {
    private val mockMessageSource = MockMessageSource()

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
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(expectedHeaderList, viewModel.propertyDetailsSection.map { it.fieldHeading })
    }

    @Test
    fun `property details section renders the address as multiple lines`() {
        val propertyOwnership = createOccupiedPropertyOwnership(address = createAddress(uprn = 1234.toLong()))

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

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
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

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
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

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
            )
        val nullLicenseViewModel =
            PropertyDetailsViewModel(
                createOccupiedPropertyOwnership(license = null),
                messageSource = mockMessageSource,
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
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

        assertEquals(expectedHeaderList, viewModel.tenancySection.map { it.fieldHeading })
        assertEquals("propertyDetails.propertyRecord.tenancy.heading", viewModel.tenancyHeadingKey)
        assertNull(viewModel.tenancyProvideLaterParagraph)
    }

    @Test
    fun `shows the bills-included row with conditional and custom tenancy values when they are provided`() {
        val propertyOwnership = createOccupiedPropertyOwnership()

        val viewModel =
            PropertyDetailsViewModel(propertyOwnership, messageSource = mockMessageSource)

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
