package uk.gov.communities.prsdb.webapp.helpers

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.util.ResourceUtils
import uk.gov.communities.prsdb.webapp.models.dataModels.PlaceNameDataModel

class DataLoader {
    companion object {
        fun loadPlaceNames(): List<PlaceNameDataModel> {
            // Files taken from https://www.gov.wales/bydtermcymru/international-place-names
            val placeNameCSVFiles =
                listOf(
                    "classpath:data/place_names/country_names.csv",
                    "classpath:data/place_names/crown_dependency_names.csv",
                    "classpath:data/place_names/overseas_territory_names.csv",
                )

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
                .reduce { currentPlaceNameList, nextPlaceNameList -> currentPlaceNameList + nextPlaceNameList }
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
