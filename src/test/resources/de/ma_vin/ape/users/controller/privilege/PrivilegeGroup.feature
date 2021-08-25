Feature: Testing methods of the privilege group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And There exists a common group with name "Common Group" with alias "common"

  Scenario: Create a privilege group
    When The Controller is called to create a privilege group with name "New Privilege Group" at common group "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "New Privilege Group"
    And There is any identification at response

  Scenario: Get and count privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    When Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "Privilege Group Name"
    And The identification is the same like the one of alias "privilege"
    When Controller is called to count privilege groups at common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to get all privilege groups from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "privilege"
    And The "identification" property at response position 1 does not exists

  Scenario: Update and get privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And The "description" of the privilege group with alias "privilege" is set to "anythingNew"
    When Controller is called to update the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "privilege"
    When Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "privilege"

  Scenario: Delete privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" as MANAGER to privilege group with alias "privilege"
    Then The result is Ok and Json
    When Controller is called to delete the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"
    When Controller is called to get the base group with the identification of the alias "subBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"

  Scenario: Get all privilege groups with pages
    Given There are privilege groups with name and alias at common group "common"
      | 1. Privilege Group Name | privilege1 |
      | 2. Privilege Group Name | privilege2 |
      | 3. Privilege Group Name | privilege3 |
      | 4. Privilege Group Name | privilege4 |
      | 5. Privilege Group Name | privilege5 |
    When Controller is called to get all privilege groups at page 0 with size 4 from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "privilege1"
    And The identification at 1 is the same like the one of alias "privilege2"
    And The identification at 2 is the same like the one of alias "privilege3"
    And The identification at 3 is the same like the one of alias "privilege4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all privilege groups at page 1 with size 4 from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "privilege5"
    And The "identification" property at response position 1 does not exists