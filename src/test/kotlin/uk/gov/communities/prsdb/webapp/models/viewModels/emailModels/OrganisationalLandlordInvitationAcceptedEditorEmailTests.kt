package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class OrganisationalLandlordInvitationAcceptedEditorEmailTests {
    @Test
    fun `Editor confirmation email uses its template and personalisation values`() {
        val email =
            OrganisationalLandlordInvitationAcceptedEditorEmail(
                recipientName = "Jane Smith",
                organisationName = "Example Organisation",
                dashboardUrl = URI("https://www.prsd.gov.uk/landlord/dashboard"),
            )

        assertEquals(EmailTemplate.ORGANISATIONAL_LANDLORD_INVITATION_ACCEPTED_EDITOR_EMAIL, email.template)
        assertEquals(
            hashMapOf(
                "recipient name" to "Jane Smith",
                "organisation name" to "Example Organisation",
                "access level" to "editor",
                "dashboard url" to "https://www.prsd.gov.uk/landlord/dashboard",
            ),
            email.toHashMap(),
        )
    }
}
