<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Scheduler Layout</title>

    <style>
        /* ----------  LAYOUT / THEME ---------- */
        html,body{margin:0;padding:0;height:100vh;overflow:hidden;font-family:Arial,sans-serif}
.top-bar{height:10vh;background:#333;color:#fff;display:flex;align-items:center;padding:0 20px;box-sizing:border-box}
.logout-button,.back-button{background:#4CAF50;border:none;color:#fff;padding:8px 16px;font-size:14px;cursor:pointer;margin-right:20px}
.main-container{display:flex;height:90vh}
.sidebar{width:20%;background:#f0f0f0;padding:20px;box-sizing:border-box;display:flex;flex-direction:column}
#semester-toggle{margin-bottom:10px}
.semester-btn{padding:8px 12px;margin-right:5px;border:1px solid #ccc;background:#eee;cursor:pointer}
.semester-btn.active{background:#4CAF50;color:#fff;border-color:#4CAF50}
.search-input{width:100%;padding:8px;margin-bottom:10px;box-sizing:border-box;font-size:14px}
.advanced-search-btn{width:100%;padding:8px;background:#4CAF50;color:#fff;border:none;font-size:14px;cursor:pointer;margin-bottom:15px}
.advanced-search-btn:hover{background:#45a049}
#sidebar-container{flex-grow:1;background:#fff;border:1px solid #ccc;padding:10px;box-sizing:border-box;overflow-y:auto}
.content-area{width:80%;display:flex;flex-direction:column}
.calendar-view{flex:2;background:#e0eaff;padding:10px;box-sizing:border-box;position:relative}
.calendar-table-wrapper{position:fixed;bottom:0;left:20%;width:80%;height:30vh;background:#fff;overflow-y:auto;z-index:100}
.calendar-table{width:100%;height:100%;border-collapse:collapse;table-layout:fixed}
.calendar-table th,.calendar-table td{border:1px solid #ccc;padding:4px;font-size:12px;text-align:center}
.calendar-table th{background:#eee}
/* ----------  CALENDAR GRID ---------- */
.calendar-container{max-width:1200px;margin:0 auto;border:1px solid #999;height:57vh;overflow-y:auto;background:#fff}
.calendar-grid{display:flex;border-top:2px solid #333;border-left:2px solid #333}
.day-column{flex:1;border-right:2px solid #333;border-bottom:2px solid #333;display:flex;flex-direction:column;position:relative}
.day-header{background:#f0f0f0;padding:8px;text-align:center;font-weight:bold;border-bottom:2px solid #333}
.time-slots{flex:1;display:flex;flex-direction:column;position:relative}
.time-slot{border-bottom:1px solid #ccc;padding:4px;font-size:12px;height:40px;position:relative}
.slot-label{position:relative;z-index:2}
.event-overlay{position:absolute;top:0;left:0;right:0;bottom:0;background:rgba(0,102,204,.8);display:flex;align-items:center;justify-content:center;font-size:10px;color:#333;z-index:1}
.remove-button{margin-left:8px;background:transparent;border:none;color:#333;font-weight:bold;cursor:pointer;pointer-events:auto}
.course-info-btn {
  background: none;
  border: none;
  color: #2196F3;    /* keeps the blue color on the “ℹ️” */
  padding: 0;
  font-size: 1.2em;  /* tweak as you like */
  line-height: 1;
  cursor: pointer;
}
.course-info-btn:hover {
  color: #1976D2;    /* subtle hover color change */
}

/* ----------  MODALS ---------- */
.modal{display:none;position:fixed;z-index:1000;left:0;top:0;width:100vw;height:100vh;background:rgba(0,0,0,.5)}
.modal-content{background:#fff;margin:10% auto;padding:20px;border-radius:8px;width:40%;box-shadow:0 5px 15px rgba(0,0,0,.3)}
.close{float:right;font-size:20px;font-weight:bold;cursor:pointer}
</style>

<!-- Libraries -->
<script crossorigin src="https://unpkg.com/react@17/umd/react.development.js"></script>
    <script crossorigin src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"></script>
    <script crossorigin src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
    <script crossorigin src="https://unpkg.com/moment/min/moment.min.js"></script>

    <!-- Expose schedule ID to JS -->
    <script>window.currentSchedId = ${schedId!0};</script>
</head>

<body>
    <!-- ----------  NAV BAR ---------- -->
    <div class="top-bar">
        <button class="logout-button" onclick="handleLogout()">Logout</button>
        <button class="back-button" onclick="location.href='/schedules'">All Schedules</button>
        <button id="addItemBtn" class="back-button" onclick="openAddItemModal()">Add Personal Item</button>
        <button class="back-button"
                  onclick="
                    window.location =
                      '/exportPage?schedId=' + encodeURIComponent(window.currentSchedId)
                     + '&semester='  + encodeURIComponent(window.currentSemester)
                  ">
            Export to PDF
          </button>
        <h3>Scheduler Navigation Bar</h3>
        <div id="credits-display"
               style="margin-left:auto;color:#fff;font-weight:bold;font-size:16px;">
            Total Credits: <span id="totalCredits">0</span>
          </div>
    </div>

    <!-- ----------  MAIN LAYOUT ---------- -->
    <div class="main-container">
        <!-- ----------  SIDEBAR ---------- -->
        <div class="sidebar">
            <h4>Search Area</h4>
            <div id="semester-toggle">
                <button id="fall-btn"   class="semester-btn active" data-semester="2023_Fall">Fall</button>
                <button id="spring-btn" class="semester-btn"        data-semester="2024_Spring">Spring</button>
            </div>
            <input type="text" class="search-input" placeholder="Search for classes…">
            <button class="advanced-search-btn" onclick="openModal()">Advanced Search</button>
            <div id="sidebar-container"></div>
        </div>

        <!-- ----------  CALENDAR + TABLE ---------- -->
        <div class="content-area">
            <div class="calendar-view">
                <div id="react-calendar-container" class="calendar-container"></div>
                <div class="calendar-table-wrapper">
                    <table id="scheduleDetailsTable" class="calendar-table"></table>
                </div>
            </div>
        </div>
    </div>

    <!-- ----------  MODALS ---------- -->
    <div id="advancedSearchModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <h3>Advanced Search Options</h3>
            <label>Department Code:</label><br><input id="deptInput"  placeholder="e.g. COMP"><br><br>
            <label>Days (e.g., MWF):</label><br><input id="daysInput" placeholder="Enter days"><br><br>
            <label>Start Time (0930):</label><br><input id="startInput" placeholder="0930"><br><br>
            <label>End Time (1500):</label><br><input id="endInput"   placeholder="1500"><br><br>
            <button id="applyFiltersBtn">Apply Filters</button>
        </div>
    </div>

    <div id="addItemModal" class="modal">
          <div class="modal-content">
            <span class="close" onclick="closeAddItemModal()">&times;</span>
            <h3>Add Personal Item</h3>
            <label>Name:</label><br>
            <input id="itemNameInput" type="text" placeholder="e.g. Doctor’s appt"><br><br>
            <label>Days (e.g. MWF):</label><br>
            <input id="itemDaysInput" type="text" placeholder="e.g. MWR"><br><br>
            <label>Start Time (HHMM):</label><br>
            <input id="itemStartInput" type="text" placeholder="0930"><br><br>
            <label>End Time (HHMM):</label><br>
            <input id="itemEndInput"   type="text" placeholder="1030"><br><br>
            <button id="createItemBtn">Create</button>
          </div>
        </div>

    <div id="errorModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeErrorModal()">&times;</span>
            <p id="errorModalMessage"></p>
        </div>
    </div>

    <div id="courseInfoModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeCourseInfoModal()">&times;</span>
            <div id="courseInfoModalContent"></div>
        </div>
    </div>

    <!-- ----------  PAGE SCRIPT ---------- -->
    <script>
<#noparse>
    document.addEventListener('DOMContentLoaded', () => {
/* ------------------  HELPER ------------------ */
const $ = sel => document.querySelector(sel);

window.handleLogout = () => location.href = '/login';

window.openAddItemModal  = () => $('#addItemModal').style.display = 'block';
window.closeAddItemModal = () => $('#addItemModal').style.display = 'none';

// ↓↓↓ CREATE PERSONAL ITEM ↓↓↓
      $('#createItemBtn').onclick = () => {
        const name  = $('#itemNameInput').value.trim();
        const days  = $('#itemDaysInput').value.trim().split('');
        const start = parseInt($('#itemStartInput').value, 10);
        const end   = parseInt($('#itemEndInput').value, 10);
        if (!name || days.length===0 || isNaN(start) || isNaN(end)) {
          return showErrorModal('Please fill out all fields.');
        }

        const meetingTimes = {};
        days.forEach(d => meetingTimes[d] = [ start, end ]);

        const params = new URLSearchParams({
          schedId:      window.currentSchedId,
          name,
          meetingTimes: JSON.stringify(meetingTimes)
        });

        fetch('/add-item', { method: 'POST', body: params })
          .then(async r => {
            const payload = await r.json();
            if (!r.ok) throw payload;
            closeAddItemModal();
            window.refreshCalendar?.();
            updateScheduleTable();
          })
          .catch(err => showErrorModal(err.error || 'Error adding personal item'));
      };

       // ↓↓↓ REMOVE PERSONAL ITEM (GLOBAL) ↓↓↓
            window.removeItemGlobal = id => {
              const params = new URLSearchParams({
                schedId: window.currentSchedId,
                itemId:  id
              });
              fetch('/remove-item', { method:'POST', body: params })
                .then(async r => {
                  const payload = await r.json();
                  if (!r.ok) throw payload;
                  window.refreshCalendar?.();
                  updateScheduleTable();
                })
                .catch(err => showErrorModal(err.error || 'Error removing item'));
            };

        /* -----  SEMESTER TOGGLE  ----- */
        // 1) Initialize to Fall by default:
        window.currentSemester = $('#fall-btn').dataset.semester;

        // 2) Wire up both buttons in one place:
        document.querySelectorAll('.semester-btn').forEach(btn => {
          btn.onclick = () => {
            // a) Toggle the 'active' class
            document.querySelectorAll('.semester-btn')
              .forEach(b => b.classList.remove('active'));
            btn.classList.add('active');

            // b) Update the global semester
            window.currentSemester = btn.dataset.semester;

            // c) Refresh everything
            performSearch();
            updateScheduleTable();
            window.refreshCalendar?.();
          };
        });



        /* ------------------  SEARCH  ------------------ */
        window.performSearch = () => {
const query = $('.search-input').value.trim();
if (!query) { $('#sidebar-container').innerHTML = ''; return; }

let url = '/search?q=' + encodeURIComponent(query) +
'&semester=' + encodeURIComponent(window.currentSemester) +
'&schedId='  + encodeURIComponent(window.currentSchedId);

const dept  = $('#deptInput') ?.value.trim(); if (dept)  url += '&dept='  + encodeURIComponent(dept);
const days  = $('#daysInput') ?.value.trim(); if (days)  url += '&days='  + encodeURIComponent(days);
const start = $('#startInput')?.value.trim(); if (start) url += '&start=' + encodeURIComponent(start);
const end   = $('#endInput')  ?.value.trim(); if (end)   url += '&end='   + encodeURIComponent(end);

fetch(url)
.then(r => r.json())
.then(list => {
let html = list.length
   ? list.map(c => {
       // build a "Day HH:MM–HH:MM" string for each meetingTime entry
       let times = '';
       if (c.meetingTimes) {
         // 1) group days by identical [start,end]
         const groups = {};
         Object.entries(c.meetingTimes).forEach(([day, [start, end]]) => {
           const key = `${start}-${end}`;
           (groups[key] = groups[key] || []).push(day);
         });
         // 2) for each group, join the days together and format the time once
         times = Object.entries(groups)
           .map(([key, days]) => {
             const [s, e] = key.split('-').map(Number);
             const fmt = i => {
               const h24 = Math.floor(i/100);
               const h12 = (h24 % 12) === 0 ? 12 : (h24 % 12);
               const m   = i % 100;
               return `${h12}:${String(m).padStart(2,'0')}`;
             };
             return `${days.join('')} ${fmt(s)}–${fmt(e)}`;
           })
           .join(', ');
       }
       // inject times into the span
       return `
     <div class="sidebar-course-item">
       <span>${c.name} (${c.courseNumber})${times ? ` <small>— ${times}</small>` : ''}</span>
       <button class="course-action-btn"
               data-course-id="${c.id}"
               data-added="${c.onSchedule}">${c.onSchedule ? '-':'+'}</button>
       <button class="course-info-btn"
               data-course-info="${encodeURIComponent(JSON.stringify(c))}">ℹ️</button>
     </div><hr>`;
     }).join('')
   : '<p>No courses found.</p>';
                  $('#sidebar-container').innerHTML = html;
                  attachCourseActionButtons();
                  attachCourseInfoButtons();
              })
.catch(err => console.error('Search error',err));
        };
        $('.search-input').addEventListener('keydown', e => e.key==='Enter'&&performSearch());

        /* -----  ADD / REMOVE COURSE  ----- */
        function attachCourseActionButtons(){
document.querySelectorAll('.course-action-btn').forEach(btn=>{
btn.onclick=()=> btn.dataset.added==='true'
? removeCourse(btn.dataset.courseId,btn)
: addCourse   (btn.dataset.courseId,btn);
});
        }
        function attachCourseInfoButtons(){
          document.querySelectorAll('.course-info-btn').forEach(btn=>{
            btn.onclick=()=>{
              const c = JSON.parse(decodeURIComponent(btn.dataset.courseInfo));

              // ── 1) group days by identical times ──
              let timesHtml = '';
              if (c.meetingTimes) {
                const groups = {};
                Object.entries(c.meetingTimes).forEach(([day, [start, end]]) => {
                  const key = `${start}-${end}`;
                  (groups[key] = groups[key] || []).push(day);
                });
                // formatter to 12-hour
                const fmt = i => {
                  const h24 = Math.floor(i/100);
                  const h12 = (h24 % 12) === 0 ? 12 : (h24 % 12);
                  const m   = i % 100;
                  return `${h12}:${String(m).padStart(2,'0')}`;
                };
                // build final string like "MWF 12:00–12:50, R 2:00–2:50"
                timesHtml = Object.entries(groups)
                  .map(([key, days]) => {
                    const [s,e] = key.split('-').map(Number);
                    return `${days.join('')} ${fmt(s)}–${fmt(e)}`;
                  })
                  .join(', ');
              }

              // ── 2) inject into modal HTML ──
              $('#courseInfoModalContent').innerHTML = `
                <h3>${c.name} (${c.courseNumber})</h3>
                <p><b>Credits:</b> ${c.credits}</p>
                <p><b>Location:</b> ${c.location}</p>
                <p><b>Section:</b> ${c.section}</p>
                <p><b>Description:</b> ${c.description||'No description'}</p>
                ${ timesHtml
                    ? `<p><b>Meeting Times:</b> ${timesHtml}</p>`
                    : ''
                }
                <p><b>Professor(s):</b> ${c.professors?.map(p=>p.name).join(', ')||'None'}</p>`;

              $('#courseInfoModal').style.display='block';
            };
          });
        }

function addCourse(courseId, btn) {
const params = new URLSearchParams({ courseId, schedId: window.currentSchedId });

    fetch('/add-course', { method: 'POST', body: params })
.then(async r => {
const j = await r.json();
if (!r.ok) throw j;
return j;
})
.then(() => {
btn.textContent   = '-';
btn.dataset.added = 'true';
window.refreshCalendar?.();
updateScheduleTable();
})
.catch(err => {
showErrorModal(err.error || 'Error adding class');
});
}
        function removeCourse(courseId,btn){
const params = new URLSearchParams({scheduleItemId:courseId,schedId:window.currentSchedId});
            fetch('/remove-course',{method:'POST',body:params})
.then(r=>r.json())
.then(()=>{btn.textContent='+';btn.dataset.added='false';window.refreshCalendar?.();updateScheduleTable();})
.catch(err=>showErrorModal(err.error||'Remove failed'));
        }
        window.removeCourseGlobal = id => {
          // hit your remove-course endpoint
          const params = new URLSearchParams({
            schedId: window.currentSchedId,
            scheduleItemId: id
          });
          fetch('/remove-course', { method: 'POST', body: params })
            .then(r => r.json())
            .then(() => {
              // 1) update calendar & table as before
              window.refreshCalendar?.();
              updateScheduleTable();

              // 2) find the matching sidebar “+/-” button and reset it
              const btn = document.querySelector(`.course-action-btn[data-course-id="${id}"]`);
              if (btn) {
                btn.textContent = '+';
                btn.dataset.added = 'false';
              }
            })
            .catch(err => showErrorModal(err.error || 'Remove failed'));
        };


        /* -----  MODALS ----- */
        window.openModal = ()=> $('#advancedSearchModal').style.display='block';
        window.closeModal= ()=> $('#advancedSearchModal').style.display='none';
        $('#applyFiltersBtn').onclick=()=>{closeModal();performSearch();};
        function showErrorModal(msg){$('#errorModalMessage').textContent=msg;$('#errorModal').style.display='block';}
window.closeErrorModal=()=>$('#errorModal').style.display='none';
window.closeCourseInfoModal=()=>$('#courseInfoModal').style.display='none';
window.onclick=e=>{
if(e.target=== $('#advancedSearchModal')) closeModal();
};

        /* -----  SCHEDULE TABLE ----- */
        function updateScheduleTable(){
          fetch(
            '/api/schedule'
            + '?schedId='  + encodeURIComponent(window.currentSchedId)
            + '&semester=' + encodeURIComponent(window.currentSemester)
          )
          .then(r => r.json())
          .then(data => {
            // 1) Inject semester on every personal item
            data.forEach(item => {
              if (item.type === 'personal') {
                item.semester = window.currentSemester;
              }
            });

            // 2) Only keep items matching the active semester
            const filtered = data.filter(item =>
              item.semester.toLowerCase() === window.currentSemester.toLowerCase()
            );

            // 3) Build your rows
            const rows = filtered.map(c => {
              const profs = c.professors?.map(p => p.name).join(', ') || 'None';
              return `
                <tr>
                  <td>${c.name}</td>
                  <td>${profs}</td>
                  <td>${c.location}</td>
                  <td>${c.credits ?? ''}</td>
                  <td>${c.courseNumber}</td>
                  <td>${c.section}</td>
                  <td>${c.description || ''}</td>
                </tr>`;
            }).join('');

            const total = filtered
                    .filter(i => i.type === 'course')
                    .reduce((sum, c) => sum + (Number(c.credits) || 0), 0);
                  document.getElementById('totalCredits').textContent = total;

            document.getElementById('scheduleDetailsTable').innerHTML = `
              <thead><tr>
                <th>Course Name</th><th>Professor(s)</th><th>Location</th>
                <th>Credits</th><th>Course Code</th><th>Section</th><th>Description</th>
              </tr></thead>
              <tbody>${rows}</tbody>`;
          });
        }
        // fetch and show total credits
        fetch(
          '/api/credits'
          + '?schedId='  + encodeURIComponent(window.currentSchedId)
          + '&semester=' + encodeURIComponent(window.currentSemester)
        )
        .then(r => r.json())
        .then(data => {
          document.getElementById('totalCredits').textContent = data.totalCredits;
        })
        .catch(err => console.error('Failed to load total credits', err));



        /* ------------------  INIT  ------------------ */
        performSearch();
        updateScheduleTable();
    });
</#noparse>
    </script>

    <!-- ----------  REACT CALENDAR ---------- -->
    <script type="text/babel">
        window.refreshCalendar=null;
        const dayLetterToIndex={M:0,T:1,W:2,R:3,F:4};
        const getCurrentMonday=()=>moment().startOf('week').add(1,'days');

        function mapScheduleItemToEvents(item){
const events=[];
Object.entries(item.meetingTimes||{}).forEach(([d,t])=>{
const base=getCurrentMonday().add(dayLetterToIndex[d], 'days');
const [s,e]=t;
const start=base.clone().hour(Math.floor(s/100)).minute(s%100);
const end  =base.clone().hour(Math.floor(e/100)).minute(e%100);
events.push({title:item.name,start:start.toDate(),end:end.toDate(),id:item.id,type:item.type});
            });
            return events;
        }

        function Calendar(){
const [events,setEvents]=React.useState([]);
const load=()=>{
fetch(
  '/api/schedule'
  + '?schedId='  + encodeURIComponent(window.currentSchedId)
  + '&semester=' + encodeURIComponent(window.currentSemester)
)
.then(r => r.json())
.then(data => {
  // 1) Tag each personal item with the current semester
  data.forEach(i => {
    if (i.type === 'personal') {
      i.semester = window.currentSemester;
    }
  });

  // 2) Only keep items whose semester exactly matches
  const filtered = data.filter(i =>
    i.semester.toLowerCase() === window.currentSemester.toLowerCase()
  );

  setEvents(filtered.flatMap(mapScheduleItemToEvents));
});


            };
            React.useEffect(load,[]);
            React.useEffect(()=>{window.refreshCalendar=load;return()=>window.refreshCalendar=null;},[]);

            const monday=getCurrentMonday();
            const days=[0,1,2,3,4].map(i=>monday.clone().add(i,'days').toDate());
            const step=(d)=>(['Tue','Thu'].includes(moment(d).format('ddd'))?90:60);

            return (
                <div className="calendar-grid">
                    {days.map((d,i)=><DayColumn key={i} day={d} startHour={8} endHour={21} stepMinutes={step(d)}
                                                events={events.filter(e=>moment(e.start).isSame(d,'day'))}/>)}
                </div>
            );
        }

        function DayColumn({day,startHour,endHour,stepMinutes,events}){
const slots=[];
let cur=moment(day).hour(startHour).minute(0);
const end=moment(day).hour(endHour).minute(0);
while(cur.isBefore(end)){slots.push({t:cur.clone(),step:stepMinutes});cur.add(stepMinutes,'minutes');}
            slots.push({t:end.clone(),step:stepMinutes,type:'night'});

            const overlaps=(ev,slot)=>{
if(slot.type==='night') return moment(ev.start).isSameOrAfter(slot.t);
const slotEnd=slot.t.clone().add(slot.step,'minutes');
return moment(ev.start).isBefore(slotEnd)&&moment(ev.end).isAfter(slot.t);
};

            return (
                <div className="day-column">
                    <div className="day-header">{moment(day).format('dddd')}</div>
                    <div className="time-slots">
                        {slots.map((s,i)=>{
const label=s.t.format('h:mm A');
const evts=events.filter(ev=>overlaps(ev,s));
return (
<div key={i} className="time-slot">
                                    <div className="slot-label">{label}</div>
                                    {evts.map((ev,j)=>
<div key={j} className="event-overlay">
                                            {ev.title}
                                            <button
                                                  className="remove-button"
                                                  onClick={e => {
                                                    e.stopPropagation();
                                                    if (ev.type === 'personal') {
                                                      window.removeItemGlobal(ev.id);
                                                    } else {
                                                      window.removeCourseGlobal(ev.id);
                                                    }
                                                  }}
                                                >
                                                  x
                                                </button>
                                        </div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                </div>
            );
        }

        ReactDOM.render(<Calendar/>,document.getElementById('react-calendar-container'));

    </script>
</body>
</html>
