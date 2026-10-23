package uk.gov.communities.prsdb.webapp.integration.journeyMap

import uk.gov.communities.prsdb.webapp.integration.journeyMap.descriptors.JourneyDescriptor
import uk.gov.communities.prsdb.webapp.integration.journeyMap.descriptors.JourneyDescriptors
import java.nio.file.Path
import java.nio.file.Paths

data class JourneyMapSettings(
    val journeys: Set<String>?,
    val outputDir: Path,
    val nodeModules: Path,
    val structureOnly: Boolean,
    val strict: Boolean,
    val screenshots: Boolean,
    val mobile: Boolean,
    val changeFlows: Boolean,
    val maxActions: Int,
    val maxVariantsPerPage: Int,
    val verbose: Boolean,
) {
    fun selectedDescriptors(): List<JourneyDescriptor> {
        val selected = JourneyDescriptors.all.filter { journeys == null || it.name in journeys }
        val unknown = journeys.orEmpty() - JourneyDescriptors.all.map { it.name }.toSet()
        require(unknown.isEmpty()) {
            "Unknown journeys $unknown. Known journeys: ${JourneyDescriptors.all.joinToString { it.name }}"
        }
        return selected
    }

    companion object {
        fun fromSystemProperties(screenshots: Boolean = true): JourneyMapSettings {
            fun property(name: String) = System.getProperty("journeyMap.$name")?.trim()?.takeIf { it.isNotEmpty() }

            return JourneyMapSettings(
                journeys = property("journeys")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet(),
                outputDir = Paths.get(property("outputDir") ?: "build/journey-maps"),
                nodeModules = Paths.get(property("nodeModules") ?: "node_modules"),
                structureOnly = property("stages")?.lowercase() == "structure",
                strict = property("strict")?.toBoolean() ?: false,
                screenshots = screenshots,
                mobile = property("viewports")?.split(",")?.any { it.trim().equals("mobile", ignoreCase = true) } ?: false,
                changeFlows = property("changeFlows")?.toBoolean() ?: true,
                maxActions = property("maxActions")?.toInt() ?: 3000,
                maxVariantsPerPage = property("maxVariantsPerPage")?.toInt() ?: 12,
                verbose = property("verbose")?.toBoolean() ?: false,
            )
        }
    }
}
