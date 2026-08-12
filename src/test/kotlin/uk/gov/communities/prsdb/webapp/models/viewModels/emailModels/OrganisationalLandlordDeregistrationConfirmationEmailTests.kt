package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OrganisationalLandlordDeregistrationConfirmationEmailTests {
    @Test
    fun `toHashMap includes the registrant and organisation names`() {
        val email =
            OrganisationalLandlordDeregistrationConfirmationEmail(
                registrantName = "Sam Smith",
                organisationName = "Example Housing Association",
            )

        assertEquals(EmailTemplate.ORGANISATIONAL_LANDLORD_DEREGISTRATION_CONFIRMATION_EMAIL, email.template)
        assertEquals(
            hashMapOf(
                "registrant name" to "Sam Smith",
                "organisation name" to "Example Housing Association",
            ),
            email.toHashMap(),
        )
    }
}
