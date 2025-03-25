package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.services.LandlordService

@Sql("/data-local.sql")
class LandlordDeregistrationJourneyTests : IntegrationTest() {
    @MockBean
    lateinit var landlordService: LandlordService

    @Test
    fun `User with properties can navigate the whole journey`(page: Page) {
        whenever(landlordService.getLandlordHasRegisteredProperties(anyString())).thenReturn(true)
        val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
        assertThat(areYouSurePage.form.fieldsetHeading)
            .containsText("Are you sure you want to delete your account and all your properties on the database?")
        areYouSurePage.submitWantsToProceed()

        // TODO PRSD-704 - redirect to reason page if the user with properties selects "yes"
        assertPageIs(page, LandlordDetailsPage::class)
    }

    @Test
    fun `User with no properties can navigate the whole journey`(page: Page) {
        whenever(landlordService.getLandlordHasRegisteredProperties(anyString())).thenReturn(false)

        val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
        assertThat(areYouSurePage.form.fieldsetHeading).containsText("Are you sure you want to delete your account from the database?")
        areYouSurePage.submitWantsToProceed()

        // TODO PRSD-705 - redirect to confirmation page if user with no properties selects "yes" val nextPage =
        assertTrue(
            areYouSurePage.page
                .url()
                .toString()
                .contains("register-as-a-landlord"),
        )
    }

    @ParameterizedTest(name = "and userHasRegisteredProperties = {0}")
    @ValueSource(booleans = [true, false])
    fun `User is returned to the landlord details page if they submit No`(
        userHasRegisteredProperties: Boolean,
        page: Page,
    ) {
        whenever(landlordService.getLandlordHasRegisteredProperties(anyString())).thenReturn(userHasRegisteredProperties)
        val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
        areYouSurePage.submitDoesNotWantToProceed()

        assertPageIs(page, LandlordDetailsPage::class)
    }

    @ParameterizedTest(name = "and userHasRegisteredProperties = {0}")
    @ValueSource(booleans = [true, false])
    fun `User is returned to the landlord details page if they click the back link`(
        userHasRegisteredProperties: Boolean,
        page: Page,
    ) {
        whenever(landlordService.getLandlordHasRegisteredProperties(anyString())).thenReturn(userHasRegisteredProperties)
        val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
        areYouSurePage.backLink.clickAndWait()
        assertPageIs(page, LandlordDetailsPage::class)
    }

    @ParameterizedTest
    @CsvSource(
        "true, Select whether you want to delete your landlord record and properties",
        "false, Select whether you want to delete your account from the database",
    )
    fun `Submitting with no option selected returns an error`(
        userHasRegisteredProperties: Boolean,
        expectedErrorMessage: String,
        page: Page,
    ) {
        whenever(landlordService.getLandlordHasRegisteredProperties(anyString())).thenReturn(userHasRegisteredProperties)

        val areYouSurePage = navigator.goToLandlordDeregistrationAreYouSurePage()
        areYouSurePage.form.submit()
        assertThat(areYouSurePage.form.getErrorMessage("wantsToProceed"))
            .containsText(expectedErrorMessage)
    }
}
