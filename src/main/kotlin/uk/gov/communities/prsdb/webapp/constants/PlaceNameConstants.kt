package uk.gov.communities.prsdb.webapp.constants

import org.springframework.util.ResourceUtils
import java.io.InputStream

fun readPlaceNamesCsv(fileName: String): List<String> {
    val inputStream: InputStream = ResourceUtils.getFile(fileName).inputStream()
    val reader = inputStream.bufferedReader()
    reader.readLine()

    val placeNames = mutableListOf<String>()
    reader.forEachLine { line ->
        val nameInQuotationMarks = line.split("\"")
        val placeName = if (nameInQuotationMarks.size > 1) nameInQuotationMarks[1] else line.split(",")[1].replace("\"", "")
        placeNames.add(placeName)
    }
    return placeNames
}

// Files taken from https://www.gov.wales/bydtermcymru/international-place-names
val PLACE_NAMES =
    (
        readPlaceNamesCsv("classpath:data/place_names/country_names.csv") +
            readPlaceNamesCsv("classpath:data/place_names/crown_dependency_names.csv") +
            readPlaceNamesCsv("classpath:data/place_names/overseas_territory_names.csv")
    ).sorted()
