@portal
Feature: ASR-TEK Portal End-to-End Tests

  # ---------------------------------------------------------------------------
  # Authentication
  # ---------------------------------------------------------------------------

  @smoke @auth
  Scenario: Admin can log in successfully
    Given I am logged in as an "Admin" User
   # Then I should be on the "DashboardPage" page

  @auth
  Scenario: Test User can log in successfully
    Given I am logged in as an "Test User" User
    #Then I should be on the "AppUserHomePage" page


  # ---------------------------------------------------------------------------
  # Admin — Dashboard Navigation
  # ---------------------------------------------------------------------------

  @smoke @admin @navigation
  Scenario: Admin navigates to Approvals from Dashboard
    Given I am logged in as an "Admin" User
    When I click on "Approvals" from Dashboard
    #Then I should be on the "ApprovalsPage" page

  @admin @navigation
  Scenario: Admin navigates to Students from Dashboard
    Given I am logged in as an "Admin" User
    #When I click on "Students" from Dashboard
    #Then I should be on the "StudentsPage" page

  @admin @navigation
  Scenario: Admin navigates to Reports from Dashboard
    Given I am logged in as an "Admin" User
    #When I click on "Reports" from Dashboard
    #Then I should be on the "ReportsPage" page

  # ---------------------------------------------------------------------------
  # Admin — Sidebar Navigation
  # ---------------------------------------------------------------------------

  @admin @navigation
  Scenario: Admin navigates to Students via sidebar
    Given I am logged in as an "Admin" User
    When I click on "Students" option in side menu
    #Then I should be on the "StudentsPage" page

  @admin @navigation
  Scenario: Admin navigates to Approvals via sidebar
    Given I am logged in as an "Admin" User
    When I click on "Approvals" option in side menu
    #Then I should be on the "ApprovalsPage" page

  @admin @navigation
  Scenario: Admin navigates to Reports via sidebar
    Given I am logged in as an "Admin" User
    When I click on "Reports" option in side menu
    #Then I should be on the "ReportsPage" page

  @admin @navigation
  Scenario: Admin navigates to Admin Permissions via sidebar
    Given I am logged in as an "Admin" User
    When I click on "Admin Permissions" option in side menu
    #Then I should be on the "AdminPermissionsPage" page

  @admin @navigation
  Scenario: Admin navigates to User Permissions via sidebar
    Given I am logged in as an "Admin" User
    When I click on "User Permissions" option in side menu
    #Then I should be on the "AppUserPermissionsPage" page

  @admin @navigation
  Scenario: Admin navigates to My Profile via sidebar
    Given I am logged in as an "Admin" User
    # When I click on "My Profile" option in side menu
    #Then I should be on the "AdminProfilePage" page

  # ---------------------------------------------------------------------------
  # Admin — Approvals Tab Navigation
  # ---------------------------------------------------------------------------

  @smoke @admin @approvals
  Scenario: Admin views Pending approvals
    Given I am logged in as an "Admin" User
    When I click on "Approvals" option in side menu
    When I click on "Pending" button
    #Then I should be on the "ApprovalsPage" page

  @admin @approvals
  Scenario: Admin views Approved accounts
    Given I am logged in as an "Admin" User
    When I click on "Approvals" option in side menu
    When I click on "Approved" button
    #Then I should be on the "ApprovalsPage" page

  @admin @approvals
  Scenario: Admin views Rejected accounts
    Given I am logged in as an "Admin" User
    When I click on "Approvals" option in side menu
    When I click on "Rejected" button
    #Then I should be on the "ApprovalsPage" page

  # ---------------------------------------------------------------------------
  # Admin — Students
  # ---------------------------------------------------------------------------

  @admin @students
  Scenario: Admin navigates to Add Student page
    Given I am logged in as an "Admin" User
    #When I click on "Students" option in side menu
    #When I click on "+ Add Student" button
    #Then I should be on the "StudentAddPage" page

  @admin @students
  Scenario: Admin searches for a student by name
    Given I am logged in as an "Admin" User
    When I click on "Students" option in side menu
    When I enter "John" into "Search by name" field
    When I click on "Search" button

  # ---------------------------------------------------------------------------
  # Admin — Reports
  # ---------------------------------------------------------------------------

  @admin @reports
  Scenario: Admin views User Activity report
    Given I am logged in as an "Admin" User
    When I click on "Reports" option in side menu
    When I click on "User Activity" tab
    #Then I should be on the "ReportsPage" page

  @admin @reports
  Scenario: Admin views Visitor Activity report
    Given I am logged in as an "Admin" User
    When I click on "Reports" option in side menu
    When I click on "Visitor Activity" tab
    #Then I should be on the "ReportsPage" page

  # ---------------------------------------------------------------------------
  # App User — Navigation
  # ---------------------------------------------------------------------------

  @smoke @appuser @navigation
  Scenario: App User navigates to Employees from Dashboard
    Given I am logged in as an "Test User" User
    When I click on "Employees" from Dashboard
    #Then I should be on the "EmployeesPage" page

  @appuser @navigation
  Scenario: App User navigates to Employees via sidebar
    Given I am logged in as an "Test User" User
    When I click on "Employees" option in side menu
    #Then I should be on the "EmployeesPage" page

  @appuser @navigation
  Scenario: App User navigates to My Profile via sidebar
    Given I am logged in as an "Test User" User
    #When I click on "My Profile" option in side menu
    #Then I should be on the "ProfilePage" page

  @appuser @navigation
  Scenario: App User navigates to Login History via sidebar
    Given I am logged in as an "Test User" User
    When I click on "Login History" option in side menu
    #Then I should be on the "LoginHistoryPage" page

  # ---------------------------------------------------------------------------
  # App User — Employees
  # ---------------------------------------------------------------------------

  @appuser @employees
  Scenario: App User navigates to Add Employee page
    Given I am logged in as an "Test User" User
   # When I click on "Employees" option in side menu
    #When I click on "+ Add Employee" button
    #Then I should be on the "EmployeeAddPage" page

  # ---------------------------------------------------------------------------
  # Session Management (reuse open session)
  # ---------------------------------------------------------------------------

  @session
  Scenario: Admin session is reused across steps
    Given I am logged in as an "Admin" User
    When I click on "Students" option in side menu
    # Second login step should reuse the existing browser session
    Given I am logged in as an "Admin" User
    #Then I should be on the "StudentsPage" page
