package uk.gov.communities.prsdb.webapp.services

import kotlinx.datetime.LocalDate
import org.springframework.stereotype.Service

@Service
class DateFormatterService {
    companion object {
        fun getFormattedDate(
            day: String,
            month: String,
            year: String,
        ): String = LocalDate(year.toInt(), month.toInt(), day.toInt()).toString()
    }
}
