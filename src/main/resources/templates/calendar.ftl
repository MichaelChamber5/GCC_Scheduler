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
        <!-- Include the Sidebar from sidebar-courses.ftl -->
        <#include "sidebar-courses.ftl">

        <!-- Content Area -->
        <div class="content-area">
            <div class="calendar-view">
                <!-- Calendar Grid from grid.ftl -->
                <div class="calendar-container" id="react-calendar-container">
                    <#include "grid.ftl">
                </div>

                <!-- Courses Table (table.ftl) -->
                <div class="calendar-table-wrapper">
                    <#include "table.ftl">
                </div>
            </div>
        </div>
    </div>
</body>
</html>
