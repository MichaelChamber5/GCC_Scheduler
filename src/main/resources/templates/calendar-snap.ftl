<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Calendar Snapshot</title>
  <!-- Include html2canvas from a CDN -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
  <style>
    /* Reset and basic font from your scheduler layout */
    html, body {
margin: 0;
padding: 0;
font-family: Arial, sans-serif;
background-color: #e0eaff;
}
    h2 {
text-align: center;
margin-top: 20px;
}
    /* Calendar container style similar to the scheduler layout */
.calendar-container {
max-width: 800px;
margin: 10px auto;
font-family: Arial, sans-serif;
border: 1px solid #999;
background-color: #fff;
padding: 10px;
display: inline-block;
}
.calendar-header {
background: #f0f0f0;
padding: 8px;
text-align: center;
font-weight: bold;
border-bottom: 2px solid #333;
}
/* Small calendar grid (the five days) */
.small-calendar-grid {
display: flex;
justify-content: space-between;
}
.day-column {
flex: 1;
border: 1px solid #ccc;
margin: 2px;
font-size: 10px;
}
.day-header {
background: #f0f0f0;
text-align: center;
padding: 4px;
border-bottom: 1px solid #ccc;
font-weight: bold;
}
.time-slot {
border-bottom: 1px dashed #ccc;
padding: 2px;
text-align: center;
}
/* Snapshot controls styling */
#snapshot-wrapper {
text-align: center;
margin-top: 20px;
}
#snapshot-img {
margin-top: 20px;
border: 1px solid #333;
display: block;
max-width: 100%;
}
button {
padding: 8px 16px;
background-color: #4CAF50;
color: white;
border: none;
font-size: 14px;
cursor: pointer;
margin-top: 10px;
}
button:hover {
background-color: #45a049;
}
</style>
</head>
<body>
<h2>Calendar Snapshot</h2>

<!-- Container that holds both the Fall and Spring calendars -->
<div id="calendars" style="text-align: center;">
    <!-- Define slot arrays using FreeMarker list literals -->
    <#assign hourlySlots = ["8:00", "9:00", "10:00", "11:00", "12:00", "1:00", "2:00", "3:00", "4:00", "5:00", "6:00", "7:00", "8:00"]>
    <#assign ninetySlots = ["8:00", "9:30", "11:00", "12:30", "2:00", "3:30", "5:00", "6:30", "8:00"]>

    <!-- Fall Semester Calendar -->
    <div class="calendar-container" id="fall-calendar">
      <div class="calendar-header">Fall Semester</div>
      <div class="small-calendar-grid">
        <!-- Monday (hourly slots) -->
        <div class="day-column">
          <div class="day-header">Monday</div>
          <#list hourlySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Tuesday (90-minute slots) -->
        <div class="day-column">
          <div class="day-header">Tuesday</div>
          <#list ninetySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Wednesday (hourly slots) -->
        <div class="day-column">
          <div class="day-header">Wednesday</div>
          <#list hourlySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Thursday (90-minute slots) -->
        <div class="day-column">
          <div class="day-header">Thursday</div>
          <#list ninetySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Friday (hourly slots) -->
        <div class="day-column">
          <div class="day-header">Friday</div>
          <#list hourlySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
      </div>
    </div>

    <!-- Spring Semester Calendar -->
    <div class="calendar-container" id="spring-calendar">
      <div class="calendar-header">Spring Semester</div>
      <div class="small-calendar-grid">
        <!-- Monday (hourly slots) -->
        <div class="day-column">
          <div class="day-header">Monday</div>
          <#list hourlySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Tuesday (90-minute slots) -->
        <div class="day-column">
          <div class="day-header">Tuesday</div>
          <#list ninetySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Wednesday (hourly slots) -->
        <div class="day-column">
          <div class="day-header">Wednesday</div>
          <#list hourlySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Thursday (90-minute slots) -->
        <div class="day-column">
          <div class="day-header">Thursday</div>
          <#list ninetySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
        <!-- Friday (hourly slots) -->
        <div class="day-column">
          <div class="day-header">Friday</div>
          <#list hourlySlots as slot>
            <div class="time-slot">${slot}</div>
          </#list>
        </div>
      </div>
    </div>
  </div>

  <!-- Snapshot capture and selection controls -->
  <div id="snapshot-wrapper">
    <button id="capture-btn">Capture Snapshot</button>
    <div id="snapshot-result" style="display: none;">
      <img id="snapshot-img" alt="Calendar Snapshot">
      <br>
      <button id="select-calendar-btn">Select This Calendar</button>
    </div>
  </div>

  <script>
    // Capture the calendars as an image using html2canvas when the button is clicked.
    document.getElementById('capture-btn').addEventListener('click', function() {
const calendarsElement = document.getElementById('calendars');
html2canvas(calendarsElement).then(canvas => {
const imageURL = canvas.toDataURL("image/png");
document.getElementById('snapshot-img').src = imageURL;
document.getElementById('snapshot-result').style.display = "block";
}).catch(err => {
console.error("Error capturing the snapshot:", err);
});
    });

    // Redirect to the full calendar view upon selection.
    document.getElementById('select-calendar-btn').addEventListener('click', function() {
window.location.href = "/calendar";
});
  </script>
</body>
</html>
