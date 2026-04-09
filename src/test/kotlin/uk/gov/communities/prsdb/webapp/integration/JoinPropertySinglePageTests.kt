package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS

class JoinPropertySinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableJointLandlordsFlag() {
        featureFlagManager.enableFeature(JOINT_LANDLORDS)
    }

    @Test
    fun `the select property page shows validation error when no property selected`(page: Page) {
        val selectPropertyPage = navigator.skipToSelectPropertyPage()

        // Submit without selecting a property
        selectPropertyPage.form.submit()

        // Verify error message appears
        assertThat(selectPropertyPage.form.getErrorMessage()).containsText("Select the property you want to join")
    }
}
