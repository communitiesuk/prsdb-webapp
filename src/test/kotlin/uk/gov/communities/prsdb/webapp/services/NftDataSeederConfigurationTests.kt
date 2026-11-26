package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.FileSystemResource
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NftDataSeederConfigurationTests {
    @Test
    fun `seeder accepts only supported certificate keys and a nonblank version`() {
        fun create(
            key: String,
            version: String,
        ) = NftDataSeeder(
            sessionFactory = mock(),
            localCouncilRepository = mock(),
            epcCertificateBaseUrl = "http://localhost",
            numOfSystemOperators = 0,
            numOfLcUsers = 0,
            numOfLandlords = 0,
            numOfProperties = 0,
            batchSize = 1,
            randomSeed = 1,
            referenceDate = "",
            numOfGeneratedAddresses = 0,
            certificateObjectVersionId = version,
            certificateObjectKey = key,
        )

        listOf("seeding/certificate_template.png", "seeding/certificate-references/1234-1.png")
            .forEach { create(it, "version-1") }
        listOf(
            "",
            "seeding/certificate-references/../1234-1.png",
            "seeding/certificate-references/0-1.png",
            "seeding/certificate-references/1234-1.png\n",
        ).forEach { key -> assertFailsWith<IllegalArgumentException> { create(key, "version-1") } }
        assertFailsWith<IllegalArgumentException> { create("seeding/certificate_template.png", " ") }
    }

    @Test
    fun `certificate key remains backwards compatible while allowing a per-seed override`() {
        val source = YamlPropertySourceLoader().load("application", FileSystemResource("src/main/resources/application.yml")).first()
        assertEquals(
            "\${NFT_SEED_CERTIFICATE_OBJECT_KEY:seeding/certificate_template.png}",
            source.getProperty("nft-seed.certificate-object-key"),
        )
    }

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
