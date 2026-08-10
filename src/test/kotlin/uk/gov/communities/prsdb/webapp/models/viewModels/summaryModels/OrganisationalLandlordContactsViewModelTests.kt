package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationGoverningBodyMember
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate

class OrganisationalLandlordContactsViewModelTests {
    private val address = MockLandlordData.createAddress("3rd Floor, 88 Kingsway Square, London, ZX1 4GP")

    @Test
    fun `main contact card has the expected title, action and rows`() {
        val orgLandlord = MockLandlordData.createOrgLandlord()
        val viewModel = OrganisationalLandlordContactsViewModel(orgLandlord, emptyList())

        val card = viewModel.mainContactCard
        assertEquals("landlordDetails.org.mainContactHeading", card.title)
        assertEquals("forms.links.change", card.actions!!.single().text)
        assertEquals("#", card.actions!!.single().url)
        assertEquals(
            listOf("landlordDetails.org.mainContactName", "landlordDetails.org.mainContactEmail", "landlordDetails.org.mainContactPhone"),
            card.summaryList.map { it.fieldHeading },
        )
    }

    @Test
    fun `lead trustee card is null for a non-trust organisation`() {
        val orgLandlord = MockLandlordData.createOrgLandlord(isTrust = false)
        assertNull(OrganisationalLandlordContactsViewModel(orgLandlord, emptyList()).leadTrusteeCard)
    }

    @Test
    fun `lead trustee card is present with rows for a trust`() {
        val orgLandlord =
            MockLandlordData.createOrgLandlord(
                isTrust = true,
                leadTrusteeName = "Anita Locke",
                leadTrusteeDateOfBirth = LocalDate.of(2001, 3, 8),
                leadTrusteeEmail = "anita.locke@keystoneliving.com",
                leadTrusteePhoneNumber = "0123456789",
                leadTrusteeAddress = address,
            )
        val card = OrganisationalLandlordContactsViewModel(orgLandlord, emptyList()).leadTrusteeCard!!
        assertEquals("landlordDetails.org.leadTrusteeHeading", card.title)
        assertEquals(
            "${UpdateLeadTrusteeController.UPDATE_LEAD_TRUSTEE_ROUTE}/${LeadTrusteeNameStep.ROUTE_SEGMENT}",
            card.actions!!.single().url,
        )
        assertEquals(
            listOf(
                "landlordDetails.org.leadTrusteeName",
                "landlordDetails.org.leadTrusteeDateOfBirth",
                "landlordDetails.org.leadTrusteeEmail",
                "landlordDetails.org.leadTrusteePhone",
                "landlordDetails.org.leadTrusteeAddress",
            ),
            card.summaryList.map { it.fieldHeading },
        )
    }

    @Test
    fun `governing body cards are numbered per member with role, name, dob and address`() {
        val orgLandlord = MockLandlordData.createOrgLandlord(isCompany = false, companyNumber = null)
        val members =
            listOf(
                OrganisationGoverningBodyMember(
                    orgLandlord,
                    GoverningBodyMemberType.DIRECTOR,
                    "Anita Locke",
                    LocalDate.of(1874, 3, 18),
                    address,
                ),
                OrganisationGoverningBodyMember(
                    orgLandlord,
                    GoverningBodyMemberType.PARTNER,
                    "Omar Hassan",
                    LocalDate.of(2001, 3, 8),
                    address,
                ),
            )
        val viewModel = OrganisationalLandlordContactsViewModel(orgLandlord, members)

        assertEquals(true, viewModel.showGoverningBody)
        assertEquals(2, viewModel.governingBodyMemberCards.size)
        val first = viewModel.governingBodyMemberCards[0]
        assertEquals("landlordDetails.org.governingBody.memberCardTitle.director", first.title)
        assertEquals("1", first.cardNumber)
        assertEquals(GoverningBodyMemberType.DIRECTOR, first.summaryList[0].fieldValue)
        assertEquals(
            listOf(
                "landlordDetails.org.governingBody.role",
                "landlordDetails.org.governingBody.memberName",
                "landlordDetails.org.governingBody.memberDateOfBirth",
                "landlordDetails.org.governingBody.memberAddress",
            ),
            first.summaryList.map { it.fieldHeading },
        )
        assertEquals("2", viewModel.governingBodyMemberCards[1].cardNumber)
    }

    @Test
    fun `governing body section is hidden for a registered company org landlord`() {
        val viewModel =
            OrganisationalLandlordContactsViewModel(
                MockLandlordData.createOrgLandlord(isCompany = true, companyNumber = "12345678"),
                emptyList(),
            )
        assertEquals(false, viewModel.showGoverningBody)
        assertEquals(0, viewModel.governingBodyMemberCards.size)
    }

    @Test
    fun `registration contact card has no action and the expected rows`() {
        val card = OrganisationalLandlordContactsViewModel(MockLandlordData.createOrgLandlord(), emptyList()).registrationContactCard
        assertEquals("landlordDetails.org.registrationContactHeading", card.title)
        assertNull(card.actions)
        assertEquals(
            listOf(
                "landlordDetails.org.registrationContactName",
                "landlordDetails.org.registrationContactDateOfBirth",
                "landlordDetails.org.registrationContactEmail",
                "landlordDetails.org.registrationContactPhone",
            ),
            card.summaryList.map { it.fieldHeading },
        )
    }
}
