# Skill: verify-architecture

Run all architecture fitness functions and report violations.

## Usage

```
/verify-architecture
```

Optionally scope to one context:

```
/verify-architecture <catalog|lending>
```

## Steps

### 1 — Run the tests

**Full verification (both contexts):**
```bash
./gradlew :catalog:test :lending:test --tests "*.arch.*"
```

**Single context:**
```bash
./gradlew :<context>:test --tests "*.arch.*"
```

### 2 — Check for Spring imports in domain layer

```bash
grep -r "org.springframework" \
  catalog/src/main/kotlin/com/library/catalog/domain/ \
  lending/src/main/kotlin/com/library/lending/domain/
```

Any match is a violation — domain classes must be pure Kotlin.

### 3 — Check for cross-context compile dependencies

```bash
grep -r "com.library.catalog" \
  lending/src/main/kotlin/ lending/src/test/kotlin/

grep -r "com.library.lending" \
  catalog/src/main/kotlin/ catalog/src/test/kotlin/
```

Any match is a violation — contexts may only communicate via `:shared` events.

### 4 — Report results

For each check, report:
- **PASS** — no violations found
- **FAIL** — list each violation with file path and line number

If any check fails, suggest the corrective action:
- Spring import in domain → move the class to `api/` or `infra/`, or remove the import
- Cross-context dependency → replace with a domain event in `:shared` and a listener
