package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockMessageSource
import java.util.Locale

@ExtendWith(MockitoExtension::class)
class MessageSourceExtensionsTests {
    @Spy
    private lateinit var messageSource: MockMessageSource

    @Test
    fun `getMessageForKey delegates to getMessage with the default locale`() {
        val key = "key"
        val args = arrayOf<Any>("arg1", "arg2")

        messageSource.getMessageForKey(key, args)

        verify(messageSource).getMessage(key, args, Locale.getDefault())
    }
}
