<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <title>Your Schedules</title>
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
.top-bar h3 {
flex-grow: 1;
margin: 0;
}

.delete-button {
    background: none;
    border: none;
    color: red;
    font-weight: bold;
    font-size: 16px;
    cursor: pointer;
    margin-left: 8px;
    line-height: 1;
  }

/* Logout button styling */
.logout-button {
background-color: #4CAF50;
border: none;
color: white;
padding: 8px 16px;
font-size: 14px;
cursor: pointer;
margin-right: 20px;
}
/* Main container holds the sidebar and content area */
.main-container {
display: flex;
height: 90vh;
}
/* Sidebar styling */
.sidebar {
width: 20%;
background-color: #f0f0f0;
padding: 20px;
box-sizing: border-box;
display: flex;
flex-direction: column;
}
/* Schedule list styling */
.schedule-list {
flex-grow: 1;
overflow-y: auto;
}
.schedule-item {
margin-bottom: 10px;
}
.schedule-item a {
display: block;
padding: 10px;
background-color: #eee;
color: #333;
text-decoration: none;
border: 1px solid #ccc;
border-radius: 4px;
}
.schedule-item a:hover {
background-color: #ddd;
}
/* Add-schedule form styling */
.add-schedule-form {
margin-top: 20px;
}
.add-schedule-form input[type="text"] {
width: calc(100% - 22px);
padding: 8px;
box-sizing: border-box;
font-size: 14px;
margin-bottom: 10px;
}
.add-schedule-form button {
width: 100%;
padding: 10px;
background-color: #4CAF50;
color: white;
border: none;
font-size: 14px;
cursor: pointer;
}
.add-schedule-form button:hover {
background-color: #45a049;
}
/* Content area styling (empty for now) */
.content-area {
width: 80%;
display: flex;
justify-content: center;
align-items: center;
background-color: #eaeaea;
}
.content-area h2 {
color: #666;
}
</style>
</head>

<body>
<div class="top-bar">
    <button class="logout-button" onclick="window.location.href='/login'">Logout</button>
    <h3>My Schedules</h3>
  </div>

  <div class="main-container">
    <!-- Sidebar: list schedules + add form -->
    <div class="sidebar">
      <div class="schedule-list">
        <#if schedules?size == 0>
          <p>No schedules yet. Create one below!</p>
        <#else>
          <#list schedules as sched>
            <div class="schedule-item">
              <a href="/schedules/${sched.ID}">${sched.name}</a>
              <form
                class="delete-form"
                method="post"
                action="/schedules/${sched.ID}/delete"
                style="display:inline;"
                onsubmit="return confirm('Are you sure you want to delete this schedule?');"
              >
                <button type="submit" class="delete-button">×</button>
              </form>
            </div>
          </#list>
        </#if>
      </div>


      <form class="add-schedule-form" method="post" action="/schedules">
        <input type="text" name="schedName" placeholder="New schedule name" required />
        <button type="submit">Add Schedule</button>
      </form>
    </div>

    <!-- Content area is empty until a schedule is selected -->
    <div class="content-area">
      <h2>Select a schedule on the left to view its calendar.</h2>
    </div>
  </div>
</body>
</html>