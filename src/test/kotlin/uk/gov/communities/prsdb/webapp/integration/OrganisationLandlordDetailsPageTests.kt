package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

class OrganisationLandlordDetailsPageTests : IntegrationTestWithImmutableData("data-mockuser-org-landlord-trust.sql") {
    @Test
    fun `organisation contacts cards are displayed for a trust org landlord`() {
        val detailsPage = navigator.goToOrgLandlordDetails()
        detailsPage.tabs.goToOrganisationContacts()

        assertThat(detailsPage.mainContactCard.title).containsText("Main contact")
        assertThat(detailsPage.mainContactCard).containsText("Sam Main-Contact")

        assertThat(detailsPage.leadTrusteeCard.title).containsText("Lead trustee")
        assertThat(detailsPage.leadTrusteeCard).containsText("Anita Locke")

        assertThat(detailsPage.registrationContactCard.title).containsText("Registration contact")
        assertThat(detailsPage.registrationContactCard).containsText("Priya Registrant")

        assertThat(detailsPage.governingBodyMembersLink).isVisible()
        assertEquals(2, detailsPage.governingBodyMemberCardCount())
    }
}
