package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UpdatePropertyComplianceSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Nested
    inner class UpdateGasSafetyStepTests {
        @Test
        fun `Submitting with no option selected returns an error`() {
            val updateGasSafetyPage = navigator.goToPropertyComplianceUpdateUpdateGasSafetyPage(PROPERTY_OWNERSHIP_ID)
            updateGasSafetyPage.form.submit()
            assertThat(
                updateGasSafetyPage.form.getErrorMessage(),
            ).containsText("Select whether you want to add a new certificate or exemption")
        }
    }

    companion object {
        private const val PROPERTY_OWNERSHIP_ID = 8L
    }
}
