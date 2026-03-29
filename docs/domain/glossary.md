## Catalog Context

| Term | Definition |
|---|---|
| **Book** | A bibliographic work identified by an ISBN. Has a title, author(s), and publication year. A Book can have multiple Copies. |
| **Author** | A person who wrote a Book. Represented as a value object (name only — no separate lifecycle). |
| **Copy** | A physical instance of a Book owned by the library. Each Copy has a unique barcode. A Copy is either Available or Borrowed. |
| **Barcode** | A unique identifier for a Copy. Used to identify a specific physical item during lending operations. |
| **Available** | A Copy status meaning it is on the shelf and can be lent out. |
| **Borrowed** | A Copy status meaning it is currently on loan to a Member. |
| **ISBN** | International Standard Book Number. The natural identifier for a Book. |

## Lending Context

| Term | Definition |
|---|---|
| **Member** | A registered library user who can borrow Copies. Identified by a Member ID. Has a borrowing limit. |
| **Member ID** | A unique identifier assigned to a Member upon registration. |
| **Borrowing Limit** | The maximum number of Copies a Member can have on loan simultaneously. Default: 3. |
| **Loan** | The act of lending a Copy to a Member. Tracks the Copy barcode, Member ID, loan date, and due date. |
| **Due Date** | The date by which a Loan must be returned. Calculated as loan date + loan period. |
| **Loan Period** | The standard duration of a Loan. Default: 14 days. |
| **Return** | The act of a Member bringing back a borrowed Copy. Closes the Loan and may trigger a Fee if overdue. |
| **Overdue** | A Loan whose Due Date has passed without a Return. Triggers fee calculation. |
| **Fee** | A monetary charge applied to a Member for an Overdue Loan. Calculated as days overdue × daily rate. |
| **Daily Rate** | The fee charged per day for an Overdue Loan. Default: €0.50. |