package uk.gov.communities.prsdb.webapp.journeys

import kotlin.reflect.KClass

sealed interface JourneyContainer {
    data class Section(
        val headingMessageKey: String,
    ) : JourneyContainer

    data class Task(
        val taskClass: KClass<*>,
        val routeSegment: String?,
    ) : JourneyContainer
}
