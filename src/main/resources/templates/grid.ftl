<#-- File: grid.ftl -->
<script type="text/babel">
  // Helper: get the Monday of the current week
  function getCurrentMonday() {
    return moment().startOf('week').add(1, 'days');
  }

  // Calendar grid component
  function Calendar() {
    const [events, setEvents] = React.useState([]);

    const fetchSchedule = () => {
      fetch('/api/schedule')
      .then(response => {
        if (!response.ok) throw new Error("Failed to fetch schedule");
        return response.json();
      })
      .then(data => {
        // Convert schedule items into events based on meetingTimes
        const allEvents = data.flatMap(item => {
          const events = [];
          Object.entries(item.meetingTimes).forEach(([dayLetter, times]) => {
            const monday = getCurrentMonday();
            const dayIndex = { "M": 0, "T": 1, "W": 2, "R": 3, "F": 4 }[dayLetter];
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
        });
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
