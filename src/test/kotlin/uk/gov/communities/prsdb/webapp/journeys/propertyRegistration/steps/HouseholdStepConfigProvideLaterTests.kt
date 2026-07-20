package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.journeys.JourneyMetadata

class HouseholdStepConfigProvideLaterTests {
    private val realFeatureFlagManager = FeatureFlagManager(uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser())

    private val routeSegment = HouseholdStep.ROUTE_SEGMENT

    private fun makeState(data: Map<String, Any?>?): HouseholdsAndTenantsState = object : HouseholdsAndTenantsState {
        override val households = HouseholdStep(HouseholdStepConfig(realFeatureFlagManager))
        override val tenants = TenantsStep(TenantsStepConfig())
        override val provideTenancyDetailsLaterStep = ProvideTenancyDetailsLaterStep(ProvideTenancyDetailsLaterStepConfig())

        override fun getStepData(key: String) = data

        override fun addStepData(key: String, value: uk.gov.communities.prsdb.webapp.journeys.FormData) {}

        override fun clearStepData(key: String) {}

        override fun getSubmittedStepData(): Map<String, Any?> = mapOf()

        override val journeyId: String = "test"
        override val journeyMetadata: JourneyMetadata = JourneyMetadata.createNew("test")

        override fun deleteJourney() {}

        override fun initializeState(seed: Any?): String = "test"

        override fun initializeOrRestoreState(seed: Any?) = "test"

        override fun save(): uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState = uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState(uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser("test"), "test")

        override fun setJourneyId(newJourneyId: String) {}

        override fun copyJourneyTo(newJourneyId: String) {}

        override fun bindKeyRegistry(registry: uk.gov.communities.prsdb.webapp.journeys.DelegateKeyRegistry) {}
    }

    @Test
    fun `Mode returns null when form model is not present`() {
        val stepConfig = HouseholdStepConfig(realFeatureFlagManager)
        stepConfig.routeSegment = HouseholdStep.ROUTE_SEGMENT
        stepConfig.validator = object : org.springframework.validation.Validator {
            override fun supports(clazz: Class<*>): Boolean = true
            override fun validate(target: Any, errors: org.springframework.validation.Errors) {}
        }
        val state = makeState(null)

        val result = stepConfig.mode(state)

        assertNull(result)
    }

    @Test
    fun `Mode returns COMPLETE when numberOfHouseholds present and action is not provideThisLater`() {
        val stepConfig = HouseholdStepConfig(realFeatureFlagManager)
        stepConfig.routeSegment = HouseholdStep.ROUTE_SEGMENT
        stepConfig.validator = object : org.springframework.validation.Validator {
            override fun supports(clazz: Class<*>): Boolean = true
            override fun validate(target: Any, errors: org.springframework.validation.Errors) {}
        }
        val state = makeState(mapOf("numberOfHouseholds" to "2", "action" to "continue"))

        val result = stepConfig.mode(state)

        assertEquals(HouseholdMode.COMPLETE, result)
    }

    @Test
    fun `Mode returns PROVIDE_THIS_LATER when action is provideThisLater`() {
        val stepConfig = HouseholdStepConfig(realFeatureFlagManager)
        stepConfig.routeSegment = HouseholdStep.ROUTE_SEGMENT
        stepConfig.validator = object : org.springframework.validation.Validator {
            override fun supports(clazz: Class<*>): Boolean = true
            override fun validate(target: Any, errors: org.springframework.validation.Errors) {}
        }
        val state = makeState(mapOf("numberOfHouseholds" to "", "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME))

        val result = stepConfig.mode(state)

        assertEquals(HouseholdMode.PROVIDE_THIS_LATER, result)
    }
}
