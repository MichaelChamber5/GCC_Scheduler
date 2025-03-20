<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <title>Scheduler Layout</title>
  <style>
    /* Basic reset and font styling for the entire page */
    html, body {
margin: 0;
padding: 0;
height: 100vh;
overflow: hidden;
font-family: Arial, sans-serif;
}
    /* Top navigation bar styling */
.top-bar {
height: 10vh;
background-color: #333;
color: white;
display: flex;
align-items: center;
padding: 0 20px;
box-sizing: border-box;
}
/* Main container holds the sidebar and content area */
.main-container {
display: flex;
height: 90vh;
}
/* Sidebar styling for search area and controls */
.sidebar {
width: 20%;
background-color: #f0f0f0;
padding: 20px;
box-sizing: border-box;
display: flex;
flex-direction: column;
}
/* Semester toggle area */
#semester-toggle {
margin-bottom: 10px;
}
/* Style for semester buttons */
.semester-btn {
padding: 8px 12px;
margin-right: 5px;
border: 1px solid #ccc;
background-color: #eee;
cursor: pointer;
}
.semester-btn.active {
background-color: #4CAF50;
color: white;
border-color: #4CAF50;
}
/* Styling for search input */
.search-input {
width: 100%;
padding: 8px;
margin-bottom: 10px;
box-sizing: border-box;
font-size: 14px;
}
/* Advanced search button styling */
.advanced-search-btn {
width: 100%;
padding: 8px;
background-color: #4CAF50;
color: white;
border: none;
font-size: 14px;
cursor: pointer;
margin-bottom: 15px;
}
.advanced-search-btn:hover {
background-color: #45a049;
}
/* Container for search results in the sidebar */
#sidebar-container {
flex-grow: 1;
background-color: #ffffff;
border: 1px solid #ccc;
padding: 10px;
box-sizing: border-box;
overflow-y: auto;
}
/* Content area holds the calendar and schedule table */
.content-area {
width: 80%;
display: flex;
flex-direction: column;
padding: 0;
margin: 0;
box-sizing: border-box;
}
/* Calendar view styling */
.calendar-view {
flex: 2;
background-color: #e0eaff;
padding: 10px;
box-sizing: border-box;
position: relative;
}
/* Schedule table container styling */
.calendar-table-wrapper {
position: fixed;
bottom: 0;
left: 20%;
width: 80%;
height: 30vh;
background-color: #ffffff;
overflow-y: auto;
z-index: 100;
}
/* Schedule table styling */
.calendar-table {
width: 100%;
height: 100%;
border-collapse: collapse;
table-layout: fixed;
}
.calendar-table th, .calendar-table td {
border: 1px solid #ccc;
padding: 4px;
font-size: 12px;
text-align: center;
}
.calendar-table th {
background-color: #eee;
}
h4 {
margin: 0 0 10px 0;
}
/* Modal styles used for advanced search, error, and course info */
.modal {
display: none;
position: fixed;
z-index: 1000;
left: 0;
top: 0;
width: 100vw;
height: 100vh;
background-color: rgba(0,0,0,0.5);
}
.modal-content {
background-color: #fefefe;
margin: 10% auto;
padding: 20px;
border-radius: 8px;
width: 40%;
box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}
.close {
float: right;
font-size: 20px;
font-weight: bold;
cursor: pointer;
}
/* React calendar component container styling */
.calendar-container {
max-width: 1200px;
margin: 0 auto;
font-family: Arial, sans-serif;
border: 1px solid #999;
height: 57vh;
overflow-y: auto;
background-color: #fff;
}
.calendar-grid {
display: flex;
border-top: 2px solid #333;
border-left: 2px solid #333;
}
.day-column {
flex: 1;
border-right: 2px solid #333;
border-bottom: 2px solid #333;
display: flex;
flex-direction: column;
position: relative;
}
.day-header {
background: #f0f0f0;
padding: 8px;
text-align: center;
font-weight: bold;
border-bottom: 2px solid #333;
}
.time-slots {
flex: 1;
display: flex;
flex-direction: column;
position: relative;
}
.time-slot {
border-bottom: 1px solid #ccc;
padding: 4px;
font-size: 12px;
position: relative;
height: 40px;
}
.slot-label {
position: relative;
z-index: 2;
}
/* Event overlay styling on calendar slots */
.event-overlay {
position: absolute;
top: 0;
left: 0;
right: 0;
bottom: 0;
background: rgba(0, 102, 204, 0.8);
z-index: 1;
display: flex;
align-items: center;
justify-content: center;
font-size: 10px;
color: #333;
}
.remove-button {
margin-left: 8px;
background: transparent;
border: none;
color: #333;
font-weight: bold;
cursor: pointer;
pointer-events: auto;
}
/* Button style for showing course details */
.course-info-btn {
margin-left: 8px;
padding: 4px 8px;
background-color: #2196F3;
color: white;
border: none;
font-size: 12px;
cursor: pointer;
}
</style>

