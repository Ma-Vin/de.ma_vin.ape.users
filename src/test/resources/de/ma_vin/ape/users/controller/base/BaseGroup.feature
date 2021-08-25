Feature: Testing methods of the base group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And There exists a common group with name "Common Group" with alias "common"

  Scenario: Create a base group
    When The Controller is called to create a base group with name "New Base Group" at common group "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "New Base Group"
    And There is any identification at response

  Scenario: Get and count base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    When Controller is called to get the base group with the identification of the alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "Base Group Name"
    And The identification is the same like the one of alias "base"
    When Controller is called to count base groups at common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to get all base groups from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "base"
    And The "identification" property at response position 1 does not exists

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

  Scenario: Add, count and remove sub base groups
    Given There exists a base group with name "Parent Base Group Name" with alias "parentBase" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to count sub base groups at base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
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

  Scenario: Add, count and remove base groups at privilege one
    Given There exists a privilege group with name "Parent Privilege Group Name" with alias "parentPrivilege" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" as MANAGER to privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to count sub base groups at privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to get all sub base groups of privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "subBase"
    And The "identification" property at response position 1 does not exists
    When Controller is called to remove the base group with alias "subBase" from privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all sub base groups of privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "identification" property at response position 0 does not exists

  Scenario: Delete base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "base"
    Then The result is Ok and Json
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

  Scenario: Get all base groups with pages
    Given There exists a base group with name "Parent Base Group Name" with alias "parentBase" at common group "common"
    And There exists a privilege group with name "Parent Privilege Group Name" with alias "parentPrivilege" at common group "common"
    And There are base groups with name and alias at common group "common"
      | 1. Base Group Name | base1 |
      | 2. Base Group Name | base2 |
      | 3. Base Group Name | base3 |
      | 4. Base Group Name | base4 |
      | 5. Base Group Name | base5 |
    When Controller is called to get all base groups at page 0 with size 4 from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "parentBase"
    And The identification at 1 is the same like the one of alias "base1"
    And The identification at 2 is the same like the one of alias "base2"
    And The identification at 3 is the same like the one of alias "base3"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all base groups at page 1 with size 4 from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "base4"
    And The identification at 1 is the same like the one of alias "base5"
    And The "identification" property at response position 2 does not exists
    Given The base groups are added to base group
      | base1 | parentBase |
      | base2 | parentBase |
      | base3 | parentBase |
      | base4 | parentBase |
      | base5 | parentBase |
    When Controller is called to get all base groups at page 0 with size 4 from base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "base1"
    And The identification at 1 is the same like the one of alias "base2"
    And The identification at 2 is the same like the one of alias "base3"
    And The identification at 3 is the same like the one of alias "base4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all base groups at page 1 with size 4 from base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "base5"
    And The "identification" property at response position 1 does not exists
    Given The base groups are added to privilege group with role
      | base1 | parentPrivilege | ADMIN       |
      | base2 | parentPrivilege | MANAGER     |
      | base3 | parentPrivilege | CONTRIBUTOR |
      | base4 | parentPrivilege | VISITOR     |
      | base5 | parentPrivilege | BLOCKED     |
    When Controller is called to get all base groups at page 0 with size 4 from privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "base1"
    And The identification at 1 is the same like the one of alias "base2"
    And The identification at 2 is the same like the one of alias "base3"
    And The identification at 3 is the same like the one of alias "base4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all base groups at page 1 with size 4 from privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "base5"
    And The "identification" property at response position 1 does not exists