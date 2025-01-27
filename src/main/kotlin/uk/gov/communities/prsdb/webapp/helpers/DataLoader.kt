package uk.gov.communities.prsdb.webapp.helpers

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.models.dataModels.PlaceNameDataModel

class DataLoader {
    companion object {
        fun loadPlaceNames(placeNameCSVFiles: List<String>): List<PlaceNameDataModel> {
            val csvSchema =
                CsvSchema
                    .builder()
                    .addColumn("ignore-Country code/Territory")
                    .addColumn("name")
                    .addColumn("ignore-Name in Welsh")
                    .addColumn("ignore-Official name in English")
                    .addColumn("ignore-Official name in Welsh")
                    .build()

            return placeNameCSVFiles
                .map { filePath -> loadCsvFile<PlaceNameDataModel>(filePath, csvSchema) }
                .flatten()
                .sortedBy { it.name }
        }

        private inline fun <reified T> loadCsvFile(
            csvFilePath: String,
            csvSchema: CsvSchema,
        ): List<T> {
            val csvMapper = CsvMapper().registerKotlinModule()

            val fileReader = ResourceUtils.getFile(csvFilePath).inputStream().bufferedReader()

            fileReader.use { reader ->
                return csvMapper
                    .readerFor(T::class.java)
                    .with(csvSchema.withHeader())
                    .readValues<T>(reader)
                    .readAll()
                    .toList()
            }
        }
    }
}
