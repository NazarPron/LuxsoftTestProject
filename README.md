# LuxsoftTestProject
- replace in memory db (hash map) with real db. Now all concurency managment works based on the rule that account object is always the same what will be a problem if we add new implementation of the account repository.
- add transactions to manage transfers.
- store all transactions logs.
