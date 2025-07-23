package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.services.FileUploader

class UpdatePropertyComplianceSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @Nested
    inner class UpdateGasSafetyStepTests {
        // TODO PRSD-1245: Re-enable this test when UpdateGasSafetyCertificateFormModel validation is re-enabled
        @Disabled
        @Test
        fun `Submitting with no option selected returns an error`() {
            val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
            updateGasSafetyPage.form.submit()
            assertThat(
                updateGasSafetyPage.form.getErrorMessage(),
            ).containsText("Select whether you want to add a new certificate or exemption")
        }
    }

    @Nested
    inner class UpdateEpcStep {
        @Test
        fun `Submitting with no value entered returns an error`() {
            val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(PROPERTY_OWNERSHIP_ID)
            updateEpcPage.form.submit()
            assertThat(updateEpcPage.form.getErrorMessage())
                .containsText("Select whether you want to add a new certificate or exemption")
        }
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 12L
    }
}
