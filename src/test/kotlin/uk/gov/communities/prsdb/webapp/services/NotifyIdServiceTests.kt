package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplate
import kotlin.test.assertEquals

class NotifyIdServiceTests {
    @Serializable
    private data class NotifyIdMetadata(
        val test_id: String? = null,
        val prod_id: String? = null,
        val enumName: String,
        val subject: String? = null,
        val bodyLocation: String,
    )

    companion object {
        private val metadataList =
            Json.decodeFromString<List<NotifyIdMetadata>>(
                javaClass.getResource("/emails/emailTemplates.json")!!.readText(),
            )

        private val letterMetadataList =
            Json.decodeFromString<List<NotifyIdMetadata>>(
                javaClass.getResource("/letters/letterTemplates.json")!!.readText(),
            )

        private val smsMetadataList =
            Json.decodeFromString<List<NotifyIdMetadata>>(
                javaClass.getResource("/sms/smsTemplates.json")!!.readText(),
            )
    }

    @ParameterizedTest
    @EnumSource(EmailTemplate::class)
    fun `getIdValue returns production Notify id values if production is set`(id: EmailTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = true)
        val metadata = metadataList.firstOrNull { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata?.prod_id, idValue)
    }

    @ParameterizedTest
    @EnumSource(EmailTemplate::class)
    fun `getIdValue returns test Notify id values if production is not set`(id: EmailTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = false)
        val metadata = metadataList.firstOrNull { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata?.test_id, idValue)
    }

    @ParameterizedTest
    @EnumSource(LetterTemplate::class)
    fun `getIdValue returns production Notify letter id values if production is set`(id: LetterTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = true)
        val metadata = letterMetadataList.firstOrNull { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata?.prod_id, idValue)
    }

    @ParameterizedTest
    @EnumSource(LetterTemplate::class)
    fun `getIdValue returns test Notify letter id values if production is not set`(id: LetterTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = false)
        val metadata = letterMetadataList.firstOrNull { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata?.test_id, idValue)
    }

    @ParameterizedTest
    @EnumSource(SmsTemplate::class)
    fun `getIdValue returns production Notify SMS id values if production is set`(id: SmsTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = true)
        val metadata = smsMetadataList.first { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata.prod_id, idValue)
    }

    @ParameterizedTest
    @EnumSource(SmsTemplate::class)
    fun `getIdValue returns test Notify SMS id values if production is not set`(id: SmsTemplate) {
        // Arrange
        val service = NotifyIdService(useProductionNotify = false)
        val metadata = smsMetadataList.first { it.enumName == id.name }

        // Act
        val idValue = service.getNotifyIdValue(id)

        // Assert
        assertEquals(metadata.test_id, idValue)
    }
}
