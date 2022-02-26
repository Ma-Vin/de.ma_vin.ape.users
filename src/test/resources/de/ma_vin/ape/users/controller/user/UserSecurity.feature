Feature: Testing methods of the user controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And There exists a common group with name "Common Group" with alias "common"

  Scenario: Unauthorized
    Given Use an unknown token
    When The Controller is called to create an user with first name "New" and last name "User" at common group "common"
    Then The result is a 4xx

  Scenario Outline: Check <role> privilege to create an user
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When The Controller is called to create an user with first name "New" and last name "User" at common group "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      | CONTRIBUTOR | 2xx           |
      | VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to delete an user with role <roleOfToDelete>
    Given There exists an user with first name "New" and last name "User" with alias "userToDelete" at common group "common"
    Given There exists an user with first name "New", last name "User", password "1 Dummy Password!" and role <roleOfToDelete> with alias "userToDelete" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to delete the user with the identification of the alias "userToDelete"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | roleOfToDelete | httpCodeRange |
      | ADMIN       | ADMIN          | 2xx           |
      | MANAGER     | ADMIN          | 4xx           |
    # indirect included: test only the httpCode switch from ok to not ok
      #| CONTRIBUTOR | ADMIN          | 4xx           |
      #| VISITOR     | ADMIN          | 4xx           |
      #| BLOCKED     | ADMIN          | 4xx           |
      | ADMIN       | MANAGER        | 2xx           |
      | MANAGER     | MANAGER        | 4xx           |
      #| CONTRIBUTOR | MANAGER        | 4xx           |
      #| VISITOR     | MANAGER        | 4xx           |
      #| BLOCKED     | MANAGER        | 4xx           |
      #| ADMIN       | CONTRIBUTOR    | 2xx           |
      | MANAGER     | CONTRIBUTOR    | 2xx           |
      #| CONTRIBUTOR | CONTRIBUTOR    | 4xx           |
      #| VISITOR     | CONTRIBUTOR    | 4xx           |
      #| BLOCKED     | CONTRIBUTOR    | 4xx           |
      #| ADMIN       | VISITOR        | 2xx           |
      | MANAGER     | VISITOR        | 2xx           |
      | CONTRIBUTOR | VISITOR        | 4xx           |
      #| VISITOR     | VISITOR        | 4xx           |
      #| BLOCKED     | VISITOR        | 4xx           |
      #| ADMIN       | BLOCKED        | 2xx           |
      | MANAGER     | BLOCKED        | 2xx           |
      | CONTRIBUTOR | BLOCKED        | 4xx           |
      #| VISITOR     | BLOCKED        | 4xx           |
      #| BLOCKED     | BLOCKED        | 4xx           |

  Scenario Outline: Check <role> privilege to get and count an user
    Given There exists an user with first name "New" and last name "User" with alias "userToGet" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get the user with the identification of the alias "userToGet"
    Then The result is a <httpCodeRange>
    When Controller is called to count users at common group with alias "common"
    Then The result is a <httpCodeRange>
    When Controller is called to get all users from common group with identification of "common"
    Then The result is a <httpCodeRange>
    When Controller is called to get all user parts from common group with identification of "common"
    Then The result is a <httpCodeRange>
    Examples:
      | role    | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
      | VISITOR | 2xx           |
      | BLOCKED | 4xx           |

  Scenario Outline: Check <role> privilege to update <testName> with role <roleOfToUpdate>
    Given There exists an user with first name "New", last name "User", password "1 Dummy Password!" and role <roleOfToUpdate> with alias "anOtherUser" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "mail" of the user with alias <aliasToUpdate> is set to "anythingNew"
    When Controller is called to update the user with the identification of the alias <aliasToUpdate>
    Then The result is a <httpCodeRange>
    Examples:
      | role        | roleOfToUpdate | httpCodeRange | aliasToUpdate | testName      |
    # indirect included: test only the httpCode switch from ok to not ok
      | ADMIN       | ADMIN          | 2xx           | "anOtherUser" | an other user |
      | MANAGER     | ADMIN          | 4xx           | "anOtherUser" | an other user |
      #| CONTRIBUTOR | ADMIN          | 4xx           | "anOtherUser" | an other user |
      #| VISITOR     | ADMIN          | 4xx           | "anOtherUser" | an other user |
      #| BLOCKED     | ADMIN          | 4xx           | "anOtherUser" | an other user |
      | ADMIN       | MANAGER        | 2xx           | "anOtherUser" | an other user |
      | MANAGER     | MANAGER        | 4xx           | "anOtherUser" | an other user |
      #| CONTRIBUTOR | MANAGER        | 4xx           | "anOtherUser" | an other user |
      #| VISITOR     | MANAGER        | 4xx           | "anOtherUser" | an other user |
      #| BLOCKED     | MANAGER        | 4xx           | "anOtherUser" | an other user |
      #| ADMIN       | CONTRIBUTOR    | 2xx           | "anOtherUser" | an other user |
      | MANAGER     | CONTRIBUTOR    | 2xx           | "anOtherUser" | an other user |
      | CONTRIBUTOR | CONTRIBUTOR    | 4xx           | "anOtherUser" | an other user |
      #| VISITOR     | CONTRIBUTOR    | 4xx           | "anOtherUser" | an other user |
      #| BLOCKED     | CONTRIBUTOR    | 4xx           | "anOtherUser" | an other user |
      #| ADMIN       | VISITOR        | 2xx           | "anOtherUser" | an other user |
      | MANAGER     | VISITOR        | 2xx           | "anOtherUser" | an other user |
      | CONTRIBUTOR | VISITOR        | 4xx           | "anOtherUser" | an other user |
      #| VISITOR     | VISITOR        | 4xx           | "anOtherUser" | an other user |
      #| BLOCKED     | VISITOR        | 4xx           | "anOtherUser" | an other user |
      #| ADMIN       | BLOCKED        | 2xx           | "anOtherUser" | an other user |
      | MANAGER     | BLOCKED        | 2xx           | "anOtherUser" | an other user |
      | CONTRIBUTOR | BLOCKED        | 4xx           | "anOtherUser" | an other user |
      #| VISITOR     | BLOCKED        | 4xx           | "anOtherUser" | an other user |
      #| BLOCKED     | BLOCKED        | 4xx           | "anOtherUser" | an other user |
      #| ADMIN       | ADMIN          | 2xx           | "user"        | itself        |
      #| MANAGER     | MANAGER        | 2xx           | "user"        | itself        |
      #| CONTRIBUTOR | CONTRIBUTOR    | 2xx           | "user"        | itself        |
      | VISITOR     | VISITOR        | 2xx           | "user"        | itself        |
      | BLOCKED     | BLOCKED        | 4xx           | "user"        | itself        |

  Scenario Outline: Check <role> privilege to set password of <testName>
    Given There exists an user with first name "New" and last name "User" with alias "anOtherUser" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    And The "mail" of the user with alias <aliasToUpdate> is set to "anythingNew"
    When Controller is called to set the password "Abcdefg123_!" of user with the identification of the alias <aliasToUpdate>
    Then The result is a <httpCodeRange>
    Examples:
      | role    | httpCodeRange | aliasToUpdate | testName      |
    # indirect included: test only the httpCode switch from ok to not ok
      | ADMIN   | 2xx           | "anOtherUser" | an other user |
      | MANAGER | 4xx           | "anOtherUser" | an other user |
      #| CONTRIBUTOR | 4xx           | "anOtherUser" | an other user |
      #| VISITOR     | 4xx           | "anOtherUser" | an other user |
      #| BLOCKED     | 4xx           | "anOtherUser" | an other user |
      #| ADMIN       | 2xx           | "user"        | itself        |
      #| MANAGER     | 2xx           | "user"        | itself        |
      #| CONTRIBUTOR | 2xx           | "user"        | itself        |
      | VISITOR | 2xx           | "user"        | itself        |
      | BLOCKED | 4xx           | "user"        | itself        |

  Scenario Outline: Check <role> privilege to set the role of an user
    Given There exists an user with first name "New" and last name "User" with alias "userToUpdate" at common group "common"
    And There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to set the role CONTRIBUTOR of user with the identification of the alias "userToUpdate"
    Then The result is a <httpCodeRange>
    Examples:
      | role    | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      | ADMIN   | 2xx           |
      | MANAGER | 4xx           |
      #| CONTRIBUTOR | 4xx           |
      #| VISITOR     | 4xx           |
      #| BLOCKED     | 4xx           |

  Scenario Outline: Check <role> privilege to add, count and remove user at privilege group
    Given There exists a privilege group with name "Privilege Group" with alias "privilege" at common group "common"
    And There exists an user with first name "ToAdd" and last name "User" with alias "userToAdd" at common group "common"
    And There exists an user with first name "Added" and last name "User" with alias "userAdded" at common group "common"
    When Controller is called to add the user with alias "userAdded" as CONTRIBUTOR to privilege group with alias "privilege"
    Then The result is Ok and Json
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to add the user with alias "userToAdd" as MANAGER to privilege group with alias "privilege"
    Then The result is a <httpCodeRange>
    When Controller is called to count users at privilege group with alias "privilege" with role NOT_RELEVANT
    Then The result is a <httpCodeRangeCount>
    When Controller is called to get all user of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups false
    Then The result is a <httpCodeRangeCount>
    When Controller is called to get all user parts of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups false
    Then The result is a <httpCodeRangeCount>
    When Controller is called to count available users for privilege group with alias "privilege"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to get all available user parts for privilege group with alias "privilege"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to remove the user with alias "userAdded" from privilege group with alias "privilege"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | httpCodeRangeCount |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           | 2xx                |
      | MANAGER     | 2xx           | 2xx                |
      | CONTRIBUTOR | 4xx           | 2xx                |
      | VISITOR     | 4xx           | 2xx                |
      | BLOCKED     | 4xx           | 4xx                |

  Scenario Outline: Check <role> privilege to add, count and remove user at base group
    Given There exists a base group with name "Base Group" with alias "base" at common group "common"
    And There exists an user with first name "ToAdd" and last name "User" with alias "userToAdd" at common group "common"
    And There exists an user with first name "Added" and last name "User" with alias "userAdded" at common group "common"
    When Controller is called to add the user with alias "userAdded" to base group with alias "base"
    Then The result is Ok and Json
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to add the user with alias "userToAdd" to base group with alias "base"
    Then The result is a <httpCodeRange>
    When Controller is called to count users at base group with alias "base"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups false
    Then The result is a <httpCodeRangeCount>
    When Controller is called to get all user parts of base group with alias "base" and dissolving sub groups false
    Then The result is a <httpCodeRangeCount>
    When Controller is called to count available users for base group with alias "base"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to get all available user parts for base group with alias "base"
    Then The result is a <httpCodeRangeCount>
    When Controller is called to remove the user with alias "userAdded" from base group with alias "base"
    Then The result is a <httpCodeRange>
    Examples:
      | role        | httpCodeRange | httpCodeRangeCount |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           | 2xx                |
      #| MANAGER     | 2xx           | 2xx                |
      | CONTRIBUTOR | 2xx           | 2xx                |
      | VISITOR     | 4xx           | 2xx                |
      | BLOCKED     | 4xx           | 4xx                |

  Scenario Outline: Check <role> privilege to get sub users
    Given There exists a base group with name "Base Group" with alias "base" at common group "common"
    And There exists a privilege group with name "Privilege Group" with alias "privilege" at common group "common"
    And There exists an user with first name "base" and last name "User" with alias "userAtBase" at common group "common"
    And There exists an user with first name "privilege" and last name "User" with alias "userAtPrivilege" at common group "common"
    When Controller is called to add the user with alias "userAtBase" to base group with alias "base"
    Then The result is Ok and Json
    When Controller is called to add the user with alias "userAtPrivilege" as CONTRIBUTOR to privilege group with alias "privilege"
    Then The result is Ok and Json
    Given There exists an user with first name "firstname", last name "lastname", password "1 Dummy Password!" and role <role> with alias "user" at common group "common"
    And There is token for user with alias "user" and password "1 Dummy Password!"
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups false
    Then The result is a <httpCodeRange>
    When Controller is called to get all user parts of base group with alias "base" and dissolving sub groups false
    Then The result is a <httpCodeRange>
    When Controller is called to get all user of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups false
    Then The result is a <httpCodeRange>
    When Controller is called to get all user parts of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups false
    Then The result is a <httpCodeRange>
    Examples:
      | role    | httpCodeRange |
    # indirect included: test only the httpCode switch from ok to not ok
      #| ADMIN       | 2xx           |
      #| MANAGER     | 2xx           |
      #| CONTRIBUTOR | 2xx           |
      | VISITOR | 2xx           |
      | BLOCKED | 4xx           |