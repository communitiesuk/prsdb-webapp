package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI

class OrganisationalLandlordInvitationAcceptedEmailTests {
    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `confirmation email uses the shared template and role-specific personalisation values`(isAdmin: Boolean) {
        // Arrange
        val email =
            OrganisationalLandlordInvitationAcceptedEmail(
                recipientName = "Jane Smith",
                roleName = if (isAdmin) "administrator" else "editor",
                organisationName = "Example Organisation",
                isAdmin = isAdmin,
                dashboardUrl = URI("https://www.example.com/landlord/dashboard"),
            )

        // Act
        val personalisation = email.toHashMap()

        // Assert
        assertEquals(EmailTemplate.ORGANISATIONAL_LANDLORD_INVITATION_ACCEPTED_EMAIL, email.template)
        assertEquals(
            hashMapOf(
                "recipient name" to "Jane Smith",
                "organisation name" to "Example Organisation",
                "role name" to if (isAdmin) "administrator" else "editor",
                "isAdmin" to if (isAdmin) "yes" else "no",
                "dashboard url" to "https://www.example.com/landlord/dashboard",
            ),
            personalisation,
        )
    }
}
