<#-- File: calendar.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Scheduler Layout</title>
  <style>
    html, body {
margin: 0;
padding: 0;
height: 100vh;
overflow: hidden;
font-family: Arial, sans-serif;
}
.top-bar {
height: 10vh;
background-color: #333;
color: white;
display: flex;
align-items: center;
padding: 0 20px;
box-sizing: border-box;
}
.main-container {
display: flex;
height: 90vh;
}
.sidebar {
width: 20%;
background-color: #f0f0f0;
padding: 20px;
box-sizing: border-box;
display: flex;
flex-direction: column;
}
/* Semester toggle styles */
#semester-toggle {
margin-bottom: 10px;
}
/* Buttons now pass full semester strings */
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
.search-input {
width: 100%;
padding: 8px;
margin-bottom: 10px;
box-sizing: border-box;
font-size: 14px;
}
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
/* Sidebar container for search results - initially empty */
#sidebar-container {
flex-grow: 1;
background-color: #ffffff;
border: 1px solid #ccc;
padding: 10px;
box-sizing: border-box;
overflow-y: auto;
}
.content-area {
width: 80%;
display: flex;
flex-direction: column;
padding: 0;
margin: 0;
box-sizing: border-box;
}
.calendar-view {
flex: 2;
background-color: #e0eaff;
padding: 10px;
box-sizing: border-box;
position: relative;
}
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
.calendar-table {
width: 100%;
height: 100%;
border-collapse: collapse;
table-layout: fixed;
}
.calendar-table tr {
height: calc(100% / 7);
}
.calendar-table td {
border: 1px solid #ccc;
padding: 4px;
font-size: 12px;
text-align: center;
}
h4 {
margin: 0 0 10px 0;
}
/* Modal Styles */
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
/* React calendar styles */
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
.event-overlay {
position: absolute;
top: 0;
left: 0;
right: 0;
bottom: 0;
background: rgba(135, 206, 250, 0.5);
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
</style>

<!-- React, ReactDOM, Babel, and Moment.js -->
<script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
  <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
  <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
  <script crossorigin src="https://unpkg.com/moment/min/moment.min.js"></script>
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
        <!-- Fixed table at bottom -->
        <div class="calendar-table-wrapper">
          <table class="calendar-table">
            <tbody>
              <#list 1..7 as row>
                <tr>
                  <#list 1..6 as col>
                    <td>Row ${row}, Col ${col}</td>
                  </#list>
                </tr>
              </#list>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <!-- Advanced Search Modal -->
  <div id="advancedSearchModal" class="modal">
    <div class="modal-content">
      <span class="close" onclick="closeModal()">&times;</span>
      <h3>Advanced Search Options</h3>
      <!-- Add your advanced filter inputs here (dept code, days, start/end time, etc.) -->
    </div>
  </div>

  <script>
    document.addEventListener("DOMContentLoaded", function() {
console.log("DOM fully loaded. Attaching event listeners.");

// Global variable to track current semester (default is from the Fall button)
var currentSemester = document.querySelector("#fall-btn").dataset.semester;
window.currentSemester = currentSemester;

// Attach click listeners to semester toggle buttons
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
        };
      });

      // Perform search and update sidebar
      window.performSearch = function() {
var query = document.querySelector(".search-input").value.trim();
if (!query) {
document.getElementById("sidebar-container").innerHTML = "";
return;
}
        console.log("Performing search with query:", query, "and semester:", window.currentSemester);

        // Instead of encodeURIComponent in the FTL, do it in JS
        var url = "/search?q=" + encodeURIComponent(query) +
                  "&semester=" + encodeURIComponent(window.currentSemester);

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
html += "</div><hr/>";
});
            }
            document.getElementById("sidebar-container").innerHTML = html;
            attachCourseActionButtons();
          })
.catch(function(err) {
console.error("Error during search:", err);
});
};

