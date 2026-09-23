---
applyTo: "**/controllers/**"
---

# Controller Instructions

## Naming Conventions
- Controllers: `{Feature}Controller.kt` (e.g., `LandlordController.kt`)
- Test classes: `{Feature}ControllerTests.kt`

## Annotations
```kotlin
@PrsdbController  // Custom controller annotation (combines @Controller)
@RequestMapping("/path")
@PreAuthorize("hasRole('LANDLORD')")
class ExampleController {
    @GetMapping("/page")
    fun getPage(): ModelAndView = ModelAndView("templateName")

    @PostMapping("/submit")
    fun handleSubmit(): RedirectView = RedirectView("/path/page", true)
}
```

`@PrsdbRestController` is used for REST API endpoints that return JSON (e.g., local API stubs).

## Permission Handling
- Use class-level `@PreAuthorize` when all handlers need the same role, or method-level annotations when access differs.
  [PropertyDetailsController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/PropertyDetailsController.kt)
  has separate landlord and local council handlers.
- Common roles: `LANDLORD`, `LOCAL_COUNCIL_USER`, `LOCAL_COUNCIL_ADMIN`, `SYSTEM_OPERATOR`.
- Services own ownership/access decisions, but controllers must call the appropriate guard **before dispatch**.
  [UpdateGasSafetyController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/UpdateGasSafetyController.kt)
  calls `propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(propertyOwnershipId)` for GET, POST and upload handlers.
- Some pre-registration, token and invitation endpoints intentionally have no role annotation. Preserve the relevant
  security chain's `permitAll()` / `anonymous()` rules rather than adding blanket class-level authorization.

## Journey Controllers
Use wildcard `/{*stepPath}` mappings, `@RequestParam FormData`, and `JourneyStepDispatcher`. The dispatch lambda runs
on `StepLifecycleOrchestrator`: GET calls `getStepModelAndView()`, POST calls `postStepModelAndView(formData)`.

For a journey that can initialise on entry, follow
[UpdateLandlordEmailController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/UpdateLandlordEmailController.kt):

```kotlin
@GetMapping("/{*stepPath}")
fun getUpdateStep(
    principal: Principal,
    @PathVariable stepPath: String,
): ModelAndView = dispatchJourneyStep(stepPath, principal) { getStepModelAndView() }

@PostMapping("/{*stepPath}")
fun postUpdateStep(
    principal: Principal,
    @PathVariable stepPath: String,
    @RequestParam formData: FormData,
): ModelAndView = dispatchJourneyStep(stepPath, principal) { postStepModelAndView(formData) }

private fun dispatchJourneyStep(
    stepPath: String,
    principal: Principal,
    dispatch: StepLifecycleOrchestrator.() -> ModelAndView,
): ModelAndView =
    JourneyStepDispatcher.handleInitialisableRequest(
        rawStepPath = stepPath,
        createRoutingMap = { journeyFactory.createJourneySteps() },
        initialiseJourney = { journeyFactory.initializeJourneyState(principal) },
        dispatch = dispatch,
    )
```

Token-entry journeys may need explicit initialisation instead:
[LettingAgentInvitationController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/LettingAgentInvitationController.kt)
validates the token, initialises state, stores the journey/token association and redirects with the journey ID.
Its step handlers then use `handleUninitialisableRequest(createRoutingMap = ..., getRedirect = ..., ...)` so a request
without state returns to the entry route rather than creating a journey without a validated token.

See [journey instructions](journeys.instructions.md) for the framework.

## Multipart Uploads
- Ordinary Spring multipart handling is disabled by `spring.servlet.multipart.enabled: false` in
  [application.yml](../../src/main/resources/application.yml). Do not replace the streaming path with `MultipartFile`.
- A multipart handler uses `@PostMapping("/{*stepPath}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])` and receives
  `@RequestAttribute(MultipartFormDataFilter.ITERATOR_ATTRIBUTE) fileInputIterator: FileItemInputIterator`.
- Pass the iterator, upload-cookie token, request and generated certificate filename to
  `CertificateUploadHelper.uploadFileAndReturnFormModel(...)`, then dispatch `postStepModelAndView(formData)`.
  Preserve journey/member IDs and any access guard before upload/dispatch.
- Follow [RegisterPropertyController](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/RegisterPropertyController.kt),
  including `annotateFormDataForMetadataOnlyFileUpload(formData)` on its ordinary POST path.

## Response Patterns
```kotlin
// Page rendering
return ModelAndView("templateName", mapOf("key" to value))

// Redirect after POST
return RedirectView("/next-page", true)

// With flash attributes
redirectAttributes.addFlashAttribute("message", "Success")
```

Keep reusable routes in controller companion-object constants, composed from shared path-segment constants. Reuse
route helpers such as `getUpdateGasSafetyFirstStepRoute(propertyOwnershipId)` instead of duplicating URL construction.

## Controller Advice
Use `@PrsdbControllerAdvice` for cross-controller behaviour in `controllers/controllerAdvice/`:
- [GlobalModelAttributes](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/controllerAdvice/GlobalModelAttributes.kt)
  uses `@ModelAttribute` for shared model values.
- [GlobalExceptionHandler](../../src/main/kotlin/uk/gov/communities/prsdb/webapp/controllers/controllerAdvice/GlobalExceptionHandler.kt)
  uses `@ExceptionHandler` for shared exception-to-response handling (e.g., update-conflict redirects).

## Controller Tests
```kotlin
@WebMvcTest(ExampleController::class)
class ExampleControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var myService: MyService

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `returns page with expected content`() {
        mvc.get("/path/page")
            .andExpect { status { isOk() } }
            .andExpect { view { name("templateName") } }
    }
}
```

The [ControllerTest](../../src/test/kotlin/uk/gov/communities/prsdb/webapp/controllers/ControllerTest.kt) base class provides
`mvc` (MockMvc) with Spring Security and the trailing-slash filter. It mocks `BackUrlStorageService`,
`ClientRegistrationRepository`, `UserRolesService`, `DashboardUrlProvider`, `FeatureFlagOverrideService` and
`PrsdbWebMvcRegistration`. Supply an appropriate user for protected endpoints and CSRF for protected POSTs;
unauthenticated success tests must target intentionally permitted endpoints.

## Local API Stubs
- Place in `local/api/controllers/`
- Annotate with `@Profile("local")`
- Match real API contract for integration testing
