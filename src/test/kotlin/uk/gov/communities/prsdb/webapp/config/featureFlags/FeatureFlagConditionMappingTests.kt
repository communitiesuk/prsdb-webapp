package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.servlet.mvc.condition.RequestCondition
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.config.conditions.FeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.conditions.InverseFeatureFlaggedRequestCondition
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagConditionMappingTests.TestController.Companion.FLAG_NAME
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.config.mappings.FeatureFlagConditionMapping
import java.lang.reflect.Method

class FeatureFlagConditionMappingTests {
    @MockitoBean
    private var mockFeatureFlagManager: FeatureFlagManager = Mockito.mock(FeatureFlagManager::class.java)

    private val handlerMapping = FeatureFlagConditionMapping(mockFeatureFlagManager)

    class TestController {
        @AvailableWhenFeatureEnabled(FLAG_NAME)
        fun enabledMethod() {}

        @AvailableWhenFeatureDisabled(FLAG_NAME)
        fun disabledMethod() {}

        fun noAnnotationMethod() {}

        companion object {
            const val FLAG_NAME = "test-flag"
        }
    }

    private fun invokeGetCustomMethodCondition(method: Method): RequestCondition<*>? {
        val reflected = FeatureFlagConditionMapping::class.java.getDeclaredMethod("getCustomMethodCondition", Method::class.java)
        reflected.isAccessible = true
        return reflected.invoke(handlerMapping, method) as RequestCondition<*>?
    }

    @Test
    fun `getCustomMethodCondition returns FeatureFlaggedRequestCondition for AvailableWhenFeatureFlagEnabled`() {
        // Arrange
        val method = TestController::class.java.getMethod("enabledMethod")

        // Act
        val condition = invokeGetCustomMethodCondition(method)

        // Assert
        assertTrue(condition is FeatureFlaggedRequestCondition)
        assertEquals(FLAG_NAME, (condition as FeatureFlaggedRequestCondition).flagName)
    }

    @Test
    fun `getCustomMethodCondition returns InverseFeatureFlaggedRequestCondition for AvailableWhenFeatureFlagDisabled`() {
        // Arrange
        val method = TestController::class.java.getMethod("disabledMethod")

        // Act
        val condition = invokeGetCustomMethodCondition(method)

        // Assert
        assertTrue(condition is InverseFeatureFlaggedRequestCondition)
        assertEquals(FLAG_NAME, (condition as InverseFeatureFlaggedRequestCondition).flagName)
    }

    @Test
    fun `getCustomMethodCondition returns null when no annotation present`() {
        // Arrange
        val method = TestController::class.java.getMethod("noAnnotationMethod")

        // Act, Assert
        assertNull(invokeGetCustomMethodCondition(method))
    }
}
