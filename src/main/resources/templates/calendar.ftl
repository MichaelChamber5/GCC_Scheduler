<#-- File: calendar.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Scheduler Layout (React Calendar with Remove Button)</title>
    <link rel="stylesheet" href="/styles/calendar.css">



<!-- React, ReactDOM, Babel, and Moment.js -->
<script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
    <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    <script crossorigin src="https://unpkg.com/moment/min/moment.min.js"></script>
</head>
<body>
    <div class="top-bar">
        <#include "navbar.ftl">
    </div>

    <div class="main-container">
        <!-- Sidebar -->
        <div class="sidebar">
            <h4>Search Area</h4>
            <!-- Search Input -->
            <input type="text" placeholder="Search for classes..." class="search-input"/>
            <!-- Advanced Search Button -->
            <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>

            <!-- This div will be dynamically populated with search results -->
            <div id="sidebar-container">
                Loading courses...
            </div>
        </div>
<script>
  // Search input: on Enter, perform AJAX call to /search and render results.
  document.querySelector('.search-input').addEventListener('keypress', function(e) {
if (e.key === "Enter") {
const query = e.target.value;
fetch("/search?q=" + encodeURIComponent(query))
.then(response => {
if (!response.ok) {
throw new Error("Search failed");
}
          return response.json();
        })
.then(results => {
let html = "";
results.forEach(course => {
html += "<div class='sidebar-course-item'>";
html += "<span>" + course.name + " (" + course.courseNumber + ")</span>";
// Always render "Add" button initially.
html += "<button class='add-course-btn' data-course-id='" + course.id + "'>Add</button>";
html += "</div><hr/>";
});
          document.getElementById("sidebar-container").innerHTML = html;
          // Attach event listeners for the new buttons
          wireAddButtons();
        })
.catch(err => console.error("Error during search:", err));
    }
  });
</script>

        <!-- Content Area (Calendar, etc.) -->
        <div class="content-area">
            <div class="calendar-view">
                <!-- The dynamic React-based calendar goes here -->
                <div class="calendar-container" id="react-calendar-container">
                    <!-- React will fill this with the custom work week calendar -->
                </div>

                <!-- Include the dynamic courses table rendered with React -->
                <div class="calendar-table-wrapper">
                    <#include "table.ftl">
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
    <!-- Sidebar and Calendar snippet logic -->
        <script>
          // On page load, fetch the sidebar snippet (which now could be empty or preset)
          document.addEventListener("DOMContentLoaded", function() {
    fetch("/sidebar-courses")
    .then(response => response.text())
    .then(html => {
    document.getElementById("sidebar-container").innerHTML = html;
    wireAddButtons();  // attach click listeners to buttons
    })
    .catch(err => console.error("Failed to load sidebar:", err));
          });

          // Attach click listeners for Add and Remove buttons
          function wireAddButtons() {
    const btns = document.querySelectorAll(".add-course-btn, .remove-course-btn");
    btns.forEach(btn => {
    if (btn.classList.contains("add-course-btn")) {
    btn.onclick = function() {
    const courseId = btn.getAttribute("data-course-id");
    addCourse(courseId, btn);
    };
              } else if (btn.classList.contains("remove-course-btn")) {
    btn.onclick = function() {
    const courseId = btn.getAttribute("data-course-id");
    removeCourseFromSidebar(courseId, btn);
    };
              }
            });
          }

          // Function to add a course. Instead of reloading the entire sidebar, update the clicked button.
          function addCourse(courseId, btn) {
    const params = new URLSearchParams();
    params.append("courseId", courseId);

    fetch("/add-course", {
    method: "POST",
    body: params
    })
    .then(response => response.text())
    .then(result => {
    // On success, update the button to "Remove"
    btn.textContent = "Remove";
    btn.classList.remove("add-course-btn");
    btn.classList.add("remove-course-btn");
    btn.onclick = function() {
    removeCourseFromSidebar(courseId, btn);
    };
              // Refresh the React calendar
              if (window.refreshCalendar) {
    window.refreshCalendar();
    }
            })
    .catch(err => console.error("Error adding course:", err));
          }

          // Function to remove a course from the sidebar.
          function removeCourseFromSidebar(courseId, btn) {
    const params = new URLSearchParams();
    params.append("scheduleItemId", courseId);

    fetch("/remove-course", {
    method: "POST",
    body: params
    })
    .then(response => response.text())
    .then(result => {
    // On success, update the button back to "Add"
    btn.textContent = "Add";
    btn.classList.remove("remove-course-btn");
    btn.classList.add("add-course-btn");
    btn.onclick = function() {
    addCourse(courseId, btn);
    };
              // Refresh the React calendar
              if (window.refreshCalendar) {
    window.refreshCalendar();
    }
            })
    .catch(err => console.error("Error removing course:", err));
          }

          // Global function to remove a course from the calendar (used by calendar remove button)
          function removeCourseGlobal(scheduleItemId) {
    const params = new URLSearchParams();
    params.append("scheduleItemId", scheduleItemId);

    fetch("/remove-course", {
    method: "POST",
    body: params
    })
    .then(response => response.text())
    .then(result => {
    if (window.refreshCalendar) {
    window.refreshCalendar();
    }
            })
    .catch(err => console.error("Error removing course:", err));
          }
        </script>

        <!-- React-based Custom Work Week Calendar -->
        <script type="text/babel">
          window.refreshCalendar = null;

          // Helper: day letter to index
          const dayLetterToIndex = { "M": 0, "T": 1, "W": 2, "R": 3, "F": 4 };

          // Get Monday of the current week
          function getCurrentMonday() {
    return moment().startOf('week').add(1, 'days');
    }

          // Convert a schedule item into event objects
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

          function Calendar() {
    const [events, setEvents] = React.useState([]);

    const fetchSchedule = () => {
    fetch('/api/schedule')
    .then(response => {
    if (!response.ok) throw new Error("Failed to fetch schedule");
    return response.json();
    })
    .then(data => {
    const allEvents = data.flatMap(mapScheduleItemToEvents);
    setEvents(allEvents);
    })
    .catch(error => {
    console.error("Error fetching schedule:", error);
    });
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
    e.stopPropagation();
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

          function removeCourse(scheduleItemId) {
    window.removeCourseGlobal(scheduleItemId);
    }

          ReactDOM.render(<Calendar />, document.getElementById('react-calendar-container'));
        </script>
    </body>
    </html>