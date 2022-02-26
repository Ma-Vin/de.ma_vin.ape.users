Feature: Testing methods of the user controller

  Background:
    Given The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And There exists a common group with name "Common Group" with alias "common"

  Scenario: Create an user
    When The Controller is called to create an user with first name "New" and last name "User" at common group "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "firstName" property at response is "New"
    And The "lastName" property at response is "User"
    And There is any identification at response

  Scenario: Get and count user
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "firstName" property at response is "New"
    And The "lastName" property at response is "User"
    And The identification is the same like the one of alias "user"
    Given There exists an user with first name "Another" and last name "User" with alias "anotherUser" at common group "common"
    When Controller is called to count users at common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 2
    When Controller is called to get all users from common group with identification of "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user"
    And The identification at 1 is the same like the one of alias "anotherUser"
    And The "identification" property at response position 2 does not exists
    When Controller is called to get all user parts from common group with identification of "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user"
    And The identification at 1 is the same like the one of alias "anotherUser"
    And The "identification" property at response position 2 does not exists

  Scenario: Add, count and remove users at base groups
    Given There exists an user with first name "Direct" and last name "User" with alias "directUser" at common group "common"
    And There exists an user with first name "Indirect" and last name "User" with alias "indirectUser" at common group "common"
    And There exists a base group with name "Base Group" with alias "base" at common group "common"
    And There exists a base group with name "Sub Base Group" with alias "subBase" at common group "common"
    When Controller is called to add the base group with alias "subBase" to base group with alias "base"
    Then The result is Ok and Json
    When Controller is called to add the user with alias "directUser" to base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to add the user with alias "indirectUser" to base group with alias "subBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to count users at base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to count available users for base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to get all available users for base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all available user parts for base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups false
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "directUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all user parts of base group with alias "base" and dissolving sub groups false
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "directUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "directUser"
    And The identification at 1 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 2 does not exists
    When Controller is called to remove the user with alias "directUser" from base group with alias "base"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all user of base group with alias "base" and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 1 does not exists

  Scenario: Add, count and remove users at privilege groups
    Given There exists an user with first name "Direct" and last name "User" with alias "directUser" at common group "common"
    And There exists an user with first name "Indirect" and last name "User" with alias "indirectUser" at common group "common"
    And There exists a privilege group with name "Privilege Group" with alias "privilege" at common group "common"
    And There exists a base group with name "Base Group" with alias "base" at common group "common"
    When Controller is called to add the base group with alias "base" as MANAGER to privilege group with alias "privilege"
    Then The result is Ok and Json
    When Controller is called to add the user with alias "indirectUser" to base group with alias "base"
    Then The result is Ok and Json
    When Controller is called to add the user with alias "directUser" as ADMIN to privilege group with alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all user of privilege group with alias "privilege" with role ADMIN and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "directUser"
    And At response position 1 does not exists
    When Controller is called to get all user of privilege group with alias "privilege" with role MANAGER and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "indirectUser"
    And At response position 1 does not exists
    When Controller is called to count users at privilege group with alias "privilege" with role NOT_RELEVANT
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to count users at privilege group with alias "privilege" with role MANAGER
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 0
    When Controller is called to get all user of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "directUser"
    And The identification of "user" at 1 is the same like the one of alias "indirectUser"
    And At response position 2 does not exists
    When Controller is called to get all user parts of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "directUser"
    And The identification of "user" at 1 is the same like the one of alias "indirectUser"
    And At response position 2 does not exists
    When Controller is called to get all user of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups false
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "directUser"
    And At response position 1 does not exists
    When Controller is called to count available users for privilege group with alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is 1
    When Controller is called to get all available users for privilege group with alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all available user parts for privilege group with alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "indirectUser"
    And The "identification" property at response position 1 does not exists
    When Controller is called to remove the user with alias "directUser" from privilege group with alias "privilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get all user of privilege group with alias "privilege" with role NOT_RELEVANT and dissolving sub groups true
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "indirectUser"
    And At response position 1 does not exists

  Scenario: Update and get user
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    And The "mail" of the user with alias "user" is set to "anythingNew"
    When Controller is called to update the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "mail" property at response is "anythingNew"
    And The identification is the same like the one of alias "user"
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "mail" property at response is "anythingNew"
    And The identification is the same like the one of alias "user"

  Scenario: Delete user
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    When Controller is called to delete the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"

  Scenario: Set Password
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    When Controller is called to set the password "Abcdefg123_!" of user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true

  Scenario: Update role of user at common group
    Given There exists an user with first name "New" and last name "User" with alias "user" at common group "common"
    When Controller is called to set the role ADMIN of user with the identification of the alias "user"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true

  Scenario: Get all users with pages
    Given There exists a base group with name "Parent Base Group Name" with alias "parentBase" at common group "common"
    And There exists a privilege group with name "Parent Privilege Group Name" with alias "parentPrivilege" at common group "common"
    And There are users with first and last name and alias at common group "common"
      | 1. First Name | 1. Last Name | user1 |
      | 2. First Name | 2. Last Name | user2 |
      | 3. First Name | 3. Last Name | user3 |
      | 4. First Name | 4. Last Name | user4 |
      | 5. First Name | 5. Last Name | user5 |
    When Controller is called to get all users at page 0 with size 4 from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user1"
    And The identification at 1 is the same like the one of alias "user2"
    And The identification at 2 is the same like the one of alias "user3"
    And The identification at 3 is the same like the one of alias "user4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all users at page 1 with size 4 from common group with alias "common"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user5"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all available users at page 0 with size 4 for base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user1"
    And The identification at 1 is the same like the one of alias "user2"
    And The identification at 2 is the same like the one of alias "user3"
    And The identification at 3 is the same like the one of alias "user4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all available users at page 1 with size 4 for base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user5"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all available user parts at page 0 with size 4 for base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user1"
    And The identification at 1 is the same like the one of alias "user2"
    And The identification at 2 is the same like the one of alias "user3"
    And The identification at 3 is the same like the one of alias "user4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all available user parts at page 1 with size 4 for base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user5"
    And The "identification" property at response position 1 does not exists
    Given The users are added to base group
      | user1 | parentBase |
      | user2 | parentBase |
      | user3 | parentBase |
      | user4 | parentBase |
      | user5 | parentBase |
    When Controller is called to get all users at page 0 with size 4 from base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user1"
    And The identification at 1 is the same like the one of alias "user2"
    And The identification at 2 is the same like the one of alias "user3"
    And The identification at 3 is the same like the one of alias "user4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all users at page 1 with size 4 from base group with alias "parentBase"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user5"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all available users at page 0 with size 4 for privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user1"
    And The identification at 1 is the same like the one of alias "user2"
    And The identification at 2 is the same like the one of alias "user3"
    And The identification at 3 is the same like the one of alias "user4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all available users at page 1 with size 4 for privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user5"
    And The "identification" property at response position 1 does not exists
    When Controller is called to get all available user parts at page 0 with size 4 for privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user1"
    And The identification at 1 is the same like the one of alias "user2"
    And The identification at 2 is the same like the one of alias "user3"
    And The identification at 3 is the same like the one of alias "user4"
    And The "identification" property at response position 4 does not exists
    When Controller is called to get all available user parts at page 1 with size 4 for privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification at 0 is the same like the one of alias "user5"
    And The "identification" property at response position 1 does not exists
    Given The users are added to privilege group with role
      | user1 | parentPrivilege | ADMIN       |
      | user2 | parentPrivilege | MANAGER     |
      | user3 | parentPrivilege | CONTRIBUTOR |
      | user4 | parentPrivilege | VISITOR     |
      | user5 | parentPrivilege | BLOCKED     |
    When Controller is called to get all users with role NOT_RELEVANT at page 0 with size 4 from privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "user1"
    And The identification of "user" at 1 is the same like the one of alias "user2"
    And The identification of "user" at 2 is the same like the one of alias "user3"
    And The identification of "user" at 3 is the same like the one of alias "user4"
    And At response position 4 does not exists
    When Controller is called to get all users with role NOT_RELEVANT at page 1 with size 4 from privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "user5"
    And At response position 1 does not exists
    When Controller is called to get all users with role CONTRIBUTOR at page 0 with size 4 from privilege group with alias "parentPrivilege"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The identification of "user" at 0 is the same like the one of alias "user3"
    And At response position 1 does not exists