<!-- Load React, ReactDOM, Babel, and Moment.js libraries -->
<script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
  <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
  <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script crossorigin src="https://unpkg.com/moment/min/moment.min.js"></script>
</head>
<body>
  <!-- Top navigation bar -->
  <div class="top-bar">
    <h3>Scheduler Navigation Bar</h3>
  </div>

  <div class="main-container">
    <!-- Sidebar with search area and semester toggle -->
    <div class="sidebar">
      <h4>Search Area</h4>
      <!-- Semester toggle buttons -->
      <div id="semester-toggle">
        <button id="fall-btn" class="semester-btn active" data-semester="2024_Fall">Fall</button>
        <button id="spring-btn" class="semester-btn" data-semester="2025_Spring">Spring</button>
      </div>
      <!-- Main search input -->
      <input type="text" placeholder="Search for classes..." class="search-input"/>
      <!-- Button to open advanced search modal -->
      <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>
      <!-- Container where search results will be displayed -->
      <div id="sidebar-container"></div>
    </div>

    <!-- Main content area containing the calendar and schedule table -->
    <div class="content-area">
      <div class="calendar-view">
        <!-- Container where the React calendar component will be rendered -->
        <div class="calendar-container" id="react-calendar-container">
          <!-- React renders the calendar here -->
        </div>
        <!-- Schedule table displayed below the calendar -->
        <div class="calendar-table-wrapper">
          <table id="scheduleDetailsTable" class="calendar-table"></table>
        </div>
      </div>
    </div>
  </div>

  <!-- Advanced Search Modal (hidden by default) -->
  <div id="advancedSearchModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeModal()">&times;</span>
      <h3>Advanced Search Options</h3>
      <div>
        <!-- Input for department code -->
        <label for="deptInput">Department Code:</label><br>
        <input type="text" id="deptInput" placeholder="e.g. COMP"><br><br>
        <!-- Input for days filter -->
        <label for="daysInput">Days (e.g., MWF):</label><br>
        <input type="text" id="daysInput" placeholder="Enter days"><br><br>
        <!-- Input for start time -->
        <label for="startInput">Start Time (military, e.g. 0930):</label><br>
        <input type="text" id="startInput" placeholder="0930"><br><br>
        <!-- Input for end time -->
        <label for="endInput">End Time (military, e.g. 1500):</label><br>
        <input type="text" id="endInput" placeholder="1500"><br><br>
        <button id="applyFiltersBtn">Apply Filters</button>
      </div>
    </div>
  </div>

  <!-- Error Modal (displays error messages) -->
  <div id="errorModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeErrorModal()">&times;</span>
      <p id="errorModalMessage"></p>
    </div>
  </div>

  <!-- Course Info Modal (displays course details) -->
  <div id="courseInfoModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeCourseInfoModal()">&times;</span>
      <div id="courseInfoModalContent"></div>
    </div>
  </div>

  <script>
    // Wait until the DOM is fully loaded
    document.addEventListener("DOMContentLoaded", function() {
console.log("DOM fully loaded. Attaching event listeners.");

// Set the default semester from the Fall button and store it in the session
var currentSemester = document.querySelector("#fall-btn").dataset.semester;
window.currentSemester = currentSemester;

// Attach click listeners to each semester button to update the current semester
document.querySelectorAll(".semester-btn").forEach(function(btn) {
btn.onclick = function() {
document.querySelectorAll(".semester-btn").forEach(function(b) {
b.classList.remove("active");
});
          btn.classList.add("active");
          window.currentSemester = btn.dataset.semester;
          console.log("Semester changed to:", window.currentSemester);
          performSearch();
          window.refreshCalendar?.();
          updateScheduleTable();
        };
      });

      // Function to perform a search using the query and advanced filters
      window.performSearch = function() {
var query = document.querySelector(".search-input").value.trim();
if (!query) {
document.getElementById("sidebar-container").innerHTML = "";
return;
}
        console.log("Performing search with query:", query, "and semester:", window.currentSemester);

        // Build search URL with basic parameters
        var url = "/search?q=" + encodeURIComponent(query) +
                  "&semester=" + encodeURIComponent(window.currentSemester);

        // Append any advanced filter parameters if provided
        var dept = document.getElementById("deptInput") ? document.getElementById("deptInput").value.trim() : "";
        if (dept !== "") {
url += "&dept=" + encodeURIComponent(dept);
}
        var days = document.getElementById("daysInput") ? document.getElementById("daysInput").value.trim() : "";
        if (days !== "") {
url += "&days=" + encodeURIComponent(days);
}
        var start = document.getElementById("startInput") ? document.getElementById("startInput").value.trim() : "";
        if (start !== "") {
url += "&start=" + encodeURIComponent(start);
}
        var end = document.getElementById("endInput") ? document.getElementById("endInput").value.trim() : "";
        if (end !== "") {
url += "&end=" + encodeURIComponent(end);
}

        // Execute the search and update the sidebar with results
        fetch(url)
.then(function(response) {
console.log("Search response status:", response.status);
if (!response.ok) {
throw new Error("Search request failed");
}
return response.json();
})
.then(function(courses) {
console.log("Search results received:", courses);
var html = "";
            if (courses.length === 0) {
html = "<p>No courses found.</p>";
} else {
courses.forEach(function(course) {
var buttonText = course.onSchedule ? "Remove" : "Add";
var dataAdded  = course.onSchedule ? "true" : "false";
html += "<div class='sidebar-course-item'>";
html += "<span>" + course.name + " (" + course.courseNumber + ")</span>";
html += "<button class='course-action-btn' data-course-id='" + course.id + "' data-added='" + dataAdded + "'>" + buttonText + "</button>";
html += "<button class='course-info-btn' data-course-info='" + encodeURIComponent(JSON.stringify(course)) + "'>Info</button>";
html += "</div><hr/>";
});
            }
            document.getElementById("sidebar-container").innerHTML = html;
            attachCourseActionButtons();
            attachCourseInfoButtons();
          })
.catch(function(err) {
console.error("Error during search:", err);
});
};

// Trigger search when the Enter key is pressed in the search input field
document.querySelector(".search-input").addEventListener("keydown", function(e) {
if (e.key === "Enter") {
performSearch();
}
      });

      // Attach click listeners to course action buttons (Add/Remove)
      function attachCourseActionButtons() {
document.querySelectorAll(".course-action-btn").forEach(function(btn) {
btn.onclick = function() {
var courseId = btn.dataset.courseId;
var isAdded  = (btn.dataset.added === "true");
if (isAdded) {
removeCourse(courseId, btn);
} else {
addCourse(courseId, btn);
}
          };
        });
      }

      // Attach click listeners to course info buttons to show course details in a modal
      function attachCourseInfoButtons() {
document.querySelectorAll(".course-info-btn").forEach(function(btn) {
btn.onclick = function() {
var courseInfoStr = btn.dataset.courseInfo;
var course = JSON.parse(decodeURIComponent(courseInfoStr));
var infoHtml = "<h3>" + course.name + " (" + course.courseNumber + ")</h3>";
infoHtml += "<p><strong>Credits:</strong> " + course.credits + "</p>";
infoHtml += "<p><strong>Location:</strong> " + course.location + "</p>";
infoHtml += "<p><strong>Section:</strong> " + course.section + "</p>";
infoHtml += "<p><strong>Description:</strong> " + (course.description ? course.description : "No description available") + "</p>";
if (course.professors && course.professors.length > 0) {
infoHtml += "<p><strong>Professor(s):</strong> " + course.professors.map(function(prof) { return prof.name; }).join(", ") + "</p>";
            } else {
infoHtml += "<p><strong>Professor(s):</strong> None</p>";
}
            document.getElementById("courseInfoModalContent").innerHTML = infoHtml;
            document.getElementById("courseInfoModal").style.display = "block";
          };
        });
      }

      // Function to add a course via an API call
      function addCourse(courseId, btn) {
var params = new URLSearchParams();
params.append("courseId", courseId);
console.log("Adding course:", courseId);
fetch("/add-course", {
method: "POST",
body: params
})
.then(function(response) {
if (!response.ok) {
return response.json().then(function(err) { throw err; });
}
return response.json();
})
.then(function(result) {
console.log("Course added:", result);
btn.textContent = "Remove";
            btn.dataset.added = "true";
            window.refreshCalendar?.();
            updateScheduleTable();
          })
.catch(function(err) {
console.error("Error adding course:", err);
showErrorModal(err.error || "An error occurred while adding the course.");
});
}

// Function to remove a course via an API call
function removeCourse(courseId, btn) {
var params = new URLSearchParams();
        params.append("scheduleItemId", courseId);
        console.log("Removing course:", courseId);
        fetch("/remove-course", {
method: "POST",
body: params
})
.then(function(r) { return r.text(); })
.then(function(result) {
console.log("Course removed:", result);
btn.textContent = "Add";
            btn.dataset.added = "false";
            window.refreshCalendar?.();
            updateScheduleTable();
          })
.catch(function(err) {
console.error("Error removing course:", err);
});
}

// Function to remove a course from the calendar overlay (global removal)
window.removeCourseGlobal = function(scheduleItemId) {
var params = new URLSearchParams();
params.append("scheduleItemId", scheduleItemId);
console.log("Global remove request for course:", scheduleItemId);
fetch("/remove-course", {
method: "POST",
body: params
})
.then(function(r) { return r.text(); })
.then(function(result) {
console.log("Global remove result:", result);
window.refreshCalendar?.();
performSearch();
updateScheduleTable();
})
.catch(function(err) {
console.error("Error removing course globally:", err);
});
};

// Functions to open and close the advanced search modal
window.openModal = function() {
document.getElementById("advancedSearchModal").style.display = "block";
};
      window.closeModal = function() {
document.getElementById("advancedSearchModal").style.display = "none";
};

      // Attach the "Apply Filters" button to close the modal and perform the search
      document.getElementById("applyFiltersBtn").addEventListener("click", function() {
closeModal();
performSearch();
});

      // Functions to show and close the error modal
      function showErrorModal(message) {
document.getElementById("errorModalMessage").textContent = message;
document.getElementById("errorModal").style.display = "block";
}
      function closeErrorModal() {
document.getElementById("errorModal").style.display = "none";
}
      window.closeErrorModal = closeErrorModal;

      // Function to close the course info modal
      window.closeCourseInfoModal = function() {
document.getElementById("courseInfoModal").style.display = "none";
};

      // Close the advanced search modal if a click is detected outside of it
      window.onclick = function(e) {
var modal = document.getElementById("advancedSearchModal");
if (e.target === modal) {
modal.style.display = "none";
}
      };

      // Update the schedule table by fetching the current schedule via the API
      function updateScheduleTable() {
fetch('/api/schedule')
.then(function(response) {
if (!response.ok) throw new Error("Failed to fetch schedule");
return response.json();
})
.then(function(data) {
var html = "<thead><tr>";
            html += "<th>Course Name</th>";
            html += "<th>Professor(s)</th>";
            html += "<th>Location</th>";
            html += "<th>Credits</th>";
            html += "<th>Course Code</th>";
            html += "<th>Section</th>";
            html += "<th>Description</th>";
            html += "</tr></thead>";
            html += "<tbody>";
            data.forEach(function(course) {
var profs = (course.professors && course.professors.length > 0)
? course.professors.map(function(p) { return p.name; }).join(", ")
                : "None";
              html += "<tr>";
              html += "<td>" + course.name + "</td>";
              html += "<td>" + profs + "</td>";
              html += "<td>" + course.location + "</td>";
              html += "<td>" + course.credits + "</td>";
              html += "<td>" + course.courseNumber + "</td>";
              html += "<td>" + course.section + "</td>";
              html += "<td>" + (course.description || "") + "</td>";
              html += "</tr>";
            });
            html += "</tbody>";
            document.getElementById("scheduleDetailsTable").innerHTML = html;
          })
.catch(function(err) {
console.error("Error fetching schedule for table:", err);
});
}

// Perform the initial search and update the schedule table when the page loads
performSearch();
updateScheduleTable();
});
</script>

