package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureDisabled
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.AvailableWhenFeatureEnabled
import uk.gov.communities.prsdb.webapp.config.FeatureFlagAnnotationValidator

class FeatureFlagAnnotationValidatorTests {
    @MockitoBean
    private lateinit var mockRequestMappingHandlerMapping: RequestMappingHandlerMapping

    private lateinit var featureFlagAnnotationValidator: FeatureFlagAnnotationValidator

    @BeforeEach
    fun setup() {
        mockRequestMappingHandlerMapping = mock(RequestMappingHandlerMapping::class.java)
        featureFlagAnnotationValidator = FeatureFlagAnnotationValidator(mockRequestMappingHandlerMapping)
    }

    class TestController {
        @AvailableWhenFeatureEnabled("flagA")
        @AvailableWhenFeatureDisabled("flagA")
        fun conflictingMethod() {}

        @AvailableWhenFeatureEnabled("flagB")
        fun enabledMethod() {}

        @AvailableWhenFeatureDisabled("flagC")
        fun disabledMethod() {}

        fun noAnnotationMethod() {}
    }

    private fun handlerMethod(methodName: String): HandlerMethod =
        HandlerMethod(TestController(), TestController::class.java.getMethod(methodName))

    @Test
    fun `throws when both annotations are present`() {
        // Arrange
        val info = RequestMappingInfo.paths("/conflict").build()
        val handlerMethods = mapOf(info to handlerMethod("conflictingMethod"))
        whenever(mockRequestMappingHandlerMapping.handlerMethods).thenReturn(handlerMethods)

        // Act & Assert
        assertThrows<IllegalStateException> {
            featureFlagAnnotationValidator.onApplicationEvent(mock(ContextRefreshedEvent::class.java))
        }
    }

    @Test
    fun `does not throw when only one annotation is present`() {
        // Arrange
        val info1 = RequestMappingInfo.paths("/enabled").build()
        val info2 = RequestMappingInfo.paths("/disabled").build()
        val handlerMethods =
            mapOf(
                info1 to handlerMethod("enabledMethod"),
                info2 to handlerMethod("disabledMethod"),
            )
        whenever(mockRequestMappingHandlerMapping.handlerMethods).thenReturn(handlerMethods)

        // Act, Assert
        assertDoesNotThrow { featureFlagAnnotationValidator.onApplicationEvent(mock(ContextRefreshedEvent::class.java)) }
    }

    @Test
    fun `does not throw when no annotation is present`() {
        // Arrange
        val info = RequestMappingInfo.paths("/none").build()
        val handlerMethods = mapOf(info to handlerMethod("noAnnotationMethod"))
        whenever(mockRequestMappingHandlerMapping.handlerMethods).thenReturn(handlerMethods)

        // Act & Assert
        assertDoesNotThrow { featureFlagAnnotationValidator.onApplicationEvent(mock(ContextRefreshedEvent::class.java)) }
    }
}
