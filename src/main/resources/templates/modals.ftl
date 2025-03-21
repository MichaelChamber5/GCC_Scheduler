<!-- Advanced Search Modal -->
<div id="advancedSearchModal" class="modal">
  <div class="modal-content">
    <span class="close" onclick="closeModal()">&times;</span>
    <h3>Advanced Search Options</h3>
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
