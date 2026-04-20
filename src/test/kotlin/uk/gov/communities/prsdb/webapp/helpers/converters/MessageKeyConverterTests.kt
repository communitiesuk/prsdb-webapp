package uk.gov.communities.prsdb.webapp.helpers.converters

import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.config.YamlMessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import java.util.Locale

class MessageKeyConverterTests {
    private val messageSource = YamlMessageSource("classpath:messages")

    @ParameterizedTest
    @EnumSource(EpcExemptionReason::class)
    fun `convert returns a resolvable message key for every EpcExemptionReason`(reason: EpcExemptionReason) {
        val messageKey = MessageKeyConverter.convert(reason)
        val resolvedMessage = messageSource.getMessage(messageKey, null, messageKey, Locale.getDefault())
        assertNotEquals(messageKey, resolvedMessage) {
            "Message key '$messageKey' for $reason does not resolve to a message"
        }
    }
}
