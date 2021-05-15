Feature: Testing methods of the common group controller

  Scenario: Create a common group
    When The Controller is called to create a common group with name "New Common Group"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "New Common Group"
    And There is any identification at response

  Scenario: Get common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    When Controller is called to get the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "Common Group Name"
    And The identification is the same like the one of alias "common"

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
    When Controller is called to delete the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the common group with the identification of the alias "common"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"