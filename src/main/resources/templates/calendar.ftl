<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Scheduler Layout</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <style>
        html, body {
            margin: 0;
            padding: 0;
            height: 100vh;
            overflow: hidden;
            font-family: Arial, sans-serif;
            background-color: white;
        }

        .top-bar {
            height: 8vh;
            background-color: #b30000; /* red color */
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

        /* Sidebar (Search Area) */
        .sidebar {
            width: 20%;
            background-color: #f5f5f5; /* Light grey */
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
            border: 1px solid #ccc;
        }

        .advanced-search-btn {
            width: 100%;
            padding: 8px;
            background-color: #b30000;
            color: white;
            border: none;
            font-size: 14px;
            cursor: pointer;
            margin-bottom: 15px;
        }

        .advanced-search-btn:hover {
            background-color: #990000;
        }

        .course-list-container {
            flex-grow: 1;
            background-color: white;
            border: 1px solid #ccc;
            padding: 10px;
            box-sizing: border-box;
            overflow-y: auto;
            max-height: calc(90vh - 150px); /* Adjust to avoid overflow of sidebar */
        }

        /* Course Element */
        .course-element {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background-color: #fff;
            border: 1px solid #ccc;
            margin: 10px 0;
            padding: 10px;
            box-sizing: border-box;
        }

        .course-info {
            flex-grow: 1;
            font-size: 14px;
        }

        .course-buttons {
            display: flex;
            align-items: center;
        }

        .course-buttons button {
            background: none;
            border: none;
            cursor: pointer;
            font-size: 16px;
            margin-left: 10px;
            color: #b30000; /* Red color */
        }

        .course-buttons button:hover {
            color: #990000;
        }

        /* Content Area */
        .content-area {
            width: 80%;
            display: flex;
            flex-direction: column;
            padding: 0;
            margin: 0;
            box-sizing: border-box;
        }

        /* Calendar View */
        .calendar-view {
            flex: 3;
            background-color: #ffecec; /* Soft red-pink tint */
            padding: 10px;
            box-sizing: border-box;
        }

        /* Table Area */
        .calendar-table-wrapper {
            flex: 1;
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            display: flex;
            flex-direction: column;
            justify-content: stretch;
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
            background-color: white;
        }

        /* Headings */
        h4 {
            margin: 0 0 10px 0;
        }
    </style>
</head>
<body>

    <div class="top-bar">
        <div style="display: flex; align-items: center; gap: 10px;">
            <h3 style="margin-right: 20px;">GCC Scheduler</h3>
            <button style="background: none; border: none; color: white; font-size: 24px; cursor: pointer;" title="Undo">⤺</button>
            <button style="background: none; border: none; color: white; font-size: 24px; cursor: pointer;" title="Redo">⤻</button>
        </div>
        <div style="margin-left: auto; display: flex; align-items: center; gap: 10px;">
            <button style="padding: 6px 12px; background-color: white; color: #b30000; border: none; border-radius: 4px; cursor: pointer;">Add Item</button>
            <button style="background: none; border: none; color: white; font-size: 24px; cursor: pointer;" title="Settings">⚙</button>
        </div>
    </div>


    <div class="main-container">
        <div class="sidebar">
            <h4>Find Courses</h4>
            <input type="text" placeholder="🔍 Search" class="search-input"/>
            <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>
            <div class="course-list-container">
                <!-- Example Course Elements -->
                <div class="course-element">
                    <div class="course-info">
                        <strong>Course Name 1</strong><br>
                        <span>Course Description or Details here...</span>
                    </div>
                    <div class="course-buttons">
                        <button onclick="toggleDetails()" title="Show Details"><i class="fas fa-chevron-down"></i></button>
                        <button onclick="addCourseToSchedule()" title="Add to Schedule"><i class="fas fa-plus"></i></button>
                    </div>
                </div>

                <!-- More course elements can go here -->
            </div>
        </div>

        <div class="content-area">
            <div class="calendar-view">
                <h4>Calendar View</h4>
            </div>
            <div class="calendar-table-wrapper">
                <!-- Renamed the div and integrated the React CourseTable component -->
                <div id="course-table"></div> <!-- Placeholder for CourseTable.jsx -->
            </div>
        </div>
    </div>

    <!-- modal -->
    <div id="advancedSearchModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <h2>Advanced Search</h2>
            <p>Additional search filters go here.</p>
        </div>
    </div>

    <!-- Script to load React component -->
    <script type="text/javascript">
        // Make sure React is loaded and your JSX file is compiled
        // The following code assumes that you have integrated React into your .ftl file

        // Replace this with the actual import of your compiled JS file
        // <script src="/path/to/compiled/courseTable.js"></script>

        // Once the page has loaded, React will render the CourseTable component
        window.onload = () => {
            // Render the CourseTable component into the 'course-table' div
            ReactDOM.render(<CourseTable />, document.getElementById('course-table'));
        };
    </script>

<script src="/js/calendar.js"></script>

</body>
</html>
