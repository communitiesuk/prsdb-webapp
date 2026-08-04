package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

class OrganisationLandlordDetailsBasicPageTests : IntegrationTestWithImmutableData("data-mockuser-org-landlord-basic.sql") {
    @Test
    fun `organisation contacts cards show only the main and registration contacts for a basic org landlord`() {
        val detailsPage = navigator.goToOrgLandlordDetails()
        detailsPage.tabs.goToOrganisationContacts()

        val mainContact = detailsPage.mainContactCard
        assertThat(mainContact.title).containsText("Main contact")
        assertThat(mainContact.summaryList.nameRow.value).containsText("Sam Main-Contact")
        assertThat(mainContact.summaryList.emailRow.value).containsText("sam.maincontact@meadowbrook.example")
        assertThat(mainContact.summaryList.phoneNumberRow.value).containsText("02555555555")

        val registrationContact = detailsPage.registrationContactCard
        assertThat(registrationContact.title).containsText("Registration contact")
        assertThat(registrationContact.summaryList.nameRow.value).containsText("Riya Registrant")
        assertThat(registrationContact.summaryList.dateOfBirthRow.value).containsText("15 June 1985")
        assertThat(registrationContact.summaryList.emailRow.value).containsText("riya.registrant@meadowbrook.example")
        assertThat(registrationContact.summaryList.phoneNumberRow.value).containsText("01444444444")

        assertThat(detailsPage.leadTrusteeCard).isHidden()
        assertThat(detailsPage.governingBodyMembersLink).isHidden()
        assertEquals(0, detailsPage.governingBodyMemberCardCount())
    }
}
