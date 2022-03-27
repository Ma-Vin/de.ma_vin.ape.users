Feature: Testing security at methods of the admin group controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And The admin group with identification "AGAA00001" is known as alias "adminGroup"
    And There exists a common group with name "Common Group" with alias "common"

  Scenario: Unauthorized
    Given Use an unknown token
    When The Controller is called to create an admin with first name "New" and last name "User" at admin group "adminGroup"
    Then The result is a 4xx

  Scenario Outline: Check <role> privilege to create an admin
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When The Controller is called to create an admin with first name "New" and last name "User" at admin group "adminGroup"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to delete an admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "adminToDelete" at admin group "adminGroup"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to delete the admin with the identification of the alias "adminToDelete"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to get and count an admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "adminToGet" at admin group "adminGroup"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get the admin with the identification of the alias "adminToGet"
    Then The result is a <httpCodeRange>
    When Controller is called to count admins at admin group with alias "adminGroup"
    Then The result is a <httpCodeRange>
    When Controller is called to get all admins from admin group with identification of "adminGroup"
    Then The result is a <httpCodeRange>
    When Controller is called to get all admin parts from admin group with identification of "adminGroup"
    Then The result is a <httpCodeRange>
    When Controller is called to get the history of admin with the identification of the alias "adminToGet"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to update admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "adminToUpdate" at admin group "adminGroup"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "mail" of the admin with alias "adminToUpdate" is set to "anythingNew"
    When Controller is called to update the admin with the identification of the alias "adminToUpdate"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to set password of admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "adminToUpdate" at admin group "adminGroup"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to set the password "Abcdefg123_!" of admin with the identification of the alias "adminToUpdate"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to get admin group
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get the admin group with the identification of the alias "adminGroup"
    Then The result is a <httpCodeRange>
    When Controller is called to get the history of admin group with the identification of the alias "adminGroup"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to update admin group
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "description" of the admin group with alias "adminGroup" is set to "Anything"
    When Controller is called to update the admin group with the identification of the alias "adminGroup"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
      | ADMIN       | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| MANAGER     | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |