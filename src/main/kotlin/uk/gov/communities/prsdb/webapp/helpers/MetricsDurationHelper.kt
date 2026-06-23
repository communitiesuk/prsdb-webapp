package uk.gov.communities.prsdb.webapp.helpers

import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import java.time.Duration

class MetricsDurationHelper {
    companion object {
        fun formatDuration(
            duration: Duration,
            messageSource: MessageSource,
        ): String {
            val components =
                listOf(
                    "day" to duration.toDaysPart(),
                    "hour" to duration.toHoursPart().toLong(),
                    "minute" to duration.toMinutesPart().toLong(),
                ).filter { (_, amount) -> amount > 0 }

            if (components.isEmpty()) {
                return pluralisedUnit("minute", 0, messageSource)
            }

            return components.joinToString(", ") { (unit, amount) -> pluralisedUnit(unit, amount, messageSource) }
        }

        private fun pluralisedUnit(
            unit: String,
            amount: Long,
            messageSource: MessageSource,
        ): String {
            val key = if (amount == 1L) "metrics.saveAndReturn.$unit" else "metrics.saveAndReturn.${unit}s"
            return messageSource.getMessageForKey(key, arrayOf(amount))
        }
    }
}
