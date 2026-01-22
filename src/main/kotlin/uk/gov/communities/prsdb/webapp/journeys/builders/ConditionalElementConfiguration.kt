package uk.gov.communities.prsdb.webapp.journeys.builders

class ConditionalElementConfiguration(
    val condition: ConfigurableElement<*>.() -> Boolean,
    val configuration: ConfigurableElement<*>.() -> Unit,
)
