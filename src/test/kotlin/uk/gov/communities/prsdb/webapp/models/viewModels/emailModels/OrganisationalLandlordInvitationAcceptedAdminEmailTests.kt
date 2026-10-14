package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class OrganisationalLandlordInvitationAcceptedAdminEmailTests {
    @Test
    fun `Admin confirmation email uses its template and personalisation values`() {
        val email =
            OrganisationalLandlordInvitationAcceptedAdminEmail(
                recipientName = "Jane Smith",
                organisationName = "Example Organisation",
                dashboardUrl = URI("https://www.prsd.gov.uk/landlord/dashboard"),
            )

        assertEquals(EmailTemplate.ORGANISATIONAL_LANDLORD_INVITATION_ACCEPTED_ADMIN_EMAIL, email.template)
        assertEquals(
            hashMapOf(
                "recipient name" to "Jane Smith",
                "organisation name" to "Example Organisation",
                "access level" to "administrator",
                "dashboard url" to "https://www.prsd.gov.uk/landlord/dashboard",
            ),
            email.toHashMap(),
        )
    }
}
