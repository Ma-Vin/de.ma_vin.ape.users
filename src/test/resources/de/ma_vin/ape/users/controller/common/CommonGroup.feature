Feature: Testing methods of the common group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"

  Scenario: Create a common group
    When The Controller is called to create a common group with name "New Common Group"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "New Common Group"
    And There is any identification at response

  Scenario: Create a common group, but missing privilege
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "Anybody" and last name "User" with alias "anyUser" at common group "common"
    When Controller is called to set the password "1 Dummy Password!" of user with the identification of the alias "anyUser"
    Then The result is Ok and Json
    Given There is token for user with alias "anyUser" and password "1 Dummy Password!"
    When The Controller is called to create a common group with name "New Common Group"
    Then The result is a 4xx

  Scenario: Get common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    When Controller is called to get the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "Common Group Name"
    And The identification is the same like the one of alias "common"

  Scenario: Get all common groups with and without pages
    Given All existing common groups are deleted
    And There are common groups with group name
      | 1. common group | group1 |
      | 2. common group | group2 |
      | 3. common group | group3 |
      | 4. common group | group4 |
      | 5. common group | group5 |
    When Controller is called to get all common groups
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "group1"
    And The identification at 1 is the same like the one of alias "group2"
    And The identification at 2 is the same like the one of alias "group3"
    And The identification at 3 is the same like the one of alias "group4"
    And The identification at 4 is the same like the one of alias "group5"
    And The "identification" property at response position 5 does not exists
    When Controller is called to get all common groups at page 0 with size 4
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "group1"
    And The identification at 1 is the same like the one of alias "group2"
    And The identification at 2 is the same like the one of alias "group3"
    And The identification at 3 is the same like the one of alias "group4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all common groups at page 1 with size 4
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "group5"
    And The "identification" property at response position 1 does not exists

  Scenario: Get all common groups, but missing privilege
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "Anybody" and last name "User" with alias "anyUser" at common group "common"
    When Controller is called to set the password "1 Dummy Password!" of user with the identification of the alias "anyUser"
    Then The result is Ok and Json
    Given There is token for user with alias "anyUser" and password "1 Dummy Password!"
    When Controller is called to get all common groups
    Then The result is a 4xx

  Scenario: Update and get common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    And The "description" of the common group with alias "common" is set to "anythingNew"
    When Controller is called to update the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "common"
    When Controller is called to get the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "common"

  Scenario: Delete common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    And There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And There exists an user with first name "Some" and last name "User" with alias "user" at common group "common"
    When Controller is called to delete the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"
    When Controller is called to get the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"
    When Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"

  Scenario: Delete common group, but missing privilege
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "Anybody" and last name "User" with alias "anyUser" at common group "common"
    When Controller is called to set the password "1 Dummy Password!" of user with the identification of the alias "anyUser"
    Then The result is Ok and Json
    Given There is token for user with alias "anyUser" and password "1 Dummy Password!"
    When Controller is called to delete the common group with the identification of the alias "common"
    Then The result is a 4xx