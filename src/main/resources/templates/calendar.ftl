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

        .course-list-container {
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
            flex: 3;
            background-color: #e0eaff;
            padding: 10px;
            box-sizing: border-box;
        }

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
    </style>
</head>
<body>

    <div class="top-bar">
        <h3>Scheduler Navigation Bar</h3>
    </div>

    <div class="main-container">
        <div class="sidebar">
            <h4>Search Area</h4>

            <!-- Search Input -->
            <input type="text" placeholder="Search for classes..." class="search-input"/>

            <!-- Advanced Search Button -->
            <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>

            <!-- Course List Container -->
            <div class="course-list-container">
                <p>Course List will appear here...</p>
            </div>
        </div>

        <div class="content-area">
            <div class="calendar-view">
                <h4>Calendar View</h4>
                <!-- Calendar content -->
            </div>

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

    <!-- Advanced Search Modal -->
    <div id="advancedSearchModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <h3>Advanced Search Options</h3>
            <!-- Future filter inputs go here -->
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

</body>
</html>
