package uk.gov.communities.prsdb.webapp.local.api.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.matchesPattern
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class MockGovUkPayControllerTests {
    private lateinit var mvc: MockMvc
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        mvc = MockMvcBuilders.standaloneSetup(MockGovUkPayController("8080")).build()
    }

    private fun createBody(
        amount: Int = 12000,
        reference: String = "REF-123",
        returnUrl: String = "https://service.example/completed",
        delayedCapture: Boolean = true,
    ) = """
        {"amount":$amount,"reference":"$reference","description":"Registration fee",
         "return_url":"$returnUrl","delayed_capture":$delayedCapture,"email":"a@b.com"}
        """.trimIndent()

    private fun createPaymentAndGetId(delayedCapture: Boolean = true): String {
        val response =
            mvc
                .perform(
                    post("/local/gov-uk-pay/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(delayedCapture = delayedCapture)),
                ).andReturn()
                .response.contentAsString
        return objectMapper.readTree(response).get("payment_id").asText()
    }

    @Test
    fun `creating a payment returns 201 with created state and a next_url`() {
        mvc
            .perform(post("/local/gov-uk-pay/v1/payments").contentType(MediaType.APPLICATION_JSON).content(createBody()))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.amount").value(12000))
            .andExpect(jsonPath("$.reference").value("REF-123"))
            .andExpect(jsonPath("$.state.status").value("created"))
            .andExpect(jsonPath("$.state.finished").value(false))
            .andExpect(jsonPath("$.delayed_capture").value(true))
            .andExpect(jsonPath("$.payment_id", matchesPattern("[a-z0-9]{26}")))
            .andExpect(jsonPath("$._links.self.href", startsWith("http://localhost:8080/local/gov-uk-pay/v1/payments/")))
            .andExpect(
                jsonPath("$._links.next_url.href", startsWith("http://localhost:8080/local/gov-uk-pay/mock-card-page/")),
            )
    }

    @Test
    fun `creating a payment with a non-positive amount returns 422 with an error body`() {
        mvc
            .perform(
                post("/local/gov-uk-pay/v1/payments").contentType(MediaType.APPLICATION_JSON).content(createBody(amount = 0)),
            ).andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.code").value("P0102"))
    }

    @Test
    fun `creating a payment without a description returns 422 with an error body`() {
        mvc
            .perform(
                post("/local/gov-uk-pay/v1/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"amount":100,"reference":"R","return_url":"https://x"}"""),
            ).andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.code").value("P0101"))
    }

    @Test
    fun `visiting the mock card page moves a delayed payment to capturable and redirects to the return url`() {
        val paymentId = createPaymentAndGetId(delayedCapture = true)

        mvc
            .perform(get("/local/gov-uk-pay/mock-card-page/$paymentId"))
            .andExpect(status().is3xxRedirection)
            .andExpect(header().string("Location", "https://service.example/completed"))

        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/$paymentId"))
            .andExpect(jsonPath("$.state.status").value("capturable"))
    }

    @Test
    fun `visiting the mock card page for an unknown payment returns 404`() {
        mvc.perform(get("/local/gov-uk-pay/mock-card-page/unknownpaymentid00000000000")).andExpect(status().isNotFound)
    }

    @Test
    fun `getting a known payment returns 200 with its details`() {
        val paymentId = createPaymentAndGetId()

        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/$paymentId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.payment_id").value(paymentId))
            .andExpect(jsonPath("$.state.status").value("created"))
            .andExpect(jsonPath("$.reference").value("REF-123"))
    }

    @Test
    fun `getting an unknown payment returns 404 with an error body`() {
        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/unknownpaymentid00000000000"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("P0200"))
    }

    @Test
    fun `capturing a capturable payment returns 204 and moves it to success`() {
        val paymentId = createPaymentAndGetId(delayedCapture = true)
        mvc.perform(get("/local/gov-uk-pay/mock-card-page/$paymentId"))

        mvc
            .perform(post("/local/gov-uk-pay/v1/payments/$paymentId/capture"))
            .andExpect(status().isNoContent)

        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/$paymentId"))
            .andExpect(jsonPath("$.state.status").value("success"))
            .andExpect(jsonPath("$.state.finished").value(true))
    }

    @Test
    fun `capturing a payment that is not capturable returns 400`() {
        val paymentId = createPaymentAndGetId(delayedCapture = true)

        mvc
            .perform(post("/local/gov-uk-pay/v1/payments/$paymentId/capture"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("P0104"))
    }

    @Test
    fun `capturing an unknown payment returns 404`() {
        mvc
            .perform(post("/local/gov-uk-pay/v1/payments/unknownpaymentid00000000000/capture"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `cancelling a non-finished payment returns 204 and moves it to cancelled`() {
        val paymentId = createPaymentAndGetId()

        mvc
            .perform(post("/local/gov-uk-pay/v1/payments/$paymentId/cancel"))
            .andExpect(status().isNoContent)

        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/$paymentId"))
            .andExpect(jsonPath("$.state.status").value("cancelled"))
            .andExpect(jsonPath("$.state.finished").value(true))
    }

    @Test
    fun `cancelling an already finished payment returns 400`() {
        val paymentId = createPaymentAndGetId(delayedCapture = true)
        mvc.perform(get("/local/gov-uk-pay/mock-card-page/$paymentId"))
        mvc.perform(post("/local/gov-uk-pay/v1/payments/$paymentId/capture"))

        mvc
            .perform(post("/local/gov-uk-pay/v1/payments/$paymentId/cancel"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("P0501"))
    }

    @Test
    fun `cancelling an unknown payment returns 404`() {
        mvc
            .perform(post("/local/gov-uk-pay/v1/payments/unknownpaymentid00000000000/cancel"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `a created payment includes the standard response fields and full links`() {
        mvc
            .perform(post("/local/gov-uk-pay/v1/payments").contentType(MediaType.APPLICATION_JSON).content(createBody()))
            .andExpect(jsonPath("$.payment_provider").value("sandbox"))
            .andExpect(jsonPath("$.email").value("a@b.com"))
            .andExpect(jsonPath("$._links.next_url_post.params.chargeTokenId").isNotEmpty)
            .andExpect(jsonPath("$._links.events.href", startsWith("http://localhost:8080/local/gov-uk-pay/v1/payments/")))
            .andExpect(jsonPath("$._links.refunds.href", startsWith("http://localhost:8080/local/gov-uk-pay/v1/payments/")))
            .andExpect(jsonPath("$._links.cancel.method").value("POST"))
            .andExpect(jsonPath("$.card_details").doesNotExist())
            .andExpect(jsonPath("$.provider_id").doesNotExist())
            .andExpect(jsonPath("$.moto").doesNotExist())
            .andExpect(jsonPath("$.refund_summary").doesNotExist())
            .andExpect(jsonPath("$.language").doesNotExist())
    }

    @Test
    fun `metadata supplied on creation is echoed back in the payment details`() {
        val body =
            """
            {"amount":100,"reference":"R","description":"D","return_url":"https://x",
             "metadata":{"ledger_code":"AB100","invoice":42}}
            """.trimIndent()

        mvc
            .perform(post("/local/gov-uk-pay/v1/payments").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(jsonPath("$.metadata.ledger_code").value("AB100"))
            .andExpect(jsonPath("$.metadata.invoice").value(42))
    }

    @Test
    fun `a captured payment exposes card details, provider id and authorisation mode`() {
        val paymentId = createPaymentAndGetId(delayedCapture = true)
        mvc.perform(get("/local/gov-uk-pay/mock-card-page/$paymentId"))
        mvc.perform(post("/local/gov-uk-pay/v1/payments/$paymentId/capture"))

        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/$paymentId"))
            .andExpect(jsonPath("$.card_details.card_brand").value("Visa"))
            .andExpect(jsonPath("$.card_details.last_digits_card_number").value("1234"))
            .andExpect(jsonPath("$.card_details.billing_address.country").value("GB"))
            .andExpect(jsonPath("$.provider_id").isNotEmpty)
            .andExpect(jsonPath("$.authorisation_mode").value("web"))
            .andExpect(jsonPath("$._links.next_url").doesNotExist())
            .andExpect(jsonPath("$._links.cancel").doesNotExist())
    }

    @Test
    fun `a capturable payment exposes capture and cancel links but not next_url`() {
        val paymentId = createPaymentAndGetId(delayedCapture = true)
        mvc.perform(get("/local/gov-uk-pay/mock-card-page/$paymentId"))

        mvc
            .perform(get("/local/gov-uk-pay/v1/payments/$paymentId"))
            .andExpect(jsonPath("$._links.capture.method").value("POST"))
            .andExpect(jsonPath("$._links.cancel.method").value("POST"))
            .andExpect(jsonPath("$._links.next_url").doesNotExist())
    }
}
