package uk.gov.communities.prsdb.webapp.examples

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import uk.gov.communities.prsdb.webapp.annotations.PrsdbController
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import java.util.zip.ZipInputStream

// TODO PRSD-1021: Remove this example once there is another way of using the OsDownloadsClient
@PrsdbController
@RequestMapping("/example/os-downloads")
class ExampleOsDownloadsController(
    private val osDownloadsClient: OsDownloadsClient,
) {
    @GetMapping("/versions")
    @ResponseBody
    fun getDataPackageVersionHistory(
        @RequestParam dataPackageId: String,
    ) = osDownloadsClient.getDataPackageVersionHistory(dataPackageId)

    @GetMapping("/version")
    @ResponseBody
    fun getDataPackageVersionDetails(
        @RequestParam dataPackageId: String,
        @RequestParam versionId: String,
    ) = osDownloadsClient.getDataPackageVersionDetails(dataPackageId, versionId)

    @GetMapping("/download")
    @ResponseBody
    fun getDataPackageVersionFile(
        @RequestParam dataPackageId: String,
        @RequestParam versionId: String,
        @RequestParam fileName: String,
    ): String {
        val zipInputStream = ZipInputStream(osDownloadsClient.getDataPackageVersionFile(dataPackageId, versionId, fileName))
        val subFileNames = mutableListOf<String>()
        zipInputStream.use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                subFileNames.add(entry.name)
                entry = zip.nextEntry
            }
        }
        return "Files in $fileName: ${subFileNames.joinToString("<br>","<br>")}"
    }
}
