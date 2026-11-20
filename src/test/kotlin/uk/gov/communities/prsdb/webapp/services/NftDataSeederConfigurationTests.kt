package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.FileSystemResource
import kotlin.test.assertEquals

class NftDataSeederConfigurationTests {
    @Test
    fun `local seeder uses local file metadata while deployed seeder requires an S3 version`() {
        val loader = YamlPropertySourceLoader()
        val certificateVersionProperty = "nft-seed.certificate-version-id"

        fun property(resource: String): Any? =
            loader.load(resource, FileSystemResource("src/main/resources/$resource.yml"))
                .firstNotNullOfOrNull { it.getProperty(certificateVersionProperty) }

        assertEquals("local-file-uploader", property("application-local"))
        assertEquals("\${NFT_SEED_CERTIFICATE_VERSION_ID:}", property("application"))
    }
}
