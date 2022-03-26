Feature: Testing methods of the admin group controller

  Background:
    Given Init admin group features
    And The clientId is "users-test"
    And The clientSecret is "f58497fe32e68138273588d18f62d92366a97bb451d573fb1860ce615e540e15"
    And There is token for user "UAA00001" with password "admin"
    And The admin group with identification "AGAA00001" is known as alias "adminGroup"
    And The admin with identification "UAA00001" is known as alias "admin"

  Scenario: Get admin group
    When Controller is called to get the admin group with the identification of the alias "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "groupName" property at response is "admins"
    And The identification is the same like the one of alias "adminGroup"

  Scenario: Update, get admin group and check changes
    Given The "description" of the admin group with alias "adminGroup" is set to "anythingNew"
    When Controller is called to update the admin group with the identification of the alias "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "adminGroup"
    When Controller is called to get the admin group with the identification of the alias "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "description" property at response is "anythingNew"
    And The identification is the same like the one of alias "adminGroup"
    When Controller is called to get the history of admin group with the identification of the alias "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "subjectIdentification" property at response position 0 is the same like the one of alias "adminGroup"
    And The "changeType" property at response position 0 is "CREATE"
    And The "subjectIdentification" property at response position 1 is the same like the one of alias "adminGroup"
    And The "changeType" property at response position 1 is "ADD"
    And The "targetIdentification" property at response position 1 is the same like the one of alias "admin"
    And The "subjectIdentification" property at response position 2 is the same like the one of alias "adminGroup"
    And The "changeType" property at response position 2 is "MODIFY"
    And The "action" property at response position 2 is "Description: \"null\" -> \"anythingNew\""
    And At response position 3 does not exists

  Scenario: Create an admin
    When The Controller is called to create an admin with first name "New" and last name "Admin" at admin group "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "firstName" property at response is "New"
    And The "lastName" property at response is "Admin"
    And There is any identification at response

  Scenario: Get and count admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "newAdmin" at admin group "adminGroup"
    When Controller is called to get the admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "firstName" property at response is "New"
    And The "lastName" property at response is "Admin"
    And The identification is the same like the one of alias "newAdmin"
    Given There exists an admin with first name "Another" and last name "Admin" with alias "anotherNewAdmin" at admin group "adminGroup"
    When Controller is called to count admins at admin group with alias "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is equal to the number of created admins
    When Controller is called to get all admins from admin group with identification of "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And There are not more entries than the number of created admins
    When Controller is called to get all admin parts from admin group with identification of "adminGroup"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And There are not more entries than the number of created admins

  Scenario: Update, get admin  and check changes
    Given There exists an admin with first name "New" and last name "Admin" with alias "newAdmin" at admin group "adminGroup"
    And The "mail" of the admin with alias "newAdmin" is set to "anythingNew"
    When Controller is called to update the admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "mail" property at response is "anythingNew"
    And The identification is the same like the one of alias "newAdmin"
    When Controller is called to get the admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "mail" property at response is "anythingNew"
    And The identification is the same like the one of alias "newAdmin"
    When Controller is called to get the history of admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The "subjectIdentification" property at response position 0 is the same like the one of alias "newAdmin"
    And The "changeType" property at response position 0 is "CREATE"
    And The "subjectIdentification" property at response position 1 is the same like the one of alias "newAdmin"
    And The "changeType" property at response position 1 is "MODIFY"
    And The "action" property at response position 1 is "Mail: \"null\" -> \"anythingNew\""
    And At response position 2 does not exists

  Scenario: Delete admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "newAdmin" at admin group "adminGroup"
    When Controller is called to delete the admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true
    When Controller is called to get the admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "ERROR"

  Scenario: Set Password of admin
    Given There exists an admin with first name "New" and last name "Admin" with alias "newAdmin" at admin group "adminGroup"
    When Controller is called to set the password "Abcdefg123_!" of admin with the identification of the alias "newAdmin"
    Then The result is Ok and Json
    And The status of the result should be "OK"
    And The response is true