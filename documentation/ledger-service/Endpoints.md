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

| End point     |Command|Example Input| Implemented |
|---------------|-------------|-------------|-------------|
| /getall       | TBD | TBD | TODO|
| /getbyid      | TBD | TBD | TODO|

### /journal/entries

| End point     |Command|Example Input| Implemented |
|---------------|-------------|-------------|-------------|
| /getall       | TBD | TBD | TODO|
| /getbyid      | TBD | TBD | TODO|
| /getbyfilters | TBD | TBD | TODO|
| /add          | TBD | TBD | TODO|
| /update       | TBD | TBD | TODO|
| /remove       | TBD | TBD | TODO|