## Endpoints for the Ledger Service

### /accounts

| End point     | Command | Example Input                                                                 | Implemented |
|---------------|---------|-------------------------------------------------------------------------------|-------------|
| /getall       | /all    |                                                                               | Y           |
| /getbyid      | /byid   | {"id":"1"}                                                                    | Y           |
| /getbyfilters | TBD     | TBD                                                                           | TODO        |
| /add          | /add    | {"typeId": 1,"description":"Testing Adding Account"}                          | Y           |
| /update       | /update | {"id":"2","description": "optional","notes":"Super optional","active": false} | Y           |

### /accounttypes

| End point     | Command | Example Input                                                          | Implemented |
|---------------|---------|------------------------------------------------------------------------|-------------|
| /getall       | /all    |                                                                        | Y           |
| /getbyid      | /byid   | /1                                                                     | Y           |
| /getbyfilters | TBD     | TBD                                                                    | TODO        |
| /add          | /add    | {"classificationId":"1","description":"Testing without notes section"} | Y           |
| /update       | /update | {"id":"1","description":"Active was updated","active": true}           | Y           |

### /accountclassifications

| End point     |Command| Example Input | Implemented |
|---------------|-------------|---------------|-------------|
| /getall       | /all |               | Y           |
| /getbyid      | /byid | /1            | Y           |

### /journal/entries

| End point     |Command| Example Input | Implemented |
|---------------|-------------|---------------|-------------|
| /getall       | /all |               | Y           |
| /getbyid      | /byid | /1            | Y        |
| /getbyfilters | TBD | TBD           | TODO        |
| /add          | /add | {"entryDate": "2026-03-16","description": "Three-line split test","notes": "Testing multi-line entry","journalLines": [{"amount": 10000,"accountId": 1,"direction": "D","notes": "Main debit"}, {"amount": 6000,"accountId": 2,"direction": "C","notes": "First credit"}, {"amount": 4000,"accountId": 3,"direction": "C","notes": "Second credit"}]}         | Y        |
| /update       | /update | {"id": 1,"description": "Initial checking to groceries test - updated","notes": "Updated journal entry notes","journalLines": [{"id": 1,"notes": "Updated debit note"}, {"id": 2,"notes": "Updated credit note"}]}           | Y        |