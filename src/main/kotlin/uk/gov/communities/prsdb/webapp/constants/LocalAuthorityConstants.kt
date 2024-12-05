package uk.gov.communities.prsdb.webapp.constants

import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.models.dataModels.LocalAuthorityDataModel
import java.io.InputStream

fun readLocalAuthoritiesCsv(fileName: String): List<LocalAuthorityDataModel> {
    val inputStream: InputStream = ResourceUtils.getFile(fileName).inputStream()
    val reader = inputStream.bufferedReader()
    reader.readLine()

    val localAuthorities = mutableListOf<LocalAuthorityDataModel>()
    reader.forEachLine { line ->
        val uprn = line.split(",")[0]
        val name = line.split(",")[1]

        val displayName =
            name
                .lowercase()
                .split(' ')
                .joinToString(" ") {
                    it.replaceFirstChar { char -> char.uppercaseChar() }
                }

        localAuthorities.add(LocalAuthorityDataModel(uprn, displayName))
    }
    return localAuthorities
}

val LOCAL_AUTHORITIES =
    (readLocalAuthoritiesCsv("classpath:data/local_authorities/addressbase-local-custodian-codes.csv")).sortedBy { it.displayName }
