package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.database.entity.Address
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class OrgLandlordViewModelTests {
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

        val viewModel = OrgLandlordViewModel(landlord)

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

        val viewModel = OrgLandlordViewModel(landlord)

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

        val viewModel = OrgLandlordViewModel(landlord)

        val headings = viewModel.organisationDetails.map { it.fieldHeading }
        assertTrue("landlordDetails.org.charityNumber" !in headings)
        assertEquals(
            "commonText.yes",
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.registeredCharity" }.fieldValue,
        )
        assertEquals(
            "commonText.other",
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.charityCommission" }.fieldValue,
        )
    }

    @Test
    fun `an organisation without a company number omits the companies house number row`() {
        val landlord = MockLandlordData.createOrgLandlord(isCompany = false, companyNumber = null)

        val viewModel = OrgLandlordViewModel(landlord)

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

        val viewModel = OrgLandlordViewModel(landlord)

        assertIterableEquals(
            listOf("3rd Floor", "88 Kingsway Square", "London", "ZX1 4QP"),
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.address" }.fieldValue as List<*>,
        )
    }

    @Test
    fun `the organisation type row maps each selected organisation type to its message key`() {
        val landlord =
            MockLandlordData.createOrgLandlord(isCompany = true, isCharity = true, isTrust = true)

        val viewModel = OrgLandlordViewModel(landlord)

        assertIterableEquals(
            listOf(
                "registerAsALandlord.orgType.checkbox.company",
                "registerAsALandlord.orgType.checkbox.charity",
                "registerAsALandlord.orgType.checkbox.trust",
            ),
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.organisationType" }.fieldValue as List<*>,
        )
    }

    @Test
    fun `an organisation with no selected organisation types shows other`() {
        val landlord =
            MockLandlordData.createOrgLandlord(isCompany = false, isCharity = false, isTrust = false)

        val viewModel = OrgLandlordViewModel(landlord)

        assertIterableEquals(
            listOf("commonText.other"),
            viewModel.organisationDetails.single { it.fieldHeading == "landlordDetails.org.organisationType" }.fieldValue as List<*>,
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
                "landlordDetails.org.address",
                "landlordDetails.org.email",
                "landlordDetails.org.phone",
                "landlordDetails.org.organisationType",
                "landlordDetails.org.registeredCharity",
                "landlordDetails.org.registeredWithCompaniesHouse",
            )

        val viewModel = OrgLandlordViewModel(landlord)

        viewModel.organisationDetails.forEach { row ->
            if (row.fieldHeading in changeableHeadings) {
                assertTrue(row.actions.isNotEmpty(), "${row.fieldHeading} should have an action link")
            } else {
                assertTrue(row.actions.isEmpty(), "${row.fieldHeading} should not have an action link")
            }
        }
    }
}
