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
        val custodianCode = line.split(",")[0]
        // Local authorities with commas in their names have the name enclosed in quotation marks
        val nameInQuotationMarks = line.split("\"")
        val displayName = if (nameInQuotationMarks.size > 1) nameInQuotationMarks[1] else line.split(",")[1]

        localAuthorities.add(LocalAuthorityDataModel(custodianCode, displayName))
    }
    return localAuthorities
}

val LOCAL_AUTHORITIES =
    (readLocalAuthoritiesCsv("classpath:data/local_authorities/addressbase-local-custodian-codes.csv")).sortedBy { it.displayName }
