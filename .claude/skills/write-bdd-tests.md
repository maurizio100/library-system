# Skill: write-bdd-tests

Write Cucumber BDD step definitions for a feature, wiring every Gherkin scenario to the real API via MockMvc.

## Usage

```
/write-bdd-tests <epic> <story-id>
```

Example:
- `/write-bdd-tests catalog 006`
- `/write-bdd-tests lending 003`

## Before you start

- Read the `.feature` file at `docs/stories/<epic>/<NNN>-<slug>.feature`
- Read any existing step defs in `<epic>/src/test/kotlin/com/library/<epic>/bdd/steps/` — reuse existing `@Given`/`@When`/`@Then` steps before writing new ones
- Check `ScenarioState.kt` for shared state fields already available

---

## File layout

```
<epic>/src/test/kotlin/com/library/<epic>/bdd/
├── CucumberSpringConfig.kt   ← already exists, do not touch
├── CucumberTest.kt           ← already exists, do not touch
├── ScenarioState.kt          ← shared state bean, extend if needed
├── TestEventListener.kt      ← event capture bean, extend if needed
└── steps/
    └── <Feature>StepDefs.kt  ← one file per feature
```

---

## Step definition class structure

```kotlin
package com.library.<epic>.bdd.steps

import com.library.<epic>.bdd.ScenarioState
import com.library.<epic>.bdd.TestEventListener
import com.library.<epic>.infra.persistence.<X>JpaRepository
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class <Feature>StepDefs {

    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var <x>JpaRepository: <X>JpaRepository
    @Autowired private lateinit var testEventListener: TestEventListener
    @Autowired private lateinit var scenarioState: ScenarioState

    @Before
    fun setUp() {
        <x>JpaRepository.deleteAll()
        testEventListener.clear()
        scenarioState.clear()
    }

    // step definitions below
}
```

---

## Making HTTP calls

Use `MockMvc`. Store the result in `scenarioState.lastMvcResult` so `Then` steps can inspect the response.

```kotlin
@When("I add a book with ISBN {string}, title {string}, author {string}, and publication year {int}")
fun iAddBook(isbn: String, title: String, author: String, year: Int) {
    val result = mockMvc.perform(
        post("/api/catalog/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"isbn":"$isbn","title":"$title","authors":["$author"],"publicationYear":$year}""")
    ).andReturn()
    scenarioState.lastMvcResult = result
}
```

---

## Asserting responses

**Success (2xx):** check the response body contains expected fields.

```kotlin
@Then("the catalog contains a book with ISBN {string}")
fun theCatalogContainsBook(isbn: String) {
    assertTrue(bookJpaRepository.findById(isbn).isPresent)
}
```

**Failure (4xx):** check status code range and that the error message appears in the response body.

```kotlin
@Then("the book is rejected with reason {string}")
fun theBookIsRejected(reason: String) {
    val status = scenarioState.lastMvcResult!!.response.status
    assertTrue(status in 400..499, "Expected 4xx but got $status")
    val body = scenarioState.lastMvcResult!!.response.contentAsString
    assertTrue(body.contains(reason), "Expected '$reason' in: $body")
}
```

**Event published:**

```kotlin
@And("a BookAdded event is published with ISBN {string}")
fun bookAddedEventPublished(isbn: String) {
    val events = testEventListener.getBookAddedEvents()
    assertEquals(1, events.size)
    assertEquals(isbn, events[0].isbn)
}
```

---

## Extending ScenarioState

If a scenario needs to pass data between steps (e.g. a created loan ID), add a nullable field to `ScenarioState.kt` and call `clear()` resets it.

```kotlin
@Component
class ScenarioState {
    var lastMvcResult: MvcResult? = null
    var lastLoanId: String? = null       // ← add what you need

    fun clear() {
        lastMvcResult = null
        lastLoanId = null
    }
}
```

---

## Extending TestEventListener

To capture a new event type, add a list and a getter to `TestEventListener.kt`:

```kotlin
@Component
class TestEventListener {
    private val loanCreatedEvents = mutableListOf<LoanCreated>()

    @EventListener
    fun on(event: LoanCreated) { loanCreatedEvents.add(event) }

    fun getLoanCreatedEvents(): List<LoanCreated> = loanCreatedEvents.toList()

    fun clear() {
        loanCreatedEvents.clear()
        // clear other lists...
    }
}
```

---

## Running the tests

```bash
./gradlew :<epic>:test --tests "*.bdd.*"
```

---

## Checklist before committing

- [ ] Every Gherkin scenario in the `.feature` file has a wired step definition
- [ ] `@Before` clears the database and event listener state
- [ ] No raw SQL or JPA calls in step defs that belong in the API (call the endpoint instead)
- [ ] Failure assertions check both status code range AND error message content
- [ ] All tests pass: `./gradlew :<epic>:test`
