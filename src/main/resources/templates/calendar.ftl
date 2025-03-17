<#-- File: calendar.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Scheduler Layout (React Calendar with Remove Button)</title>
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

/* The container for the sidebar courses snippet */
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

/* The bottom table that you originally had */
.calendar-table-wrapper {
position: fixed;
bottom: 0;
left: 20%;      /* starts after the 20% sidebar */
width: 80%;     /* fills the rest of the screen */
height: 30vh;   /* adjust height as needed */
background-color: #ffffff; /* optional, to visually separate it */
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

/* Additional styling for the React-based calendar */
.calendar-container {
max-width: 1200px;
margin: 0 auto;
font-family: Arial, sans-serif;
border: 1px solid #999;
height: 57vh;
overflow-y: auto;
background-color: #fff;
}
/* The React-based time grid for the custom work week */
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
height: 40px; /* For illustration */
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
background: rgba(135, 206, 250, 0.5); /* light blue semi-transparent */
z-index: 1;
display: flex;
align-items: center;
justify-content: center;
font-size: 10px;
color: #333;
}
/* Let the 'x' button be clickable */
.remove-button {
margin-left: 8px;
background: transparent;
border: none;
color: #333;
font-weight: bold;
cursor: pointer;
pointer-events: auto;  /* ensure the button is clickable */
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
            <!-- Search Input -->
            <input type="text" placeholder="Search for classes..." class="search-input"/>
            <!-- Advanced Search Button -->
            <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>

            <!-- This div will be dynamically populated with /sidebar-courses snippet -->
            <div id="sidebar-container">
                Loading courses...
            </div>
        </div>

        <!-- Content Area (Calendar, etc.) -->
        <div class="content-area">
            <div class="calendar-view">
                <!-- The dynamic React-based calendar goes here -->
                <div class="calendar-container" id="react-calendar-container">
                    <!-- React will fill this with the custom work week calendar -->
                </div>

                <!-- Example “fixed” table at bottom -->
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
            <!-- Put advanced filter inputs here -->
        </div>
    </div>

    <script>
        function openModal() {
document.getElementById("advancedSearchModal").style.display = "block";
}
        function closeModal() {
document.getElementById("advancedSearchModal").style.display = "none";
}
        window.onclick = function(event) {
const modal = document.getElementById("advancedSearchModal");
if (event.target === modal) {
modal.style.display = "none";
}
        }
    </script>

    <!-- Existing Sidebar + Calendar snippet logic -->
    <script>
      // On page load, fetch the sidebar snippet
      document.addEventListener("DOMContentLoaded", function() {
fetch("/sidebar-courses")
.then(response => response.text())
.then(html => {
document.getElementById("sidebar-container").innerHTML = html;
wireAddButtons();  // attach click listeners to “Add” buttons
})
.catch(err => console.error("Failed to load sidebar:", err));
      });

      // Attach click listeners for “Add Course” buttons
      function wireAddButtons() {
const btns = document.querySelectorAll(".add-course-btn");
btns.forEach(btn => {
btn.addEventListener("click", function() {
const courseId = btn.getAttribute("data-course-id");
addCourse(courseId);
});
        });
      }

      // POST to /add-course, then refresh sidebar & the React calendar
      function addCourse(courseId) {
const params = new URLSearchParams();
params.append("courseId", courseId);

fetch("/add-course", {
method: "POST",
body: params
})
.then(response => response.text())
.then(html => {
const parser = new DOMParser();
const doc = parser.parseFromString(html, "text/html");

// 1) Sidebar
const sidebarDiv = doc.getElementById("sidebar-snippet");
if (sidebarDiv) {
document.getElementById("sidebar-container").innerHTML = sidebarDiv.innerHTML;
wireAddButtons();
}

          // 2) Re-fetch data in React calendar
          if (window.refreshCalendar) {
window.refreshCalendar();
}
        })
.catch(err => console.error("Error adding course:", err));
      }

      // POST to /remove-course, then refresh the calendar
      function removeCourseGlobal(scheduleItemId) {
const params = new URLSearchParams();
params.append("scheduleItemId", scheduleItemId);

fetch("/remove-course", {
method: "POST",
body: params
})
.then(response => response.text())
.then(result => {
// Re-fetch data in React calendar
if (window.refreshCalendar) {
window.refreshCalendar();
}
        })
.catch(err => console.error("Error removing course:", err));
      }
    </script>

    <!-- React-based Custom Work Week Calendar -->
    <script type="text/babel">
      // We'll define a function so we can refresh the calendar manually after add/remove ops
      window.refreshCalendar = null;

      // Helper: day letter to index
      const dayLetterToIndex = { "M": 0, "T": 1, "W": 2, "R": 3, "F": 4 };

      // Get Monday of the current week
      function getCurrentMonday() {
return moment().startOf('week').add(1, 'days');
}

      // Convert a schedule item into event objects
      // We store the scheduleItemId so we can remove it if needed
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
scheduleItemId: item.id    // store the item ID for removal
});
        });
        return events;
      }

      function Calendar() {
const [events, setEvents] = React.useState([]);

// We'll define a function that fetches the schedule
const fetchSchedule = () => {
fetch('/api/schedule')
.then(response => {
if (!response.ok) throw new Error("Failed to fetch schedule");
return response.json();
})
.then(data => {
// Convert each item to one or more events with an ID
const allEvents = data.flatMap(mapScheduleItemToEvents);
setEvents(allEvents);
})
.catch(error => {
console.error("Error fetching schedule:", error);
});
        };

        // On mount, fetch schedule
        React.useEffect(() => {
fetchSchedule();
}, []);

        // We'll expose a global function so we can re-fetch after removing courses
        React.useEffect(() => {
window.refreshCalendar = fetchSchedule;
return () => { window.refreshCalendar = null; };
        }, []);

        // Build the 5-day array (Mon-Fri)
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
let current = moment(day).hour(startHour).minute(0);
const end = moment(day).hour(endHour).minute(0);

while (current.isBefore(end)) {
slots.push(current.clone());
current.add(stepMinutes, 'minutes');
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
e.stopPropagation(); // prevent any parent clicks
removeCourse(event.scheduleItemId);
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

      // We'll call the removeCourse function we defined in the <script> above
      function removeCourse(scheduleItemId) {
// Just call the global removeCourse function from above
window.removeCourseGlobal(scheduleItemId);
}

      // Render the calendar into #react-calendar-container
      ReactDOM.render(<Calendar />, document.getElementById('react-calendar-container'));
    </script>
</body>
</html>

