Feature: Testing methods of the privilege group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And There exists a common group with name "Common Group" with alias "common"

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
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to delete a privilege group
    Given There exists a privilege group with name "Privilege Group Name" with alias "privilege" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to delete the privilege group with the identification of the alias "privilege"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

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
    When Controller is called to get all privilege group parts from common group with alias "common"
    Then The result is a <httpCodeRange>
    When Controller is called to get the history of privilege group with the identification of the alias "privilege"
    Then The result is a <httpCodeRange>
    When Controller is called to get the privilege groups of user with alias "user"
    Then The result is a <httpCodeRange>
    When Controller is called to get the privilege group parts of user with alias "user"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
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
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           | "admin"       |
      | MANAGER     | 2xx           | "manager"     |
      | CONTRIBUTOR | 4xx           | "contributor" |
      #| VISITOR     | 4xx           | "visitor"     |
      #| BLOCKED     | 4xx           | "blocked"     |