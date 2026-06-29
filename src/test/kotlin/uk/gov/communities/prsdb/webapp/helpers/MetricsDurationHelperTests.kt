package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.context.MessageSource
import org.springframework.context.MessageSourceResolvable
import uk.gov.communities.prsdb.webapp.helpers.MetricsDurationHelper.Companion.formatDuration
import java.time.Duration
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

class MetricsDurationHelperTests {
    // Echoes back "<amount> <unit>" so assertions can verify which key (singular vs plural) and
    // amount the helper resolved, as well as how the components are joined.
    private val messageSource =
        object : MessageSource {
            override fun getMessage(
                code: String,
                args: Array<out Any>?,
                defaultMessage: String?,
                locale: Locale?,
            ): String = format(code, args)

            override fun getMessage(
                code: String,
                args: Array<out Any>?,
                locale: Locale?,
            ): String = format(code, args)

            override fun getMessage(
                resolvable: MessageSourceResolvable,
                locale: Locale?,
            ): String = format(resolvable.codes?.firstOrNull() ?: "", resolvable.arguments)

            private fun format(
                code: String,
                args: Array<out Any>?,
            ): String = "${args?.firstOrNull()} ${code.substringAfterLast('.')}"
        }

    @Test
    fun `formatDuration renders days, hours and minutes when all are present`() {
        val duration = Duration.ofDays(1).plusHours(6).plusMinutes(22)

        assertEquals("1 day, 6 hours, 22 minutes", formatDuration(duration, messageSource))
    }

    @Test
    fun `formatDuration suppresses zero-valued components`() {
        val duration = Duration.ofDays(2).plusMinutes(43)

        assertEquals("2 days, 43 minutes", formatDuration(duration, messageSource))
    }

    @Test
    fun `formatDuration renders a single component when only that component is non-zero`() {
        assertEquals("50 days", formatDuration(Duration.ofDays(50), messageSource))
        assertEquals("1 hour", formatDuration(Duration.ofHours(1), messageSource))
        assertEquals("22 minutes", formatDuration(Duration.ofMinutes(22), messageSource))
    }

    @Test
    fun `formatDuration uses singular unit keys for amounts of one`() {
        val duration = Duration.ofDays(1).plusHours(1).plusMinutes(1)

        assertEquals("1 day, 1 hour, 1 minute", formatDuration(duration, messageSource))
    }

    @Test
    fun `formatDuration renders zero minutes for sub-minute durations`() {
        assertEquals("0 minutes", formatDuration(Duration.ofSeconds(30), messageSource))
        assertEquals("0 minutes", formatDuration(Duration.ZERO, messageSource))
    }
}
