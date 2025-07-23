package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.services.FileUploader

class PropertyComplianceUpdateSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @MockitoBean
    private lateinit var fileUploader: FileUploader

    @MockitoBean
    private lateinit var epcRegisterClient: EpcRegisterClient

    @Nested
    inner class UpdateEpcStep {
        @Test
        fun `Submitting with no value entered returns an error`() {
            val propertyOwnershipId = 12L
            val updateEpcPage = navigator.goToPropertyComplianceUpdateUpdateEpcPage(propertyOwnershipId)
            updateEpcPage.form.submit()
            assertThat(updateEpcPage.form.getErrorMessage())
                .containsText("Select whether you want to add a new certificate or exemption")
        }
    }
}