<!-- React-based Calendar Component -->
<script type="text/babel">
    window.refreshCalendar = null;
    // Map day letters to indices for the calendar
    const dayLetterToIndex = { "M": 0, "T": 1, "W": 2, "R": 3, "F": 4 };

    // Get the current week's Monday as the starting point
    function getCurrentMonday() {
return moment().startOf('week').add(1, 'days');
}

    // Convert a course's meeting times into calendar events
    function mapScheduleItemToEvents(item) {
const events = [];
Object.entries(item.meetingTimes).forEach(([dayLetter, times]) => {
const monday = getCurrentMonday();
const dayIndex = dayLetterToIndex[dayLetter];
const eventDate = monday.clone().add(dayIndex, 'days');
const startNum = times[0];
const endNum = times[1];
const startHour = Math.floor(startNum / 100);
const startMinute = startNum % 100;
const endHour = Math.floor(endNum / 100);
const endMinute = endNum % 100;
const start = eventDate.clone().hour(startHour).minute(startMinute);
const end = eventDate.clone().hour(endHour).minute(endMinute);
events.push({
title: item.name,
start: start.toDate(),
end: end.toDate(),
scheduleItemId: item.id
});
      });
      return events;
    }

    // Main Calendar component
    function Calendar() {
const [events, setEvents] = React.useState([]);
const fetchSchedule = () => {
fetch('/api/schedule')
.then(response => {
if (!response.ok) throw new Error("Failed to fetch schedule");
return response.json();
})
.then(data => {
// Filter courses based on the current semester
const filteredData = data.filter(item =>
item.semester && item.semester.toLowerCase().endsWith(window.currentSemester.toLowerCase())
);
const mapped = filteredData.flatMap(mapScheduleItemToEvents);
setEvents(mapped);
})
.catch(error => console.error("Error fetching schedule:", error));
      };

      React.useEffect(() => {
fetchSchedule();
}, []);

      React.useEffect(() => {
window.refreshCalendar = fetchSchedule;
return () => { window.refreshCalendar = null; };
      }, []);

      const monday = getCurrentMonday();
      const days = [];
      // Build an array of days for the current week (Monday to Friday)
      for (let i = 0; i < 5; i++) {
days.push(monday.clone().add(i, 'days').toDate());
}

      // Determine time slot increment: 90 minutes for Tuesday/Thursday, 60 for others.
      const getStepMinutes = (day) => {
const dayAbbrev = moment(day).format('ddd');
return (dayAbbrev === 'Tue' || dayAbbrev === 'Thu') ? 90 : 60;
};

      return (
        <div className="calendar-grid">
          {days.map((day, index) => {
const dayEvents = events.filter(event =>
moment(event.start).isSame(day, 'day')
);
return (
<DayColumn
key={index}
                day={day}
                startHour={8}
                endHour={21}
                stepMinutes={getStepMinutes(day)}
                events={dayEvents}
              />
            );
          })}
        </div>
      );
    }

    // Component for each day in the calendar
    function DayColumn({ day, startHour, endHour, stepMinutes, events }) {
const dayLabel = moment(day).format('dddd, MMM Do');
const slots = [];
let currentSlot = moment(day).hour(startHour).minute(0);
const end = moment(day).hour(endHour).minute(0);

// Create regular time slots until the end hour
while (currentSlot.isBefore(end)) {
slots.push({ time: currentSlot.clone(), type: 'regular', step: stepMinutes });
        currentSlot.add(stepMinutes, 'minutes');
      }
      // Add one extra slot for events starting at or after the end hour (night slot)
      const nightSlot = moment(day).hour(endHour).minute(0);
      slots.push({ time: nightSlot, type: 'night' });

      // Check if an event should be displayed in a given slot
      const eventOverlapsSlot = (event, slot) => {
if (slot.type === 'night') {
// Include any event that starts at or after the end hour.
return moment(event.start).isSameOrAfter(slot.time);
} else {
const slotEnd = slot.time.clone().add(slot.step, 'minutes');
return moment(event.start).isBefore(slotEnd) && moment(event.end).isAfter(slot.time);
}
      };

      return (
        <div className="day-column">
          <div className="day-header">{dayLabel}</div>
          <div className="time-slots">
            {slots.map((slot, index) => {
// Use the same time format for all slots (e.g., "9:00 AM")
const label = slot.time.format('h:mm A');
const overlappingEvents = events.filter(event =>
eventOverlapsSlot(event, slot)
);
return (
<div key={index} className="time-slot">
                  <div className="slot-label">{label}</div>
                  {overlappingEvents.map((event, idx) => (
<div key={idx} className="event-overlay">
                      {event.title}
                      <button
                        className="remove-button"
                        onClick={(e) => {
e.stopPropagation();
removeCourseGlobal(event.scheduleItemId);
}}
                      >
                        x
                      </button>
                    </div>
                  ))}
                </div>
              );
            })}
          </div>
        </div>
      );
    }

    // Helper function to remove a course from the calendar
    function removeCourse(scheduleItemId) {
window.removeCourseGlobal(scheduleItemId);
}

    // Render the Calendar component into the calendar container
    ReactDOM.render(<Calendar />, document.getElementById('react-calendar-container'));
  </script>
</body>
</html>
