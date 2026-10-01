package uk.gov.communities.prsdb.webapp.constants

import java.time.LocalDate
import java.time.Month

// TODO PDJB-1705: make this configurable per environment to enable local testing before Nov 14th 2026
val GRATIS_PERIOD_END_DATE: LocalDate = LocalDate.of(2027, Month.NOVEMBER, 14)
