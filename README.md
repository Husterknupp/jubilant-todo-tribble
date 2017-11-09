# jubilant-todo-tribble
find repo todos, create Jira tickets and reference todos in the tickets.

[![Build Status](https://travis-ci.org/Husterknupp/jubilant-todo-tribble.svg?branch=master)](https://travis-ci.org/Husterknupp/jubilant-todo-tribble)

### Compile & Run
```
mvn package
java -jar target/jubilant-todo-tribble-1.0.0-SNAPSHOT.jar
```

### Configuration
copy `templates/application-local.yaml` -> `<APP_DIR>/application.yaml` and fill with your own data 

### Libraries
- http://khttp.readthedocs.io/en/latest/user/quickstart.html#more-complicated-post-requests
- https://github.com/FasterXML/jackson-module-kotlin

### APIs used
- Jira: https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/updating-an-issue-via-the-jira-rest-apis + https://docs.atlassian.com/jira/REST/server/#api/2/issue-editIssue    
- GitLab: https://docs.gitlab.com/ce/api/commits.html#get-a-single-commit
