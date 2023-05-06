#!/usr/bin/env bash

: ${HOST=streamify.ru}
: ${PORT=443}
: ${USE_K8S=true}
: ${HEALTH_URL=https://health.streamify.ru}
: ${MGM_PORT=4004}
: ${SONG_ID_COMM_RECS=1}
: ${SONG_ID_NOT_FOUND=13}
: ${SONG_ID_NO_RECS=113}
: ${SONG_ID_NO_COMM=213}
: ${SKIP_CB_TESTS=false}
: ${NAMESPACE=streamify}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
    echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo  "- Failing command: $curlCmd"
    echo  "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function testUrl() {
  url=$@
  if $url -ks -f -o /dev/null
  then
    return 0
  else
    return 1
  fi;
}

function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url
  do
    n=$((n + 1))
    if [[ $n == 100 ]]
    then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}

function testCompositeCreated() {

    # Expect that the Song Composite for songId $SONG_ID_COMM_RECS has been created with three recommendations and three comments
    if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS -s"
    then
        echo -n "FAIL"
        return 1
    fi

    set +e
    assertEqual "$SONG_ID_COMM_RECS" $(echo $RESPONSE | jq .songId)
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    assertEqual 3 $(echo $RESPONSE | jq ".comments | length")
    if [ "$?" -eq "1" ] ; then return 1; fi

    set -e
}

function waitForMessageProcessing() {
    echo "Wait for messages to be processed... "

    # Give background processing some time to complete...
    sleep 1

    n=0
    until testCompositeCreated
    do
        n=$((n + 1))
        if [[ $n == 40 ]]
        then
            echo " Give up"
            exit 1
        else
            sleep 6
            echo -n ", retry #$n "
        fi
    done
    echo "All messages are now processed!"
}

function recreateComposite() {
  local songId=$1
  local composite=$2

  assertCurl 202 "curl -X DELETE $AUTH -k https://$HOST:$PORT/api/common/${songId} -s"
  assertEqual 202 $(curl -X POST -s -k https://$HOST:$PORT/api/common -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite" -w "%{http_code}")
}

function setupTestdata() {

  body="{\"songId\":$SONG_ID_NO_RECS"
  body+=\
',"name":"song name A","author":"a1","streamingUrl":"s3", "comments":[
  {"commentId":1,"author":"author 1","content":"content 1"},
  {"commentId":2,"author":"author 2","content":"content 2"},
  {"commentId":3,"author":"author 3","content":"content 3"}
]}'
  recreateComposite "$SONG_ID_NO_RECS" "$body"

  body="{\"songId\":$SONG_ID_NO_COMM"
  body+=\
',"name":"song name B","author":"a2","streamingUrl":"s2", "recommendations":[
  {"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
  {"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
  {"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}
]}'
  recreateComposite "$SONG_ID_NO_COMM" "$body"


  body="{\"songId\":$SONG_ID_COMM_RECS"
  body+=\
',"name":"song name C","author":"a3","streamingUrl":"s3", "recommendations":[
      {"recommendationId":1,"author":"author 1","rate":1,"content":"content 1"},
      {"recommendationId":2,"author":"author 2","rate":2,"content":"content 2"},
      {"recommendationId":3,"author":"author 3","rate":3,"content":"content 3"}
  ], "comments":[
      {"commentId":1,"author":"author 1","content":"content 1"},
      {"commentId":2,"author":"author 2","content":"content 2"},
      {"commentId":3,"author":"author 3","content":"content 3"}
  ]}'
  recreateComposite "$SONG_ID_COMM_RECS" "$body"

}

