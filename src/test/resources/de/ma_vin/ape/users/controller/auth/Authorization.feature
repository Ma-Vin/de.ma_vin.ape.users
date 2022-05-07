Feature: Testing methods of the authorization controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And No user is given
    And No tokens and code is given
    And No scope is given
    And No state is given

  Scenario: Authorize with response type code
    Given The scope is "READ"
    And The state is "SomeState"
    When The user "UAA00001" is logged in with password "admin"
    Then The result is successful logged in
    When The Controller is called to authorize with response type CODE
    Then The result is Ok and Json
    And The "code" property is anything
    And The "state" property is "SomeState"

  Scenario: Authorize with response type code but not logged in
    Given The scope is "READ"
    Given The state is "SomeState"
    When The Controller is called to authorize with response type CODE
    Then The result is redirected to location "https://localhost/login"

  Scenario: Authorize with response type token
    Given The scope is "READ"
    When The user "UAA00001" is logged in with password "admin"
    Then The result is successful logged in
    When The Controller is called to authorize with response type TOKEN
    Then The result is Ok and Json
    And The "access_token" property is anything
    And The "refresh_token" property is anything
    And The "scope" property is "read"

  Scenario: Authorize with response type token but not logged in
    Given The scope is "READ"
    When The Controller is called to authorize with response type TOKEN
    Then The result is redirected to location "https://localhost/login"

  Scenario: Issue token with grand type password
    Given The scope is "WRITE"
    And The user has id "UAA00001" with password "admin"
    When The Controller is called to issue a token with grand type PASSWORD
    Then The result is Ok and Json
    And The "access_token" property is anything
    And The "refresh_token" property is anything
    And The "scope" property is "write"

  Scenario: Issue token with grand type password but wrong password
    Given The scope is "WRITE"
    And The user has id "UAA00001" with password "anythingElse"
    When The Controller is called to issue a token with grand type PASSWORD
    Then The result is a 4xx

  Scenario: Issue token with grand type client
    Given The scope is "WRITE|READ"
    When The Controller is called to issue a token with grand type CLIENT_CREDENTIALS
    Then The result is Ok and Json
    And The "access_token" property is anything
    And The "refresh_token" property is anything
    And The "scope" property is "read|write"

  Scenario: Issue token with grand type client but wrong client secret
    Given The scope is "WRITE|READ"
    And The clientSecret is "anythingWrong"
    When The Controller is called to issue a token with grand type CLIENT_CREDENTIALS
    Then The result is a 4xx

  Scenario: Issue token with grand type code
    Given The scope is "READ"
    And The state is "SomeState"
    When The user "UAA00001" is logged in with password "admin"
    Then The result is successful logged in
    When The Controller is called to authorize with response type CODE
    Then The result is Ok and Json
    And The code is taken over
    Given No scope is given
    And No state is given
    When The Controller is called to issue a token with grand type AUTHORIZATION_CODE
    Then The result is Ok and Json
    And The "access_token" property is anything
    And The "refresh_token" property is anything
    And The "scope" property is "read"

  Scenario: Issue token with grand type code but wrong code
    Given The scope is "READ"
    And The state is "SomeState"
    When The user "UAA00001" is logged in with password "admin"
    Then The result is successful logged in
    When The Controller is called to authorize with response type CODE
    Then The result is Ok and Json
    Given The authorization code is "anythingWrong"
    And No scope is given
    And No state is given
    When The Controller is called to issue a token with grand type AUTHORIZATION_CODE
    Then The result is a 4xx

  Scenario: Issue token with grand type refresh token
    Given The scope is "WRITE|READ"
    When The Controller is called to issue a token with grand type CLIENT_CREDENTIALS
    Then The result is Ok and Json
    And The token is taken over
    And The refresh token is taken over
    When The Controller is called to issue a token with grand type REFRESH_TOKEN
    Then The result is Ok and Json
    And The "access_token" property is anything
    And The "refresh_token" property is anything
    And The "scope" property is "read|write"
    And The token changes
    And The refresh token changes

  Scenario: Issue token with grand type refresh token but not invalid refresh token
    Given The refresh token is "anythingWrong"
    When The Controller is called to issue a token with grand type REFRESH_TOKEN
    Then The result is a 4xx