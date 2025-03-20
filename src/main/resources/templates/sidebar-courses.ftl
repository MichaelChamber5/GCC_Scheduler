<link rel="stylesheet" href="/styles/calendar.css">

<!-- Sidebar Search Area Container -->
<div id="sidebar-search-area" style="padding: 10px;">
  <!-- Semester Toggle -->
  <div id="semester-toggle" style="margin-bottom: 10px;">
    <button class="semester-toggle-btn active" data-semester="Fall" style="font-size:10px; padding:5px;">Fall</button>
    <button class="semester-toggle-btn" data-semester="Spring" style="font-size:10px; padding:5px;">Spring</button>
  </div>

  <!-- Search Bar and Advanced Search Button -->
  <div id="search-bar-container" style="margin-bottom: 10px;">
    <input type="text" id="course-search-input" placeholder="Search for classes..." style="width:100%; padding:8px; font-size:10px;"/>
    <button id="advanced-search-btn" style="width:100%; padding:8px; font-size:10px; margin-top:5px;" onclick="openAdvancedSearch()">Advanced Search</button>
  </div>
</div>

<!-- Advanced Search Modal -->
<div id="advancedSearchModal" class="modal">
  <div class="modal-content">
    <span class="close" onclick="closeAdvancedSearch()">&times;</span>
    <h3 style="font-size:14px;">Advanced Search Options</h3>
    <!-- Advanced search inputs can go here -->
    <button id="advanced-search-submit" style="padding:5px; font-size:10px;" onclick="submitAdvancedSearch()">Search</button>
  </div>
</div>

<!-- New Container for Course List -->
<div id="course-list-container" style="padding: 10px; border-top:1px solid #ccc;">
  <div id="course-list" style="max-height: 500px; overflow-y: auto; font-size:10px; padding-top:5px;">
    Loading courses...
  </div>
</div>

<#noparse>
<script>
  // Advanced Search Modal functions
  function openAdvancedSearch() {
    document.getElementById("advancedSearchModal").style.display = "block";
  }
  function closeAdvancedSearch() {
    document.getElementById("advancedSearchModal").style.display = "none";
  }
  function submitAdvancedSearch() {
    // Implement advanced search logic if needed; for now, just close modal
    closeAdvancedSearch();
    loadCourses(); // Optionally trigger a search based on advanced criteria
  }

  // Semester Toggle Logic
  document.querySelectorAll(".semester-toggle-btn").forEach(btn => {
    btn.addEventListener("click", function() {
      document.querySelectorAll(".semester-toggle-btn").forEach(b => b.classList.remove("active"));
      btn.classList.add("active");
      // Optionally, filter courses by semester here
      loadCourses();
    });
  });

  // Function to load courses from /api/courses and render the course list
  function loadCourses() {
    fetch("/api/courses")
      .then(response => {
        if (!response.ok) throw new Error("Failed to fetch courses");
        return response.json();
      })
      .then(courses => {
        let html = "";
        courses.forEach(course => {
          // Build a course element:
          // Display department code, course number, and course name.
          html += `<div class="course-element" data-course-id="${course.id}" style="border-bottom:1px solid #ccc; padding: 5px;">`;
          html += `<div class="course-summary" style="display: flex; justify-content: space-between; align-items: center;">`;
          html += `<div class="course-info">${course.depCode ? course.depCode + " " : ""}${course.courseNumber || "N/A"} - ${course.name || "N/A"}</div>`;
          html += `<div class="course-actions">`;
          // Dropdown button with down-chevron (&#9660;) that toggles details
          html += `<button class="dropdown-btn" style="font-size:10px;" onclick="toggleCourseDetails(this)">&#9660;</button> `;
          // Add/Remove button starts with +
          html += `<button class="add-remove-btn" data-added="false" style="font-size:10px;" onclick="toggleAddRemove(this, ${course.id})">+</button>`;
          html += `</div>`;
          html += `</div>`;
          // Hidden details section for more information
          html += `<div class="course-details" style="display:none; margin-top:5px;">`;
          html += `<div>Professors: ${course.professors ? course.professors.map(p => p.name).join(", ") : "N/A"}</div>`;
          html += `<div>Location: ${course.location || "N/A"}</div>`;
          html += `</div>`;
          html += `</div>`;
        });
        document.getElementById("course-list").innerHTML = html;
      })
      .catch(err => {
        console.error("Error loading courses:", err);
        document.getElementById("course-list").innerHTML = "Error loading courses.";
      });
  }

  // Toggle course details dropdown
  function toggleCourseDetails(btn) {
    const courseElement = btn.closest(".course-element");
    const details = courseElement.querySelector(".course-details");
    if(details.style.display === "none") {
      details.style.display = "block";
      btn.innerHTML = "&#9650;"; // up chevron
    } else {
      details.style.display = "none";
      btn.innerHTML = "&#9660;"; // down chevron
    }
  }

  // Toggle add/remove button and call the appropriate endpoint
  function toggleAddRemove(btn, courseId) {
    const added = btn.getAttribute("data-added") === "true";
    if (!added) {
      // Call add-course endpoint
      const params = new URLSearchParams();
      params.append("courseId", courseId);
      fetch("/add-course", { method: "POST", body: params })
      .then(response => response.text())
      .then(result => {
        btn.textContent = "-";
        btn.setAttribute("data-added", "true");
        // Optionally refresh other parts (like grid and table) if needed:
        if (window.refreshCalendar) { window.refreshCalendar(); }
        if (window.refreshUserCourses) { window.refreshUserCourses(); }
      })
      .catch(err => console.error("Error adding course:", err));
    } else {
      // Call remove-course endpoint
      const params = new URLSearchParams();
      params.append("scheduleItemId", courseId);
      fetch("/remove-course", { method: "POST", body: params })
      .then(response => response.text())
      .then(result => {
        btn.textContent = "+";
        btn.setAttribute("data-added", "false");
        if (window.refreshCalendar) { window.refreshCalendar(); }
        if (window.refreshUserCourses) { window.refreshUserCourses(); }
      })
      .catch(err => console.error("Error removing course:", err));
    }
  }

  // Initially load courses when the sidebar loads
  loadCourses();
</script>
</#noparse>
