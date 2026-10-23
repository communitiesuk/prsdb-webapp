package uk.gov.communities.prsdb.webapp.integration.journeyMap.descriptors

import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.integration.pageObjects.Navigator
import uk.gov.communities.prsdb.webapp.journeyMap.discovery.SeededAddress

data class JourneyDescriptor(
    val name: String,
    val title: String,
    val basePath: String,
    val seedScripts: List<String>,
    val start: (Navigator) -> Unit,
    val address: SeededAddress? = null,
    val singleRenderPaths: Set<String> = emptySet(),
    val arrange: (JourneyMapHarness) -> Unit = {},
    val startingPoints: List<StartingPoint> = emptyList(),
    // The journey is entered by following the first link into it on the page that start opens, as users do
    val startsFromLink: Boolean = false,
    // How users reach a journey that is not entered by following a link, such as the link in an invitation email
    val entryLabel: String? = null,
)

// Data that makes the journey start differently, such as a property with joint landlords, explored after the default
data class StartingPoint(
    val label: String,
    val seedScripts: List<String>,
)
