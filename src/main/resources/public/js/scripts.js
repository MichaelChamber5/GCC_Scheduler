// Main Application Script
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
      updateScheduleTable();
    };
  });

  // Perform search and update sidebar (include advanced filters if available)
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

    // Append advanced filters if provided
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
            var dataAdded = course.onSchedule ? "true" : "false";
            html += "<div class='sidebar-course-item'>";
            html += "<span>" + course.name + " (" + course.courseNumber + ")</span>";
            html += "<button class='course-action-btn' data-course-id='" + course.id + "' data-added='" + dataAdded + "'>" + buttonText + "</button>";
            // Add the Info button. Store the course object (URI-encoded JSON) in a data attribute.
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

  // Trigger search on Enter key in the search input field
  document.querySelector(".search-input").addEventListener("keydown", function(e) {
    if (e.key === "Enter") {
      performSearch();
    }
  });

  function attachCourseActionButtons() {
    document.querySelectorAll(".course-action-btn").forEach(function(btn) {
      btn.onclick = function() {
        var courseId = btn.dataset.courseId;
        var isAdded = (btn.dataset.added === "true");
        if (isAdded) {
          removeCourse(courseId, btn);
        } else {
          addCourse(courseId, btn);
        }
      };
    });
  }

  function attachCourseInfoButtons() {
    document.querySelectorAll(".course-info-btn").forEach(function(btn) {
      btn.onclick = function() {
        var courseInfoStr = btn.dataset.courseInfo;
        var course = JSON.parse(decodeURIComponent(courseInfoStr));
        // Build the info HTML with additional details
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
        // Re-run search to update sidebar buttons
        performSearch();
        updateScheduleTable();
      })
      .catch(function(err) {
        console.error("Error removing course globally:", err);
      });
  };

  // Modal open/close for Advanced Search
  window.openModal = function() {
    document.getElementById("advancedSearchModal").style.display = "block";
  };
  window.closeModal = function() {
    document.getElementById("advancedSearchModal").style.display = "none";
  };

  // Set up the Apply Filters button inside the Advanced Search Modal
  document.getElementById("applyFiltersBtn").addEventListener("click", function() {
    closeModal();
    performSearch();
  });

  // Error modal helper functions
  function showErrorModal(message) {
    document.getElementById("errorModalMessage").textContent = message;
    document.getElementById("errorModal").style.display = "block";
  }
  function closeErrorModal() {
    document.getElementById("errorModal").style.display = "none";
  }
  window.closeErrorModal = closeErrorModal;

  // Course Info Modal close helper
  window.closeCourseInfoModal = function() {
    document.getElementById("courseInfoModal").style.display = "none";
  };

  window.onclick = function(e) {
    var modal = document.getElementById("advancedSearchModal");
    if (e.target === modal) {
      modal.style.display = "none";
    }
  };

  // Function to update the schedule table underneath the calendar
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

  // Initial calls
  performSearch();
  updateScheduleTable();
});


// React Calendar Component (Babel)
window.refreshCalendar = null;
const dayLetterToIndex = { "M": 0, "T": 1, "W": 2, "R": 3, "F": 4 };

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
    return () => {
      window.refreshCalendar = null;
    };
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
