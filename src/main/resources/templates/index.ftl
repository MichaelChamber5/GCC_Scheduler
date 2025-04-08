<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Scheduler Layout</title>
<link rel="stylesheet" href="/css/styles.css">
<script type="text/babel" src="/js/scripts.js"></script>
</head>
<body>
  <div class="top-bar">
    <h3>Scheduler Navigation Bar</h3>
  </div>

  <div class="main-container">
    <!-- Sidebar -->
    <div class="sidebar">
      <h4>Search Area</h4>
      <!-- Semester Toggle -->
      <div id="semester-toggle">
        <!-- Buttons now pass full semester strings -->
        <button id="fall-btn" class="semester-btn active" data-semester="2024_Fall">Fall</button>
        <button id="spring-btn" class="semester-btn" data-semester="2025_Spring">Spring</button>
      </div>
      <!-- Search Input -->
      <input type="text" placeholder="Search for classes..." class="search-input"/>
      <!-- Advanced Search Button -->
      <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>
      <!-- Sidebar Container (initially empty) -->
      <div id="sidebar-container"></div>
    </div>

    <!-- Content Area (Calendar, etc.) -->
    <div class="content-area">
      <div class="calendar-view">
        <!-- React calendar container -->
        <div class="calendar-container" id="react-calendar-container">
          <!-- React will render the calendar here -->
        </div>
        <!-- Schedule Table Underneath the Calendar -->
        <div class="calendar-table-wrapper">
          <table id="scheduleDetailsTable" class="calendar-table"></table>
        </div>
      </div>
    </div>
  </div>

  <!-- Advanced Search Modal -->
  <div id="advancedSearchModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeModal()">&times;</span>
      <h3>Advanced Search Options</h3>
      <!-- Filter Inputs -->
      <div>
        <label for="deptInput">Department Code:</label><br>
        <input type="text" id="deptInput" placeholder="e.g. COMP"><br><br>

        <label for="daysInput">Days (e.g., MWF):</label><br>
        <input type="text" id="daysInput" placeholder="Enter days"><br><br>

        <label for="startInput">Start Time (military, e.g. 0930):</label><br>
        <input type="text" id="startInput" placeholder="0930"><br><br>

        <label for="endInput">End Time (military, e.g. 1500):</label><br>
        <input type="text" id="endInput" placeholder="1500"><br><br>

        <button id="applyFiltersBtn">Apply Filters</button>
      </div>
    </div>
  </div>

  <!-- Error Modal -->
  <div id="errorModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeErrorModal()">&times;</span>
      <p id="errorModalMessage"></p>
    </div>
  </div>

  <!-- Course Info Modal -->
  <div id="courseInfoModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeCourseInfoModal()">&times;</span>
      <div id="courseInfoModalContent"></div>
    </div>
  </div>
</body>
</html>