// Trigger search on Enter key
document.querySelector(".search-input").addEventListener("keydown", function(e) {
if (e.key === "Enter") {
performSearch();
}
      });

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

      function addCourse(courseId, btn) {
var params = new URLSearchParams();
params.append("courseId", courseId);
console.log("Adding course:", courseId);
fetch("/add-course", {
method: "POST",
body: params
})
.then(function(r) { return r.text(); })
.then(function(result) {
console.log("Course added:", result);
btn.textContent = "Remove";
            btn.dataset.added = "true";
            window.refreshCalendar?.();
          })
.catch(function(err) {
console.error("Error adding course:", err);
});
}

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
          })
.catch(function(err) {
console.error("Error removing course:", err);
});
}

// Removing from the calendar overlay
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
// re-run search to flip the button back if it was on "Remove"
performSearch();
})
.catch(function(err) {
console.error("Error removing course globally:", err);
});
};

// Modal open/close
window.openModal = function() {
document.getElementById("advancedSearchModal").style.display = "block";
};
      window.closeModal = function() {
document.getElementById("advancedSearchModal").style.display = "none";
};
      window.onclick = function(e) {
var modal = document.getElementById("advancedSearchModal");
if (e.target === modal) {
modal.style.display = "none";
}
      };

      // Initial search
      performSearch();
    });
  </script>

  <!-- React-based Calendar Component -->
  <script type="text/babel">
    window.refreshCalendar = null;

    const dayLetterToIndex = { "M":0, "T":1, "W":2, "R":3, "F":4 };

    function getCurrentMonday() {
return moment().startOf('week').add(1, 'days');
}

    function mapScheduleItemToEvents(item) {
const events = [];
Object.entries(item.meetingTimes).forEach(([dayLetter, times]) => {
const monday = getCurrentMonday();
const dayIndex = dayLetterToIndex[dayLetter];
const eventDate = monday.clone().add(dayIndex, 'days');

const startNum = times[0];
const endNum   = times[1];
const startHour   = Math.floor(startNum / 100);
const startMinute = startNum % 100;
const endHour     = Math.floor(endNum / 100);
const endMinute   = endNum % 100;

const start = eventDate.clone().hour(startHour).minute(startMinute);
const end   = eventDate.clone().hour(endHour).minute(endMinute);

events.push({
title: item.name,
start: start.toDate(),
end: end.toDate(),
scheduleItemId: item.id
});
      });
      return events;
    }

    function Calendar() {
const [events, setEvents] = React.useState([]);

const fetchSchedule = () => {
fetch('/api/schedule')
.then(response => {
if (!response.ok) throw new Error("Failed to fetch schedule");
return response.json();
})
.then(data => {
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
      for (let i = 0; i < 5; i++) {
days.push(monday.clone().add(i, 'days').toDate());
}

      const getStepMinutes = (day) => {
const dayAbbrev = moment(day).format('ddd');
return (dayAbbrev === 'Tue' || dayAbbrev === 'Thu') ? 75 : 60;
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
                endHour={18}
                stepMinutes={getStepMinutes(day)}
                events={dayEvents}
              />
            );
          })}
        </div>
      );
    }

    function DayColumn({ day, startHour, endHour, stepMinutes, events }) {
const dayLabel = moment(day).format('dddd, MMM Do');
const slots = [];
let currentSlot = moment(day).hour(startHour).minute(0);
const end = moment(day).hour(endHour).minute(0);

while (currentSlot.isBefore(end)) {
slots.push(currentSlot.clone());
currentSlot.add(stepMinutes, 'minutes');
}

      const eventOverlapsSlot = (event, slotStart, slotEnd) => {
return moment(event.start).isBefore(slotEnd) && moment(event.end).isAfter(slotStart);
};

      return (
        <div className="day-column">
          <div className="day-header">{dayLabel}</div>
          <div className="time-slots">
            {slots.map((slot, index) => {
const slotEnd = slot.clone().add(stepMinutes, 'minutes');
const overlappingEvents = events.filter(event =>
eventOverlapsSlot(event, slot, slotEnd)
);
return (
<div key={index} className="time-slot">
                  <div className="slot-label">{slot.format('h:mm A')}</div>
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

    function removeCourse(scheduleItemId) {
// Not used by DayColumn's remove button; that calls removeCourseGlobal directly.
window.removeCourseGlobal(scheduleItemId);
}

    ReactDOM.render(<Calendar />, document.getElementById('react-calendar-container'));
  </script>
</body>
</html>
