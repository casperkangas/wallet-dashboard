## Goal Description
We need to fix two issues from the recent deployment:
1. **Missing Debt Account**: Because the `account_type` column was added *after* the initial sync, accounts that weren't updated recently didn't get their `accountType` pulled from the API. We need to force a full re-sync so all accounts correctly retrieve their types.
2. **Missing Historical Transactions**: The API defaults to returning a maximum of 30 transactions, and enforces a hard limit of 200 per request. We need to implement pagination (using `limit=200` and `offset`) to fetch your complete transaction history.

## Proposed Changes

### 1. Self-Healing Synchronization
#### [MODIFY] src/main/java/sync/IncrementalSynchronizer.java
- **Accounts**: Add self-healing logic. If any account in the database has a `null` `accountType`, we will temporarily ignore the `lastUpdated` timestamp and perform a full sync.
- **Transactions**: Since you only got 30 transactions on the first run, we will add a check: if the total number of transactions in the database is exactly 30 (or less), we will force a full sync to pull the rest of your historical data.
- **Pagination**: Implement a `do-while` loop in both `syncAccountsIncrementally` and `syncTransactionsIncrementally` that appends `&limit=200&offset=X` to the API requests. The loop will continue fetching pages until the API returns fewer than 200 items.

### 2. Debt Widget Logic Enhancements
#### [MODIFY] src/main/java/services/DashboardService.java
- Enhance `getDebtBalance()` to also include accounts with a negative balance just in case they are not explicitly flagged as "CreditCard" or "Loan" by BudgetBakers.

## Verification Plan
1. Compile and run the unit tests.
2. The user will run the application and click **Sync Now**.
3. We expect to see the sync pull significantly more than 30 transactions (fetching all pages of 200).
4. We expect the Debt widget to accurately reflect both credit cards.
