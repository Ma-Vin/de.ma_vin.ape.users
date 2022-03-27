Feature: Testing methods of the common group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"

  Scenario: Unauthorized
    Given Use an unknown token
    When The Controller is called to create a common group with name "New Common Group"
    Then The result is a 4xx

  Scenario: Create a common group, but missing privilege
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "Anybody" and last name "User" with alias "anyUser" at common group "common"
    When Controller is called to set the password "1 Dummy Password!" of user with the identification of the alias "anyUser"
    Then The result is Ok and Json
    Given There is token for user with alias "anyUser" and password "1 Dummy Password!"
    When The Controller is called to create a common group with name "New Common Group"
    Then The result is a 4xx

  Scenario Outline: Check <role> privilege to get common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get the common group with the identification of the alias "common"
    Then The result is a <httpCodeRange>
    When Controller is called to get the history of common group with the identification of the alias "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
      | VISITOR     | 2xx           |
      | BLOCKED     | 4xx           |

  Scenario: Check global admin privilege to get all common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    When Controller is called to get all common groups
    Then The result is a 2xx
    When Controller is called to get all common group parts
    Then The result is a 2xx

  Scenario Outline: Check <role> privilege to get all common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get all common groups
    Then The result is a <httpCodeRange>
    When Controller is called to get all common group parts
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      | ADMIN       | 4xx           |
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to Get parent common group of user
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "Anybody" and last name "User" with alias "anyUser" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get the parent common group of user with the identification of the alias "anyUser"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
      | VISITOR     | 2xx           |
      | BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to update common group
    Given There exists a common group with name "Common Group Name" with alias "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "description" of the common group with alias "common" is set to <value>
    When Controller is called to update the common group with the identification of the alias "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | value         |
    # indirect included: test only the httpCode switch from ok to not ok
      | ADMIN       | 2xx           | "admin"       |
      | MANAGER     | 4xx           | "manager"     |
      #| CONTRIBUTOR | 4xx           | "contributor" |
      #| VISITOR     | 4xx           | "visitor"     |
      #| BLOCKED     | 4xx           | "blocked"     |
