package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.CORRESPONDENCE_ADDRESS
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.CorrespondenceEmailOption
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.UPDATE_CONFLICT_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.PropertyDetailsPageLandlordView
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceEmailCyaPagePropertyDetailsUpdate
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages.CorrespondenceEmailFormPagePropertyDetailsUpdate
import java.util.regex.Pattern
import kotlin.test.assertEquals

class UpdateCorrespondenceEmailJourneyTests : IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    private val propertyOwnershipId = 1L
    private val urlArguments = mapOf("propertyOwnershipId" to propertyOwnershipId.toString())

    @BeforeEach
    fun enableFeatureFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(CORRESPONDENCE_ADDRESS)
    }

    @ParameterizedTest
    @EnumSource(CorrespondenceEmailOption::class)
    fun `a landlord can save either email option only after confirming CYA`(
        option: CorrespondenceEmailOption,
        page: Page,
    ) {
        val originalEmail = storedEmail()
        val emailPage = startJourney(page)
        val expectedEmail =
            when (option) {
                CorrespondenceEmailOption.ACCOUNT_EMAIL -> {
                    emailPage.submitAccountEmail()
                    "alex.surname@example.com"
                }
                CorrespondenceEmailOption.DIFFERENT_EMAIL -> {
                    emailPage.submitDifferentEmail("updated@example.com")
                    "updated@example.com"
                }
            }
        val cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(cyaPage.summaryList.emailRow.value).hasText(expectedEmail)
        assertEquals(originalEmail, storedEmail())

        cyaPage.confirm()

        var propertyPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertThat(propertyPage.propertyDetailsSummaryList.contactEmailAddressRow.value).hasText(expectedEmail)
        assertEquals(expectedEmail, storedEmail())
        propertyPage = navigator.goToPropertyDetailsLandlordView(propertyOwnershipId)
        assertThat(propertyPage.propertyDetailsSummaryList.contactEmailAddressRow.value).hasText(expectedEmail)
    }

    @Test
    fun `a landlord can change the email in a child journey before confirming the update`(page: Page) {
        val originalEmail = storedEmail()
        var emailPage = startJourney(page)
        emailPage.submitDifferentEmail("first@example.com")
        var cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)

        cyaPage.summaryList.emailRow.clickFirstActionLinkAndWait()
        emailPage = assertPageIs(page, CorrespondenceEmailFormPagePropertyDetailsUpdate::class, urlArguments)
        emailPage.submitDifferentEmail("second@example.com")

        cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(cyaPage.summaryList.emailRow.value).hasText("second@example.com")
        assertEquals(originalEmail, storedEmail())
        cyaPage.confirm()

        val propertyPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertThat(propertyPage.propertyDetailsSummaryList.contactEmailAddressRow.value).hasText("second@example.com")
        assertEquals("second@example.com", storedEmail())
    }

    @Test
    fun `back from a child journey discards an unsubmitted change`(page: Page) {
        val originalEmail = storedEmail()
        var emailPage = startJourney(page)
        emailPage.submitDifferentEmail("first@example.com")
        var cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)
        cyaPage.summaryList.emailRow.clickFirstActionLinkAndWait()
        emailPage = assertPageIs(page, CorrespondenceEmailFormPagePropertyDetailsUpdate::class, urlArguments)
        emailPage.form.differentEmailInput.fill("discarded@example.com")

        emailPage.backLink.clickAndWait()

        cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)
        assertThat(cyaPage.summaryList.emailRow.value).hasText("first@example.com")
        assertEquals(originalEmail, storedEmail())
    }

    @Test
    fun `back from the initial question returns to the property record without saving`(page: Page) {
        val originalEmail = storedEmail()
        val emailPage = startJourney(page)

        emailPage.backLink.clickAndWait()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(originalEmail, storedEmail())
    }

    @Test
    fun `submitting the existing email follows the normal confirmation flow`(page: Page) {
        val originalEmail = storedEmail()
        val emailPage = startJourney(page)
        emailPage.submitDifferentEmail(originalEmail)
        val cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)

        cyaPage.confirm()

        assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertEquals(originalEmail, storedEmail())
    }

    @Test
    fun `a stale confirmation preserves the newer email and discards the update journey`(page: Page) {
        val emailPage = startJourney(page)
        emailPage.submitDifferentEmail("stale@example.com")
        val cyaPage = assertPageIs(page, CorrespondenceEmailCyaPagePropertyDetailsUpdate::class, urlArguments)
        val staleCyaUrl = page.url()
        val propertyOwnership = propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)!!
        propertyOwnership.updateCorrespondenceEmail("newer@example.com")
        propertyOwnershipRepository.saveAndFlush(propertyOwnership)

        cyaPage.confirm()

        assertThat(page).hasURL(Pattern.compile(".*$UPDATE_CONFLICT_ERROR_ROUTE"))
        assertEquals("newer@example.com", storedEmail())

        page.navigate(staleCyaUrl)

        val propertyPage = assertPageIs(page, PropertyDetailsPageLandlordView::class, urlArguments)
        assertThat(propertyPage.propertyDetailsSummaryList.contactEmailAddressRow.value).hasText("newer@example.com")
    }

    private fun startJourney(page: Page): CorrespondenceEmailFormPagePropertyDetailsUpdate {
        navigator
            .goToPropertyDetailsLandlordView(propertyOwnershipId)
            .propertyDetailsSummaryList.contactEmailAddressRow
            .clickFirstActionLinkAndWait()
        return assertPageIs(page, CorrespondenceEmailFormPagePropertyDetailsUpdate::class, urlArguments)
    }

    private fun storedEmail(): String = propertyOwnershipRepository.findByIdAndIsActiveTrue(propertyOwnershipId)!!.correspondenceEmail
}
