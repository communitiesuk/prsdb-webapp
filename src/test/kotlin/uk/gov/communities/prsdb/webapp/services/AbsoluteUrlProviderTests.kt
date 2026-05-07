package uk.gov.communities.prsdb.webapp.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORD_INVITATION_PATH_SEGMENT

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
    fun `buildLocalCouncilDashboardUri prepends https when configured base URL has no scheme`() {
        val provider = createProvider(localCouncilBaseUrl = "example.test/local-council")

        val uri = provider.buildLocalCouncilDashboardUri()

        assertThat(uri.toString()).startsWith("https://example.test/local-council")
    }

    @Test
    fun `buildJointLandlordInvitationUri prepends https when configured base URL has no scheme`() {
        val provider = createProvider(landlordBaseUrl = "example.test/landlord")

        val uri = provider.buildJointLandlordInvitationUri("token-123")

        assertThat(uri.toString()).isEqualTo("https://example.test/landlord/$JOINT_LANDLORD_INVITATION_PATH_SEGMENT/token-123")
    }

    private fun createProvider(
        landlordBaseUrl: String = "https://example.test/landlord",
        localCouncilBaseUrl: String = "https://example.test/local-council",
    ): AbsoluteUrlProvider = AbsoluteUrlProvider(landlordBaseUrl, localCouncilBaseUrl)
}
