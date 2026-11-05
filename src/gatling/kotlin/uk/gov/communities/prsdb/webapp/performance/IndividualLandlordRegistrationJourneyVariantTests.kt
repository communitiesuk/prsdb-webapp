package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IndividualLandlordRegistrationJourneyVariantTests {
    @Test
    fun `selected address and unverified identity use manual identity steps without changing the default journey`() {
        val options =
            IndividualLandlordRegistrationJourney.Options(
                includeCoreIdentity = false,
                manualAddress = false,
                requestNamePrefix = "Selected and unverified: ",
            )

        val names = IndividualLandlordRegistrationJourney.requestNames(options)

        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.ADDRESS_SELECTION_SELECTED_SUBMIT_REQUEST}")
        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.IDENTITY_NOT_VERIFIED_FORM_REQUEST}")
        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.IDENTITY_NOT_VERIFIED_SUBMIT_REQUEST}")
        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.NAME_FORM_REQUEST}")
        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.NAME_SUBMIT_REQUEST}")
        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.DATE_OF_BIRTH_FORM_REQUEST}")
        assertContains(names, "Selected and unverified: ${IndividualLandlordRegistrationJourney.DATE_OF_BIRTH_SUBMIT_REQUEST}")
        assertFalse(names.any { it.contains(IndividualLandlordRegistrationJourney.CONFIRM_IDENTITY_FORM_REQUEST) })
        assertFalse(names.any { it.contains(IndividualLandlordRegistrationJourney.MANUAL_ADDRESS_FORM_REQUEST) })
    }

    @Test
    fun `default individual registration continues to use verified identity and manual address`() {
        val defaults = IndividualLandlordRegistrationJourney.Options()
        val names = IndividualLandlordRegistrationJourney.requestNames(defaults)

        assertTrue(names.contains(IndividualLandlordRegistrationJourney.CONFIRM_IDENTITY_FORM_REQUEST))
        assertTrue(names.contains(IndividualLandlordRegistrationJourney.MANUAL_ADDRESS_FORM_REQUEST))
        assertFalse(names.contains(IndividualLandlordRegistrationJourney.IDENTITY_NOT_VERIFIED_FORM_REQUEST))
        assertFalse(names.contains(IndividualLandlordRegistrationJourney.ADDRESS_SELECTION_SELECTED_SUBMIT_REQUEST))
        assertTrue(names.containsAll(IndividualLandlordRegistrationJourney.requestNames))
    }

    @Test
    fun `post registration authentication is measured by its callback because refresh authentication starts it`() {
        val prefix = IndividualLandlordRegistrationJourney.POST_REGISTRATION_AUTHENTICATION_PREFIX
        val names =
            IndividualLandlordRegistrationJourney.requestNames(
                IndividualLandlordRegistrationJourney.Options(requestNamePrefix = "Variant: "),
            )

        assertContains(names, "Variant: $prefix: callback")
        assertFalse(names.contains("Variant: $prefix: start"))
        assertEquals(names.size, names.toSet().size)
    }

    @Test
    fun `embedded registration starts at the host privacy step and leaves completion to the host journey`() {
        val names =
            IndividualLandlordRegistrationJourney.requestNames(
                IndividualLandlordRegistrationJourney.Options(embeddedEntryUrlKey = "hostEntry", requestNamePrefix = "Host: "),
            )

        assertContains(names, "Host: ${IndividualLandlordRegistrationJourney.PRIVACY_FORM_REQUEST}")
        assertContains(names, "Host: ${IndividualLandlordRegistrationJourney.REAUTHENTICATION_REQUEST}")
        listOf(
            IndividualLandlordRegistrationJourney.START_REQUEST,
            IndividualLandlordRegistrationJourney.PRIVACY_INITIALIZE_REQUEST,
            IndividualLandlordRegistrationJourney.CONFIRMATION_REQUEST,
            IndividualLandlordRegistrationJourney.DASHBOARD_REQUEST,
        ).forEach { assertFalse(names.contains("Host: $it")) }
    }
}
