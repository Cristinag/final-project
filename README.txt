How to use the Rest API:

1. Uploading a unified diff containing the changes made since the last build

Send a POST request to https://<host>:<port/testprioritization/ containing the
name of the project and the unified diff.

Example:

curl -X POST \
  -H "Content-Type:multipart/related" \
  --form "diff=@<path-to-diff-file>" \
  --form "project=<projectName>" 
  http://localhost:8080/testprioritization/

When the creation is successful, the HTTP response is a "201 Created" and the 
Location header contains the url of the project ("/<projectName>").

NOTE: I am assuming a unified diff format similar to the one created by git diff.
To see an example check the file src/test/resources/test-data/diff.txt

2. To retrieve a test prioritization corresponding to the last changes posted
for the project, send a GET request to 
https://<host>:<port/testprioritization/<projectName>

Example:
curl -X GET http://localhost:80/testprioritization/<projectName>

On success;
The HTTP response is "200 OK" and a json array is returned with
the names of the tests ordered in decreasing order of their probability to fail.

On failure:
If there no changes(builds) have been uploaded for the given project, 
a "404 Not Found" status is returned with an error of the form:

{ status: 404, 
  description: 'Not found', 
  message: 
    'com.testprioritization.domain.dao.exceptions.NoSuchProjectException:
    No project <projectName> was found.'}


3. Saving the test results

Send a POST request to https://<host>:<port/testprioritization/<projectName>
uploading the test report (which has to be in TAP format).

Example:
curl-X POST -H "Content-Type:multipart/related" \
  --form "test-report=@<test-report-file>" \
   http://localhost:8080/testprioritization/<project-name>

When the creation is successful, the HTTP status is a "201 Created".

If the test results for the last build of the specified project have already
been saved,  the HTTP status is "400 Bad Request" and the response body:
{ status: 400, 
  description: 'Bad request', 
  message: 
    'com.testprioritization.domain.dao.exceptions.TestResultsAlreadyProcessedException: 
    The test results for the last build in project <projectName> have already been saved'}

If the specified project does not exist, the response status is "404 Not Found"
and the message body:
{ status: 404, 
  description: 'Not found', 
  message: 
    'com.testprioritization.domain.dao.exceptions.NoSuchProjectException: 
    No project <project-name> was found.'}