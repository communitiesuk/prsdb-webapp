package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class OrgLandlordViewModelTests {
    @Test
    fun `the view model exposes the organisation landlord's details`() {
        val testLandlord = MockLandlordData.createOrgLandlord()

        val viewModel = OrgLandlordViewModel(testLandlord)

        assertEquals(testLandlord.name, viewModel.name)
        assertEquals(testLandlord.address.singleLineAddress, viewModel.singleLineAddress)
        assertEquals(testLandlord.wholeOrgEmail, viewModel.email)
        assertEquals(testLandlord.phoneNumber, viewModel.phoneNumber)
        assertEquals(testLandlord.isCompany, viewModel.isCompany)
        assertEquals(testLandlord.isCharity, viewModel.isCharity)
        assertEquals(testLandlord.isTrust, viewModel.isTrust)
        assertEquals(testLandlord.companyNumber, viewModel.companyNumber)
        assertEquals(testLandlord.charityNumber, viewModel.charityNumber)
        assertEquals(testLandlord.mainContactName, viewModel.mainContactName)
        assertEquals(testLandlord.mainContactEmail, viewModel.mainContactEmail)
        assertEquals(testLandlord.mainContactPhone, viewModel.mainContactPhone)
    }

    @Test
    fun `the view model exposes lead trustee details for a trust`() {
        val testLandlord =
            MockLandlordData.createOrgLandlord(
                isCompany = false,
                isTrust = true,
                leadTrusteeName = "Lead trustee",
                leadTrusteeEmail = "lead.trustee@example.com",
                leadTrusteePhoneNumber = "07123456782",
            )

        val viewModel = OrgLandlordViewModel(testLandlord)

        assertEquals("Lead trustee", viewModel.leadTrusteeName)
        assertEquals("lead.trustee@example.com", viewModel.leadTrusteeEmail)
        assertEquals("07123456782", viewModel.leadTrusteePhone)
    }
}
