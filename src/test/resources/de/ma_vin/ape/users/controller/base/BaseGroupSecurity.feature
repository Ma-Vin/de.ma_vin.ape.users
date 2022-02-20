Feature: Testing security at methods of the base group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And There exists a common group with name "Common Group" with alias "common"

  Scenario: Unauthorized
    Given Use an unknown token
    When The Controller is called to create a base group with name "New Base Group" at common group "common"
    Then The result is a 4xx

  Scenario Outline: Check <role> privilege to create a base group
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When The Controller is called to create a base group with name "New Base Group" at common group "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to delete a base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to delete the base group with the identification of the alias "base"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      | MANAGER     | 2xx           |
      | CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to get and count base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get the base group with the identification of the alias "base"
    Then The result is a <httpCodeRange>
    When Controller is called to get all base groups from common group with alias "common"
    Then The result is a <httpCodeRange>
    When Controller is called to count base groups at common group with alias "common"
    Then The result is a <httpCodeRange>
    When Controller is called to get all base group parts from common group with alias "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
      | VISITOR     | 2xx           |
      | BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to update base group
    Given There exists a base group with name "Base Group Name" with alias "base" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "description" of the base group with alias "base" is set to <value>
    When Controller is called to update the base group with the identification of the alias "base"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | value         |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           | "admin"       |
      | MANAGER     | 2xx           | "manager"     |
      | CONTRIBUTOR | 4xx           | "contributor" |
      #| VISITOR     | 4xx           | "visitor"     |
      #| BLOCKED     | 4xx           | "blocked"     |

  Scenario Outline: Check <role> privilege to add, count and remove base group at privilege one
    Given There exists a privilege group with name "Parent Privilege Group Name" with alias "parentPrivilege" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    And There exists a base group with name "Another Sub Base Group Name" with alias "anotherSubBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" as MANAGER to privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to add the base group with alias "anotherSubBase" as MANAGER to privilege group with alias "parentPrivilege"
    Then The result is a <httpCodeRange>
    When Controller is called to count sub base groups at privilege group with alias "parentPrivilege"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to remove the base group with alias "subBase" from privilege group with alias "parentPrivilege"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | httpCodeRangeCount |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           | 2xx                |
      | MANAGER     | 2xx           | 2xx                |
      | CONTRIBUTOR | 4xx           | 2xx                |
      | VISITOR     | 4xx           | 2xx                |
      | BLOCKED     | 4xx           | 4xx                |

  Scenario Outline: Check <role> privilege to add, count and remove base group at base one
    Given There exists a base group with name "Parent Base Group Name" with alias "parentBase" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    And There exists a base group with name "Another Sub Base Group Name" with alias "anotherSubBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "parentBase"
    Then The result is Ok and Json
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to add the base group with alias "anotherSubBase" to base group with alias "parentBase"
    Then The result is a <httpCodeRange>
    When Controller is called to count sub base groups at base group with alias "parentBase"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to remove the base group with alias "subBase" from base group with alias "parentBase"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | httpCodeRangeCount |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           | 2xx                |
      #| MANAGER     | 2xx           | 2xx                |
      | CONTRIBUTOR | 2xx           | 2xx                |
      | VISITOR     | 4xx           | 2xx                |
      | BLOCKED     | 4xx           | 4xx                |

  Scenario Outline: Check <role> privilege to get sub base groups
    Given There exists a privilege group with name "Parent Privilege Group Name" with alias "parentPrivilege" at common group "common"
    And There exists a base group with name "Parent Base Group Name" with alias "parentBase" at common group "common"
    And There exists a base group with name "Sub Base Group Name" with alias "subBase" at common group "common"
    And There exists a base group with name "Another Sub Base Group Name" with alias "anotherSubBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" as MANAGER to privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    When Controller is called to add the base group with alias "anotherSubBase" to base group with alias "parentBase"
    Then The result is Ok and Json
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get all sub base groups of privilege group with alias "parentPrivilege"
    Then The result is a <httpCodeRange>
    When Controller is called to get all sub base group parts of privilege group with alias "parentPrivilege"
    Then The result is a <httpCodeRange>
    When Controller is called to get all sub groups of base group with alias "parentBase"
    Then The result is a <httpCodeRange>
    When Controller is called to get all sub group parts of base group with alias "parentBase"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
      | VISITOR     | 2xx           |
      | BLOCKED     | 4xx           |
