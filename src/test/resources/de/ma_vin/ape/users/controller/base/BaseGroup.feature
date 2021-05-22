Feature: Testing methods of the base group controller

  Background:
    Given There exists a common group with name "Common Group" with alias "common"

  Scenario: Create a base group
    When The Controller is called to create a base group with name "New Base Group" at common group "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "New Base Group"
    And There is any identification at response

  Scenario: Get base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    When Controller is called to get the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "Base Group Name"
    And The identification is the same like the one of alias "base"

  Scenario: Update and get base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    And The "description" of the base group with alias "base" is set to "anythingNew"
    When Controller is called to update the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "base"
    When Controller is called to get the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "base"

  Scenario: Add and remove sub base groups
    Given There exists a base group with name "Parent Base Group Name" with alias "parentBase" at common group "common"
    Given There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all sub groups of base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "subBase"
    And The "identification" property at response position 1 does not exists
    When Controller is called to remove the base group with alias "subBase" from base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all sub groups of base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "identification" property at response position 0 does not exists

  Scenario: Delete base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    Given There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "base"
    When Controller is called to delete the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"
    When Controller is called to get the base group with the identification of the alias "subBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"