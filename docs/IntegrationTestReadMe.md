# Integration Test Framework

We use Playwright to drive our web integration tests. This is configured in our base `IntegrationTest` class.

## Data Seeding

We set seed data by passing SQL script names into integration test class constructors. There are three base classes:

* `IntegrationTestWithMutableData` - resets and seeds the database before each test (use when tests affect the database)
* `IntegrationTestWithImmutableData` - resets and seeds the database before each test class (use when tests don't affect the database)
* `IntegrationTest` - doesn't reset or seed the database (use when **all** tests get their seed data from nested classes)

Tests that require different seed data to the rest of the class must be put in nested classes that inherit from 
`NestedIntegrationTestWithMutableData` or `NestedIntegrationTestWithImmutableData` depending on the outer class.

## Page Objects (and Components)

We encapsulate logic for interacting with our pages into page objects (as described by [Martin Fowler](https://martinfowler.com/bliki/PageObject.html) and
[Selenium](https://www.selenium.dev/documentation/test_practices/encouraged/page_object_models/), amongst others).

To quote Fowler:
> A page object wraps an HTML page, or fragment, with an application-specific API, allowing you to manipulate page
> elements without digging around in the HTML. The basic rule of thumb for a page object is that it should allow a
> software client to do anything and see anything that a human can.

I.e. our page objects provide an API that describes how to interact with the web app, and they use Playwright, which
provides an API to interact with the HTML.

Our code makes a distinction between pages and components (i.e. bits of pages), but there isn't much of philosophical
difference - they both follow the "page object" pattern.

Our page objects (and components) follow a few rules of thumb, described below.

### Page objects are "reactive"

In Playwright, `Locator` is they key interface. It describes how to find an element or elements on the page, and will
do so when calling code attempts to interact with those element(s) (e.g. by calling the `click()` method). This means
that if the page changes over time (whether due to navigation or dynamic behaviour like some JavaScript) the element(s)
found will be the ones that are there at the time of interaction (e.g. `click()`) rather than when the `Locator` was
created.

Page objects follow this same principle.

Typically, we achieve that just by making use of `Locator`s. On occasion, though, we want to provide a property that
requires actually locating elements. In those cases, we retain "reactivity" by using getters on the property:

```kotlin
    val widgetText: String
        get() = widgetLocator.innerText()
```

### Page objects are "scoped"

If one page object is a child of another, then the element(s) it represents are also children of the parent page
object's element(s).

It is for this reason that our page objects take a `parentLocator: Locator` in their constructor (or factory method)

### Properties are preferred to factory methods

We prefer properties (`val someComponent`) to factory methods (e.g. `getComponent()`). Because page
objects are "reactive" (see above), there shouldn't be a problem with creating all the child components when the
parent is constructed.

In some cases, this might mean we need to create specialised subclasses of generic components (e.g. see the many
subclasses of `Form`).

The exception to this is where dynamic values need to be passed in (e.g. `getComponentByUserEnteredName(name)`).

### Properties are preferred to getX methods

We prefer properties to `getSomePropertyOfThePage()` methods. This is purely stylistic, but it tends to make the tests
easier to read.

### Custom classes are preferred to `Locator`

We prefer to expose components as properties, rather than raw `Locator`s, in all but the most trivial of cases. This is
for all the usual reasons we like to follow object orientation - e.g. it makes it easier to extend in future, it hides
away unnecessary (and in some cases invalid) operations, etc.

## Navigation

Integration test classes have a `Navigator`, which is responsible for navigating between pages. There are three types of
navigation method:

* `navigateToX` - navigate to X
* `goToX` - navigate to X and return a page object
* `skipToX` - perform some set-up*, then navigate to X and return a page object

*Make calls to endpoints that configure the session to make the page reachable.