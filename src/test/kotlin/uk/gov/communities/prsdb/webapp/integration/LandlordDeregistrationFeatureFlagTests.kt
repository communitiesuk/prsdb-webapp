package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterOrganisationalLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig.AreYouSureStep
import kotlin.test.assertEquals
import uk.gov.communities.prsdb.webapp.journeys.organisationalLandlordDeregistration.stepConfig.AreYouSureStep as OrgAreYouSureStep

// TODO PDJB-2149: Delete this test class along with the landlord deregistration journeys when the ACTIVE_INACTIVE flag is removed
class LandlordDeregistrationFeatureFlagTests :
    IntegrationTestWithImmutableData("data-mockuser-landlord-with-properties-and-incomplete-property.sql") {
    @BeforeEach
    fun enableActiveInactive() {
        featureFlagManager.enableFeature(ACTIVE_INACTIVE)
    }

    @Test
    fun `the landlord details page does not show the delete account link`() {
        val detailsPage = navigator.goToLandlordDetails()

        assertThat(detailsPage.deleteAccountLink).isHidden()
    }

    @Test
    fun `the landlord deregistration journey is not available`() {
        val response =
            navigator.navigate("${DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE}/${AreYouSureStep.ROUTE_SEGMENT}")

        assertEquals(404, response?.status())
    }
}

// TODO PDJB-2149: Delete this test class along with the landlord deregistration journeys when the ACTIVE_INACTIVE flag is removed
@WithOrgLandlordProfile
class OrgLandlordDeregistrationFeatureFlagTests : IntegrationTestWithImmutableData("data-mockuser-org-landlord-trust.sql") {
    // TODO PDJB-1828: Remove this setup when the MULTI_USER_ORGANISATIONS feature flag is removed
    @BeforeEach
    fun enableFeatures() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        featureFlagManager.enableFeature(ACTIVE_INACTIVE)
    }

    @Test
    fun `the org landlord details page does not show the delete organisation link`() {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertThat(detailsPage.deleteOrganisationLink).isHidden()
    }

    @Test
    fun `the organisational landlord deregistration journey is not available`() {
        val response =
            navigator.navigate(
                "${DeregisterOrganisationalLandlordController.ORGANISATIONAL_LANDLORD_DEREGISTRATION_ROUTE}/" +
                    OrgAreYouSureStep.ROUTE_SEGMENT,
            )

        assertEquals(404, response?.status())
    }
}
