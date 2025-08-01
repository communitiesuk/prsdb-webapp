package uk.gov.communities.prsdb.webapp.helpers

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PropertyComplianceJourneyHelper {
    companion object {
        private fun compactTime() =
            LocalDateTime
                .Format {
                    yearTwoDigits(2000)
                    monthNumber()
                    dayOfMonth()
                    hour()
                    minute()
                    second()
                }.format(
                    Clock.System.now().toLocalDateTime(
                        TimeZone.of("Europe/London"),
                    ),
                )

        fun getCertFilename(
            propertyOwnershipId: Long,
            stepName: String,
        ): String = "certificateUpload.$propertyOwnershipId.$stepName.${compactTime()}"
    }
}
