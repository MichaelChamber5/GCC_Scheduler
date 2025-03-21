<#-- File: table.ftl -->
<link rel="stylesheet" href="/styles/calendar.css">
<div class="course-table-container" style="max-width:100%; margin:20px auto; font-family: Arial, sans-serif;">
    <div id="course-table"></div>
</div>

<script type="text/babel">
  function CourseTable() {
    const [courses, setCourses] = React.useState([]);

    // Fetch the user's courses from the API
    const fetchCourses = () => {
      fetch('/api/usercourses')
        .then(response => {
          if (!response.ok) {
            throw new Error("Failed to fetch user courses");
          }
          return response.json();
        })
        .then(data => {
          setCourses(data);
        })
        .catch(error => {
          console.error("Error fetching courses:", error);
        });
    };

    // Initial fetch when the component mounts
    React.useEffect(() => {
      fetchCourses();
    }, []);

    // Expose a refresh function globally for external triggers
    React.useEffect(() => {
      window.refreshUserCourses = fetchCourses;
      return () => { window.refreshUserCourses = null; };
    }, []);

    return (
      <table className="course-table" style={{ width:"100%", borderCollapse:"collapse" }}>
        <thead>
          <tr>
            <th style={{ border:"1px solid #ccc", padding:"4px", background:"#f0f0f0", fontSize:"10px" }}>Course</th>
            <th style={{ border:"1px solid #ccc", padding:"4px", background:"#f0f0f0", fontSize:"10px" }}>Course Name</th>
            <th style={{ border:"1px solid #ccc", padding:"4px", background:"#f0f0f0", fontSize:"10px" }}>Professor</th>
            <th style={{ border:"1px solid #ccc", padding:"4px", background:"#f0f0f0", fontSize:"10px" }}>Location</th>
          </tr>
        </thead>
        <tbody>
          {courses.map(course => (
            <tr key={course.id}>
              <td style={{ border:"1px solid #ccc", padding:"4px", fontSize:"10px" }}>
                {course.depCode ? course.depCode + " " : ""}{course.courseNumber || "N/A"} {course.section ? "(" + course.section + ")" : ""}
              </td>
              <td style={{ border:"1px solid #ccc", padding:"4px", fontSize:"10px" }}>
                {course.name || "N/A"}
              </td>
              <td style={{ border:"1px solid #ccc", padding:"4px", fontSize:"10px" }}>
                {course.professors || "N/A"}
              </td>
              <td style={{ border:"1px solid #ccc", padding:"4px", fontSize:"10px" }}>
                {course.location || "N/A"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  ReactDOM.render(<CourseTable />, document.getElementById('course-table'));
</script>
