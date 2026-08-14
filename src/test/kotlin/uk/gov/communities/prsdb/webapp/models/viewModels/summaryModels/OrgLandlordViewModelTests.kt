package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class OrgLandlordViewModelTests {
    private val messageSource = YamlMessageSource("classpath:messages")

    @Test
    fun `a registered charity and registered company shows all organisation detail rows in order`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCompany = true,
                isCharity = true,
                isTrust = false,
                companyNumber = "01234567",
                charityRegisteredWith = CharityRegulator.ENGLAND_AND_WALES,
                charityNumber = "0123456",
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertIterableEquals(
            listOf(
                "landlordDetails.org.registrationDate",
                "landlordDetails.org.lrn",
                "landlordDetails.org.landlordType",
                "landlordDetails.org.name",
                "landlordDetails.org.address",
                "landlordDetails.org.email",
                "landlordDetails.org.phone",
                "landlordDetails.org.organisationType",
                "landlordDetails.org.registeredCharity",
                "landlordDetails.org.charityCommission",
                "landlordDetails.org.charityNumber",
                "landlordDetails.org.registeredWithCompaniesHouse",
                "landlordDetails.org.companyNumber",
            ),
            viewModel.organisationDetails.map { it.fieldHeading },
        )
    }

    @Test
    fun `an organisation that is not a registered charity omits the charity commission and charity number rows`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCharity = false,
                charityRegisteredWith = null,
                charityNumber = null,
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        val headings = viewModel.organisationDetails.map { it.fieldHeading }
        assertTrue("landlordDetails.org.charityCommission" !in headings)
        assertTrue("landlordDetails.org.charityNumber" !in headings)
        assertEquals(
            "commonText.no",
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.registeredCharity" }.fieldValue,
        )
    }

    @Test
    fun `a registered charity with regulator NONE shows the charity commission row but omits the charity number row`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCharity = true,
                charityRegisteredWith = CharityRegulator.NONE,
                charityNumber = null,
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        val headings = viewModel.organisationDetails.map { it.fieldHeading }
        assertTrue("landlordDetails.org.charityNumber" !in headings)
        assertEquals(
            "commonText.yes",
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.registeredCharity" }.fieldValue,
        )
        assertEquals(
            "commonText.other",
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.charityCommission" }
                .getConvertedFieldValue(),
        )
    }

    @Test
    fun `an organisation without a company number omits the companies house number row`() {
        val landlord = MockLandlordData.createOrgLandlord(isCompany = false, companyNumber = null)

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        val headings = viewModel.organisationDetails.map { it.fieldHeading }
        assertTrue("landlordDetails.org.companyNumber" !in headings)
        assertEquals(
            "commonText.no",
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.registeredWithCompaniesHouse" }
                .fieldValue,
        )
    }

    @Test
    fun `the organisation address is split into multiple lines`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                address = Address(AddressDataModel("3rd Floor, 88 Kingsway Square, London, ZX1 4QP")),
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertIterableEquals(
            listOf("3rd Floor", "88 Kingsway Square", "London", "ZX1 4QP"),
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.address" }.fieldValue as List<*>,
        )
    }

    @Test
    fun `the organisation type row joins the selected organisation types into a single comma separated value`() {
        val landlord =
            MockLandlordData.createOrgLandlord(isCompany = true, isCharity = true, isTrust = true)

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertEquals(
            "Company, Charity, Trust",
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.organisationType" }
                .fieldValue,
        )
    }

    @Test
    fun `an organisation with no selected organisation types shows Other`() {
        val landlord =
            MockLandlordData.createOrgLandlord(isCompany = false, isCharity = false, isTrust = false)

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertEquals(
            "Other",
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.organisationType" }
                .fieldValue,
        )
    }

    @Test
    fun `a registered charity with a charity number groups the charity rows into one bordered section`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCharity = true,
                charityRegisteredWith = CharityRegulator.ENGLAND_AND_WALES,
                charityNumber = "0123456",
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertTrue(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.registeredCharity" }
                .withoutBottomBorder,
        )
        assertTrue(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.charityCommission" }
                .withoutBottomBorder,
        )
        assertFalse(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.charityNumber" }
                .withoutBottomBorder,
        )
    }

    @Test
    fun `a registered charity without a charity number closes the charity section after the charity commission row`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCharity = true,
                charityRegisteredWith = CharityRegulator.NONE,
                charityNumber = null,
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertTrue(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.registeredCharity" }
                .withoutBottomBorder,
        )
        assertFalse(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.charityCommission" }
                .withoutBottomBorder,
        )
    }

    @Test
    fun `an organisation that is not a registered charity keeps the border on the registered charity row`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCharity = false,
                charityRegisteredWith = null,
                charityNumber = null,
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertFalse(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.registeredCharity" }
                .withoutBottomBorder,
        )
    }

    @Test
    fun `a registered company groups the companies house rows into one bordered section`() {
        val landlord = MockLandlordData.createOrgLandlord(isCompany = true, companyNumber = "01234567")

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertTrue(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.registeredWithCompaniesHouse" }
                .withoutBottomBorder,
        )
        assertFalse(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.companyNumber" }
                .withoutBottomBorder,
        )
    }

    @Test
    fun `an organisation without a company number keeps the border on the registered with companies house row`() {
        val landlord = MockLandlordData.createOrgLandlord(isCompany = false, companyNumber = null)

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        assertFalse(
            viewModel.organisationDetails
                .single { it.fieldHeading == "landlordDetails.org.registeredWithCompaniesHouse" }
                .withoutBottomBorder,
        )
    }

    @Test
    fun `only the changeable rows have action links`() {
        val landlord =
            MockLandlordData.createOrgLandlord(
                isCompany = true,
                isCharity = true,
                companyNumber = "01234567",
                charityRegisteredWith = CharityRegulator.ENGLAND_AND_WALES,
                charityNumber = "0123456",
            )
        val changeableHeadings =
            listOf(
                "landlordDetails.org.name",
                "landlordDetails.org.email",
                "landlordDetails.org.phone",
                "landlordDetails.org.organisationType",
                "landlordDetails.org.registeredCharity",
                "landlordDetails.org.registeredWithCompaniesHouse",
            )

        val viewModel = OrgLandlordViewModel(landlord, messageSource)

        viewModel.organisationDetails.forEach { row ->
            if (row.fieldHeading in changeableHeadings) {
                assertTrue(row.actions.isNotEmpty(), "${row.fieldHeading} should have an action link")
            } else {
                assertTrue(row.actions.isEmpty(), "${row.fieldHeading} should not have an action link")
            }
        }
    }
}
