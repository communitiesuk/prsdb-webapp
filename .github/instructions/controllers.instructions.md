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
class ExampleController {
    
    @GetMapping("/page")
    fun getPage(): ModelAndView { }
    
    @PostMapping("/submit")
    fun handleSubmit(): RedirectView { }
}
```

`@PrsdbRestController` is used for REST API endpoints that return JSON (e.g., local API stubs).

## Permission Handling
- Use `@PreAuthorize` for role-based access
- Common roles: `LANDLORD`, `LOCAL_COUNCIL_USER`, `LOCAL_COUNCIL_ADMIN`
- Check ownership/access in service layer, not controller

## Journey Controllers
- Delegate to `Journey.getModelAndViewForStep()` for GET requests
- Delegate to `Journey.completeStep()` for POST requests
- Use `@PathVariable` for step IDs and subpages

## Response Patterns
```kotlin
// Page rendering
return ModelAndView("templateName", mapOf("key" to value))

// Redirect after POST
return RedirectView("/next-page", true)

// With flash attributes
redirectAttributes.addFlashAttribute("message", "Success")
```

## Controller Tests
```kotlin
@WebMvcTest(ExampleController::class)
class ExampleControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    
    @MockitoBean
    private lateinit var myService: MyService
    
    @Test
    fun `returns page with expected content`() {
        mvc.get("/path/page")
            .andExpect { status { isOk() } }
            .andExpect { view { name("templateName") } }
    }
}
```

The `ControllerTest` base class provides `mvc` (MockMvc) and auto-mocks common dependencies: `BackUrlStorageService`, `ClientRegistrationRepository`, `UserRolesService`, `PrsdbWebMvcRegistration`.

## Local API Stubs
- Place in `local/api/controllers/`
- Annotate with `@Profile("local")`
- Match real API contract for integration testing
