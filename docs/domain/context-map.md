## Overview

```mermaid
graph LR
  subgraph Library System
    Catalog["Catalog Context"]
    Lending["Lending Context"]
  end

  Catalog -- "CopyAvailabilityChanged" --> Lending
  Lending -- "queries copy availability" --> Catalog

  Catalog -. "upstream" .-> Lending
```

## Relationships

### Catalog → Lending (Upstream / Downstream)

- Catalog publishes `CopyAvailabilityChanged` events. Lending subscribes to stay in sync.
- Lending conforms to Catalog's model of what a "copy" is — it does not redefine book or copy concepts.

### Lending → Catalog (Query)

- Lending may query Catalog for current copy availability before creating a loan (synchronous read, no state mutation).
