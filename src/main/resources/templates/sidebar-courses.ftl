<#-- File: sidebar-courses.ftl -->
    <link rel="stylesheet" href="styles/calendar.css">
<div>
<#-- Suppose you pass in a "courses" model from your Spark route. Example:
model.put("courses", someListOfCourses);
-->
<#list courses as course>
<div class="sidebar-course-item">
      <span>${course.name} (${course.courseNumber})</span>
      <!-- Button triggers /add-course with the course ID -->
      <button
        class="add-course-btn"
        data-course-id="${course.id}"
      >+</button>
    </div>
    <hr/>
  </#list>
</div>