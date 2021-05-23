Feature: Testing methods of the user controller

  Background:
    Given There exists a common group with name "Common Group" with alias "common"

  Scenario: Create an user
    When The Controller is called to create an user with first name "New" and last name "User" at common group "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "firstName" property at response is "New"
    And The "lastName" property at response is "User"
    And There is any identification at response

  Scenario: Get user
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "firstName" property at response is "New"
    And The "lastName" property at response is "User"
    And The identification is the same like the one of alias "user"
    Given There exists an user with first name "Another" and last name "User" with alias "anotherUser" at common group "common"
    When Controller is called to get all users from common group with identification of "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user"
    And The identification at 1 is the same like the one of alias "anotherUser"
    And The "identification" property at response position 2 does not exists

  Scenario: Add and remove users at base groups
    Given There exists an user with first name "Direct" and last name "User" with alias "directUser" at common group "common"
    And There exists an user with first name "Indirect" and last name "User" with alias "indirectUser" at common group "common"
    And There exists a base group with name "Base Group" with alias "base" at common group "common"
    And There exists a base group with name "Sub Base Group" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "base"
    Then The result is Ok and Json
    When Controller is called to add the user with alias "directUser" to base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to add the user with alias "indirectUser" to base group with alias "subBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups false
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "directUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "directUser"
    And The identification at 1 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 2 does not exists
    When Controller is called to remove the user with alias "directUser" from base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 1 does not exists

  Scenario: Update and get user
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    And The "mail" of the user with alias "user" is set to "anythingNew"
    When Controller is called to update the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "mail" property at response is "anythingNew"
    And The identification is the same like the one of alias "user"
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "mail" property at response is "anythingNew"
    And The identification is the same like the one of alias "user"

  Scenario: Delete user
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    When Controller is called to delete the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"