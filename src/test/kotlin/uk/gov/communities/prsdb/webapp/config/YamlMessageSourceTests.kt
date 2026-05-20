package uk.gov.communities.prsdb.webapp.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.USE_COMPLIANCE_ACTIONS_PAGE_REDESIGN
import java.util.Locale

class YamlMessageSourceTests {
    private val featureFlagManager =
        mock<FeatureFlagManager>().also {
            whenever(it.checkFeature(any())).thenReturn(false)
        }

    @Nested
    inner class BasicMessageResolution {
        private val messageSource = YamlMessageSource("classpath:test-messages/basic", featureFlagManager)

        @Test
        fun `resolves simple nested message key`() {
            val message = messageSource.getMessage("simple.message", null, Locale.ENGLISH)
            assertEquals("This is a simple message", message)
        }

        @Test
        fun `resolves deeply nested message key`() {
            val message = messageSource.getMessage("simple.nested.deep", null, Locale.ENGLISH)
            assertEquals("This is a deeply nested message", message)
        }

        @Test
        fun `resolves top level message key`() {
            val message = messageSource.getMessage("topLevel", null, Locale.ENGLISH)
            assertEquals("A top level message", message)
        }

        @Test
        fun `converts non-string values to strings`() {
            val message = messageSource.getMessage("numeric", null, Locale.ENGLISH)
            assertEquals("12345", message)
        }

        @Test
        fun `returns default message for non-existent key`() {
            val defaultMessage = "default"
            val message = messageSource.getMessage("nonexistent.key", null, defaultMessage, Locale.ENGLISH)
            assertEquals(defaultMessage, message)
        }
    }

    @Nested
    inner class MessageFormatting {
        private val messageSource = YamlMessageSource("classpath:test-messages/basic", featureFlagManager)

        @Test
        fun `formats message with arguments`() {
            val message = messageSource.getMessage("greeting", arrayOf("World"), Locale.ENGLISH)
            assertEquals("Hello, World!", message)
        }

        @Test
        fun `formats message with multiple arguments`() {
            val message = messageSource.getMessage("greetings", arrayOf("Test User 1", "Test User 2"), Locale.ENGLISH)
            assertEquals("Hello, Test User 1 and Test User 2!", message)
        }
    }

    @Nested
    inner class PrefixedFileLoading {
        private val messageSource = YamlMessageSource("classpath:test-messages/with-prefix", featureFlagManager)

        @Test
        fun `resolves message from default file without prefix`() {
            val message = messageSource.getMessage("common.button.submit", null, Locale.ENGLISH)
            assertEquals("Submit", message)
        }

        @Test
        fun `resolves message from non-default file with filename prefix`() {
            val message = messageSource.getMessage("customPage.title", null, Locale.ENGLISH)
            assertEquals("Custom Page Title", message)
        }

        @Test
        fun `resolves nested message from non-default file with filename prefix`() {
            val message = messageSource.getMessage("customPage.form.label", null, Locale.ENGLISH)
            assertEquals("Enter your name", message)
        }

        @Test
        fun `resolves deeply nested message from non-default file`() {
            val message = messageSource.getMessage("customPage.form.error.required", null, Locale.ENGLISH)
            assertEquals("Name is required", message)
        }
    }

    @Nested
    inner class DuplicateKeyDetection {
        @Test
        fun `throws exception when duplicate key exists across files`() {
            val exception =
                assertThrows(IllegalStateException::class.java) {
                    YamlMessageSource("classpath:test-messages/duplicate-keys", featureFlagManager).getMessage(
                        "any.key",
                        null,
                        Locale.ENGLISH,
                    )
                }

            assertTrue(exception.message?.contains("Duplicate message key") == true)
            assertTrue(exception.message?.contains("duplicatePage.title") == true)
        }
    }

    @Nested
    inner class EmptyFolder {
        @Test
        fun `handles empty messages folder gracefully`() {
            val messageSource = YamlMessageSource("classpath:test-messages/empty", featureFlagManager)
            val defaultMessage = "default"
            val message = messageSource.getMessage("any.key", null, defaultMessage, Locale.ENGLISH)
            assertEquals(defaultMessage, message)
        }
    }

    @Nested
    inner class FeatureFlagFileSwitching {
        @Test
        fun `loads complianceActions and skips complianceActionsOld when redesign flag is enabled`() {
            val flagManager =
                mock<FeatureFlagManager>().also {
                    whenever(it.checkFeature(USE_COMPLIANCE_ACTIONS_PAGE_REDESIGN)).thenReturn(true)
                }
            val messageSource = YamlMessageSource("classpath:test-messages/feature-flags", flagManager)

            assertEquals("New heading", messageSource.getMessage("complianceActions.heading", null, Locale.ENGLISH))
            assertEquals("New subtitle", messageSource.getMessage("complianceActions.subtitle", null, Locale.ENGLISH))
        }

        @Test
        fun `loads complianceActionsOld remapped to complianceActions prefix and skips complianceActions when redesign flag is disabled`() {
            val flagManager =
                mock<FeatureFlagManager>().also {
                    whenever(it.checkFeature(USE_COMPLIANCE_ACTIONS_PAGE_REDESIGN)).thenReturn(false)
                }
            val messageSource = YamlMessageSource("classpath:test-messages/feature-flags", flagManager)

            assertEquals("Old heading", messageSource.getMessage("complianceActions.heading", null, Locale.ENGLISH))
            assertEquals("Old subtitle", messageSource.getMessage("complianceActions.subtitle", null, Locale.ENGLISH))
        }
    }
}
