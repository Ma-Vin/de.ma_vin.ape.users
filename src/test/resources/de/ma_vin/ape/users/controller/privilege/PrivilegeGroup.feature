Feature: Testing methods of the privilege group controller

  Background:
    Given There exists a common group with name "Common Group" with alias "common"

  Scenario: Create a privilege group
    When The Controller is called to create a privilege group with name "New Privilege Group" at common group "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "New Privilege Group"
    And There is any identification at response

  Scenario: Get privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    When Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "Privilege Group Name"
    And The identification is the same like the one of alias "privilege"

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
    When Controller is called to delete the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"