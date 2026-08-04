package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

class OrganisationLandlordDetailsPageTests : IntegrationTestWithImmutableData("data-mockuser-org-landlord-trust.sql") {
    @Test
    fun `organisation contacts cards show the correct rows for each contact type of a trust org landlord`() {
        val detailsPage = navigator.goToOrgLandlordDetails()
        detailsPage.tabs.goToOrganisationContacts()

        val mainContact = detailsPage.mainContactCard
        assertThat(mainContact.title).containsText("Main contact")
        assertThat(mainContact.summaryList.nameRow.value).containsText("Sam Main-Contact")
        assertThat(mainContact.summaryList.emailRow.value).containsText("sam.maincontact@keystoneliving.com")
        assertThat(mainContact.summaryList.phoneNumberRow.value).containsText("02222222222")

        val leadTrustee = detailsPage.leadTrusteeCard
        assertThat(leadTrustee.title).containsText("Lead trustee")
        assertThat(leadTrustee.summaryList.nameRow.value).containsText("Anita Locke")
        assertThat(leadTrustee.summaryList.dateOfBirthRow.value).containsText("8 March 2001")
        assertThat(leadTrustee.summaryList.emailRow.value).containsText("anita.locke@keystoneliving.com")
        assertThat(leadTrustee.summaryList.phoneNumberRow.value).containsText("03333333333")
        assertThat(leadTrustee.summaryList.addressRow.value).containsText("88 Kingsway Square")

        assertThat(detailsPage.governingBodyMembersLink).isVisible()
        assertEquals(2, detailsPage.governingBodyMemberCardCount())

        val director = detailsPage.governingBodyMemberCard("1. Director")
        assertThat(director.title).containsText("1. Director")
        assertThat(director.summaryList.roleRow.value).containsText("Director")
        assertThat(director.summaryList.nameRow.value).containsText("David Director")
        assertThat(director.summaryList.dateOfBirthRow.value).containsText("18 March 1974")
        assertThat(director.summaryList.addressRow.value).containsText("12 Director Avenue")

        val partner = detailsPage.governingBodyMemberCard("2. Partner")
        assertThat(partner.title).containsText("2. Partner")
        assertThat(partner.summaryList.roleRow.value).containsText("Partner")
        assertThat(partner.summaryList.nameRow.value).containsText("Omar Hassan")
        assertThat(partner.summaryList.dateOfBirthRow.value).containsText("8 March 2001")
        assertThat(partner.summaryList.addressRow.value).containsText("34 Partner Lane")

        val registrationContact = detailsPage.registrationContactCard
        assertThat(registrationContact.title).containsText("Registration contact")
        assertThat(registrationContact.summaryList.nameRow.value).containsText("Priya Registrant")
        assertThat(registrationContact.summaryList.dateOfBirthRow.value).containsText("1 January 1990")
        assertThat(registrationContact.summaryList.emailRow.value).containsText("priya.registrant@keystoneliving.com")
        assertThat(registrationContact.summaryList.phoneNumberRow.value).containsText("01111111111")
    }
}
