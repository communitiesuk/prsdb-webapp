package uk.gov.communities.prsdb.webapp.integration.oneLoginSimulator

import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPairGenerator
import java.util.Base64

class OneLoginSimulatorClientKeys private constructor(
    val publicKeyPem: String,
    val publicKeyLocation: String,
    val privateKeyLocation: String,
    private val publicKeyPath: Path,
    private val privateKeyPath: Path,
    private val directory: Path,
) : AutoCloseable {
    override fun close() {
        Files.deleteIfExists(publicKeyPath)
        Files.deleteIfExists(privateKeyPath)
        Files.deleteIfExists(directory)
    }

    companion object {
        fun create(): OneLoginSimulatorClientKeys {
            val keyPair =
                KeyPairGenerator
                    .getInstance("RSA")
                    .apply { initialize(2048) }
                    .generateKeyPair()
            val publicKeyPem = encodePem("PUBLIC KEY", keyPair.public.encoded)
            val privateKeyPem = encodePem("PRIVATE KEY", keyPair.private.encoded)
            val directory = Files.createTempDirectory("one-login-simulator-keys")
            val publicKeyPath = Files.writeString(directory.resolve("public-key.pem"), publicKeyPem)
            val privateKeyPath = Files.writeString(directory.resolve("private-key.pem"), privateKeyPem)

            return OneLoginSimulatorClientKeys(
                publicKeyPem = publicKeyPem,
                publicKeyLocation = publicKeyPath.toUri().toString(),
                privateKeyLocation = privateKeyPath.toUri().toString(),
                publicKeyPath = publicKeyPath,
                privateKeyPath = privateKeyPath,
                directory = directory,
            )
        }

        private fun encodePem(
            type: String,
            encodedKey: ByteArray,
        ): String {
            val encoded = Base64.getMimeEncoder(64, "\n".toByteArray()).encodeToString(encodedKey)
            return "-----BEGIN $type-----\n$encoded\n-----END $type-----\n"
        }
    }
}
