package uk.gov.communities.prsdb.webapp.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrganisationalLandlordUserInvitationController.Companion.ACCEPT_INVITATION_ROUTE
import java.util.UUID

class AbsoluteUrlProviderTests {
    @Test
    fun `buildLandlordDashboardUri prepends https when configured base URL has no scheme`() {
        val provider = createProvider(landlordBaseUrl = "example.test/landlord")

        val uri = provider.buildLandlordDashboardUri()

        assertThat(uri.toString()).startsWith("https://example.test/landlord")
    }

    @Test
    fun `buildLandlordDashboardUri preserves an explicit https scheme`() {
        val provider = createProvider(landlordBaseUrl = "https://example.test/landlord")

        val uri = provider.buildLandlordDashboardUri()

        assertThat(uri.toString()).startsWith("https://example.test/landlord")
    }

    @Test
    fun `buildLandlordDashboardUri preserves an explicit http scheme`() {
        val provider = createProvider(landlordBaseUrl = "http://localhost:8080/landlord")

        val uri = provider.buildLandlordDashboardUri()

        assertThat(uri.toString()).startsWith("http://localhost:8080/landlord")
    }

    @Test
    fun `buildLandlordDashboardUri clears base URL query parameters when the controller URI has none`() {
        val provider = createProvider(landlordBaseUrl = "https://example.test/landlord?source=base")

        val uri = provider.buildLandlordDashboardUri()

        assertThat(uri.query).isNull()
    }

    @Test
    fun `buildTeamMembersUri builds the landlord team members URL`() {
        val provider = createProvider(landlordBaseUrl = "https://example.test")

        val uri = provider.buildTeamMembersUri()

        assertThat(uri.toString()).isEqualTo("https://example.test/landlord/team-members")
    }

    @Test
    fun `buildLocalCouncilDashboardUri prepends https when configured base URL has no scheme`() {
        val provider = createProvider(localCouncilBaseUrl = "example.test/local-council")

        val uri = provider.buildLocalCouncilDashboardUri()

        assertThat(uri.toString()).startsWith("https://example.test/local-council")
    }

    @Test
    fun `buildJointLandlordInvitationUri prepends https when configured base URL has no scheme`() {
        val provider = createProvider(landlordBaseUrl = "example.test/landlord")

        val uri = provider.buildJointLandlordInvitationUri("token-123")

        assertThat(uri.toString()).isEqualTo("https://example.test/landlord/$JOINT_LANDLORD_INVITATION_PATH_SEGMENT?token=token-123")
    }

    @Test
    fun `buildOrganisationalLandlordInvitationUri returns the accept invitation url with the token`() {
        val provider = createProvider(landlordBaseUrl = "example.test/landlord")

        val uri = provider.buildOrganisationalLandlordInvitationUri("token-123")

        assertThat(uri.toString()).isEqualTo("https://example.test$ACCEPT_INVITATION_ROUTE?token=token-123")
    }

    @Test
    fun `buildJointLandlordInvitationUri replaces base URL query parameters with the controller query`() {
        val provider = createProvider(landlordBaseUrl = "https://example.test/landlord?source=base&token=old")

        val uri = provider.buildJointLandlordInvitationUri("token-123")

        assertThat(uri.toString()).isEqualTo("https://example.test/landlord/$JOINT_LANDLORD_INVITATION_PATH_SEGMENT?token=token-123")
    }

    @Test
    fun `buildLettingAgentPropertyDetailsUri prepends https when configured base URL has no scheme`() {
        val provider = createProvider(landlordBaseUrl = "example.test/landlord")
        val token = UUID.fromString("3334abcd-5678-abcd-1234-567abcd1111a")

        val uri = provider.buildLettingAgentPropertyDetailsUri(token)

        assertThat(
            uri.toString(),
        ).isEqualTo("https://example.test/landlord/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/$token")
    }

    @Test
    fun `buildPropertyRegistrationPaymentReturnUri returns the payment return step url for the journey and payment`() {
        val provider = createProvider(landlordBaseUrl = "example.test/landlord")

        val uri = provider.buildPropertyRegistrationPaymentReturnUri("journey-123", "payment-reference")

        assertThat(uri.toString())
            .isEqualTo(
                "https://example.test/landlord/register-property/payment-return?journeyId=journey-123&paymentReference=payment-reference",
            )
    }

    private fun createProvider(
        landlordBaseUrl: String = "https://example.test/landlord",
        localCouncilBaseUrl: String = "https://example.test/local-council",
    ): AbsoluteUrlProvider = AbsoluteUrlProvider(landlordBaseUrl, localCouncilBaseUrl)
}