function testCircuitBreaker() {

    echo "Start Circuit Breaker tests!"

    if [[ $USE_K8S == "false" ]]
    then
        EXEC="docker-compose exec -T composer"
    else
        EXEC="kubectl -n $NAMESPACE exec deploy/composer -c composer -- "
    fi

    # First, use the health - endpoint to verify that the circuit breaker is closed
    assertEqual "CLOSED" "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/health | jq -r .components.circuitBreakers.details.song.details.state)"

    # Open the circuit breaker by running three slow calls in a row, i.e. that cause a timeout exception
    # Also, verify that we get 500 back and a timeout related error message
    for ((n=0; n<3; n++))
    do
        assertCurl 500 "curl -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS?delay=3 $AUTH -s"
        message=$(echo $RESPONSE | jq -r .message)
        assertEqual "Did not observe any item or terminal signal within 2000ms" "${message:0:57}"
    done

    # Verify that the circuit breaker is open
    assertEqual "OPEN" "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/health | jq -r .components.circuitBreakers.details.song.details.state)"

    # Verify that the circuit breaker now is open by running the slow call again, verify it gets 200 back, i.e. fail fast works, and a response from the fallback method.
    assertCurl 200 "curl -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS?delay=3 $AUTH -s"
    assertEqual "Fallback song$SONG_ID_COMM_RECS" "$(echo "$RESPONSE" | jq -r .name)"

    # Also, verify that the circuit breaker is open by running a normal call, verify it also gets 200 back and a response from the fallback method.
    assertCurl 200 "curl -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS $AUTH -s"
    assertEqual "Fallback song$SONG_ID_COMM_RECS" "$(echo "$RESPONSE" | jq -r .name)"

    # Verify that a 404 (Not Found) error is returned for a non existing songId ($SONG_ID_NOT_FOUND) from the fallback method.
    assertCurl 404 "curl -k https://$HOST:$PORT/api/common/$SONG_ID_NOT_FOUND $AUTH -s"
    assertEqual "song Id: $SONG_ID_NOT_FOUND not found in fallback cache!" "$(echo $RESPONSE | jq -r .message)"

    # Wait for the circuit breaker to transition to the half open state (i.e. max 10 sec)
    echo "Will sleep for 10 sec waiting for the CB to go Half Open..."
    sleep 10

    # Verify that the circuit breaker is in half open state
    assertEqual "HALF_OPEN" "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/health | jq -r .components.circuitBreakers.details.song.details.state)"

    # Close the circuit breaker by running three normal calls in a row
    # Also, verify that we get 200 back and a response based on information in the song database
    for ((n=0; n<3; n++))
    do
        assertCurl 200 "curl -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS $AUTH -s"
        assertEqual "song name C" "$(echo "$RESPONSE" | jq -r .name)"
    done

    # Verify that the circuit breaker is in closed state again
    assertEqual "CLOSED" "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/health | jq -r .components.circuitBreakers.details.song.details.state)"

    # Verify that the expected state transitions happened in the circuit breaker
    assertEqual "CLOSED_TO_OPEN"      "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/circuitbreakerevents/song/STATE_TRANSITION | jq -r .circuitBreakerEvents[-3].stateTransition)"
    assertEqual "OPEN_TO_HALF_OPEN"   "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/circuitbreakerevents/song/STATE_TRANSITION | jq -r .circuitBreakerEvents[-2].stateTransition)"
    assertEqual "HALF_OPEN_TO_CLOSED" "$($EXEC curl -s http://localhost:${MGM_PORT}/actuator/circuitbreakerevents/song/STATE_TRANSITION | jq -r .circuitBreakerEvents[-1].stateTransition)"
}

set -e

echo "Start Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"
echo "USE_K8S=${USE_K8S}"
echo "HEALTH_URL=${HEALTH_URL}"
echo "MGM_PORT=${MGM_PORT}"
echo "SKIP_CB_TESTS=${SKIP_CB_TESTS}"

if [[ $@ == *"start"* ]]
then
  echo "Restarting the test environment..."
  echo "$ docker-compose down --remove-orphans"
  docker-compose down --remove-orphans
  echo "$ docker-compose up -d"
  docker-compose up -d
