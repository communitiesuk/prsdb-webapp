package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.CheckAnswersPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyChangeInterruptionPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.OccupancyFormPagePropertyRegistration
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyStateSessionBuilder

class PropertyRegistrationOccupancyChangeInterruptionSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @BeforeEach
    fun enableRestructureAndSkippingAndDelegateFlags() {
        featureFlagManager.enableFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)
        featureFlagManager.enableFeature(DELEGATE_TO_LETTING_AGENT)
    }

    @Test
    fun `clicking Go back on the interruption page returns to the occupancy page`(page: Page) {
        val interruptionPage = goToOccupancyChangeInterruptionPage(page)

        interruptionPage.goBackLink.clickAndWait()

        assertPageIs(page, OccupancyFormPagePropertyRegistration::class)
    }

    private fun goToOccupancyChangeInterruptionPage(page: Page): OccupancyChangeInterruptionPagePropertyRegistration {
        val taskListPage =
            navigator.goToRestructuredPropertyRegistrationTaskList(
                PropertyStateSessionBuilder.beforePropertyRegistrationCheckAnswersDelegatedToLettingAgent(),
            )
        taskListPage.clickSubmitYourRegistrationTaskWithName("Check and submit your answers")
        val checkAnswersPage = assertPageIs(page, CheckAnswersPagePropertyRegistration::class)

        checkAnswersPage.summaryList.occupancyQuestionRow.clickFirstActionLinkAndWait()
        val occupancyPage = assertPageIs(page, OccupancyFormPagePropertyRegistration::class)

        occupancyPage.submitIsVacant()
        return assertPageIs(page, OccupancyChangeInterruptionPagePropertyRegistration::class)
    }
}
