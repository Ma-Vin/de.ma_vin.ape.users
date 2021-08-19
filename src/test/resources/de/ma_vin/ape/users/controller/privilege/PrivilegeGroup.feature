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

  Scenario: Unauthorized
    Given Use an unknown token
    When The Controller is called to create a privilege group with name "New Privilege Group" at common group "common"
    Then The result is a 4xx

  Scenario Outline: Check <role> privilege to create a privilege group
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When The Controller is called to create a privilege group with name "New Privilege Group" at common group "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 4xx           |
      | VISITOR     | 4xx           |
      | BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to delete a privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to delete the privilege group with the identification of the alias "privilege"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 4xx           |
      | VISITOR     | 4xx           |
      | BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to get and count privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When  Controller is called to get the privilege group with the identification of the alias "privilege"
    Then The result is a <httpCodeRange>
    When Controller is called to count privilege groups at common group with alias "common"
    Then The result is a <httpCodeRange>
    When Controller is called to get all privilege groups from common group with alias "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 2xx           |
      | VISITOR     | 2xx           |
      | BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to update privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "description" of the privilege group with alias "privilege" is set to <value>
    When Controller is called to update the privilege group with the identification of the alias "privilege"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | value         |
      | ADMIN       | 2xx           | "admin"       |
      | MANAGER     | 2xx           | "manager"     |
      | CONTRIBUTOR | 4xx           | "contributor" |
      | VISITOR     | 4xx           | "visitor"     |
      | BLOCKED     | 4xx           | "blocked"     |