fi

waitForService curl -k $HEALTH_URL/actuator/health

export TENANT=keycloak:9999
export WRITER_CLIENT_ID=writer
export WRITER_CLIENT_SECRET=nkmOLhPB6CF7zRooQ7m080MNUYBpsvAT

ACCESS_TOKEN=$(curl -X POST http://$TENANT/realms/streamify/protocol/openid-connect/token \
  -d grant_type=client_credentials \
  -d redirect_uri=https://localhost:8443/api/common \
  -d scope=song:read+song:write \
  -d client_id=$WRITER_CLIENT_ID \
  -d client_secret=$WRITER_CLIENT_SECRET -s | jq -r .access_token)

echo ACCESS_TOKEN=$ACCESS_TOKEN
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""

setupTestdata

waitForMessageProcessing

# Verify that a normal request works, expect three recommendations and three comments
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS -s"
assertEqual $SONG_ID_COMM_RECS $(echo $RESPONSE | jq .songId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")

# Verify that a 404 (Not Found) error is returned for a non-existing songId ($SONG_ID_NOT_FOUND)
assertCurl 404 "curl $AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_NOT_FOUND -s"
assertEqual "No song found for songId: $SONG_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

# Verify that no recommendations are returned for songId $SONG_ID_NO_RECS
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_NO_RECS -s"
assertEqual $SONG_ID_NO_RECS $(echo $RESPONSE | jq .songId)
assertEqual 0 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 3 $(echo $RESPONSE | jq ".comments | length")

# Verify that no comments are returned for songId $SONG_ID_NO_COMM
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_NO_COMM -s"
assertEqual $SONG_ID_NO_COMM $(echo $RESPONSE | jq .songId)
assertEqual 3 $(echo $RESPONSE | jq ".recommendations | length")
assertEqual 0 $(echo $RESPONSE | jq ".comments | length")

# Verify that a 422 (Unprocessable Entity) error is returned for a songId that is out of range (-1)
assertCurl 422 "curl $AUTH -k https://$HOST:$PORT/api/common/-1 -s"
assertEqual "\"Invalid songId: -1\"" "$(echo $RESPONSE | jq .message)"

# Verify that a 400 (Bad Request) error error is returned for a songId that is not a number, i.e. invalid format
assertCurl 400 "curl $AUTH -k https://$HOST:$PORT/api/common/invalidsongId -s"
assertEqual "\"Type mismatch.\"" "$(echo $RESPONSE | jq .message)"

# Verify that a request without access token fails on 401, Unauthorized
assertCurl 401 "curl -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS -s"

# Verify that the reader - client with only read scope can call the read API but not delete API.
export READER_CLIENT_ID=reader
export READER_CLIENT_SECRET=pqqMqAPRKV8eLSPrnAkCYgUSt8A5RrYB

READER_ACCESS_TOKEN=$(curl -X POST http://$TENANT/realms/streamify/protocol/openid-connect/token \
  -d grant_type=client_credentials \
  -d audience=https://localhost:8443/api/common \
  -d scope=song:read \
  -d client_id=$READER_CLIENT_ID \
  -d client_secret=$READER_CLIENT_SECRET -s | jq -r .access_token)

echo READER_ACCESS_TOKEN=$READER_ACCESS_TOKEN
READER_AUTH="-H \"Authorization: Bearer $READER_ACCESS_TOKEN\""

assertCurl 200 "curl $READER_AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS -s"
assertCurl 403 "curl -X DELETE $READER_AUTH -k https://$HOST:$PORT/api/common/$SONG_ID_COMM_RECS -s"

if [[ $SKIP_CB_TESTS == "false" ]]
then
    testCircuitBreaker
fi

if [[ $@ == *"stop"* ]]
then
    echo "We are done, stopping the test environment..."
    echo "$ docker-compose down"
    docker-compose down
fi

echo "End, all tests OK:" `date`
