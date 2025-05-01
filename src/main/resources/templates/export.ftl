<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Print View — ${scheduleName?html} (${semester?html})</title>

  <!-- html2pdf.js -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.9.2/html2pdf.bundle.min.js"></script>

  <style>
    body { margin: 1em; font-family: Arial, sans-serif; }
    h1   { text-align: center; margin-bottom: 0.5em; }
    #downloadPdf {
      display: block;
      margin: 0 auto 1em;
      background: #4CAF50;
      color: #fff;
      border: none;
      padding: 12px 24px;
      font-size: 16px;
      cursor: pointer;
      border-radius: 4px;
    }
    #downloadPdf:hover { background: #45a049; }

    /* schedule table styling */
    .calendar-table {
      width: 100%;
      border-collapse: collapse;
      table-layout: fixed;
    }
    .calendar-table th,
    .calendar-table td {
      border: 1px solid #ccc;
      padding: 4px;
      font-size: 12px;
      text-align: center;
      word-wrap: break-word;
    }
    .calendar-table th { background: #eee; }
  </style>
</head>
<body>

  <button id="downloadPdf">Download PDF</button>

  <div id="exportContent">
    <h1>${scheduleName?html} — ${semester?html}</h1>
    <p style="text-align:center; font-size:14pt; font-weight:bold; margin-top:0.5em;">
        Total Credits: ${totalCredits}
    </p>

    <table class="calendar-table">
      <thead>
        <tr>
          <th>Name</th>
          <th>Credits</th>
          <th>Professor</th>
          <th>Location</th>
          <th>Days</th>
          <th>Time</th>
        </tr>
      </thead>
      <tbody>
        <#list items as i>
          <tr>
            <td>${i.name?html}</td>
            <td>${i.credits?string}</td>
            <td>
              <#-- if professors is comma-list, split and take first -->
              <#assign profs = i.professors?default("")?split(",")>
              ${profs[0]?html}
            </td>
            <td>${i.location?html!''}</td>
            <td>${i.days?html!''}</td>
            <td>
              <#assign s = i.start?string("0000")>
              <#assign e = i.end?string("0000")>
              ${s?substring(0,2)}:${s?substring(2)}–${e?substring(0,2)}:${e?substring(2)}
            </td>
          </tr>
        </#list>
      </tbody>
    </table>
  </div>

  <script>
    document.getElementById('downloadPdf').addEventListener('click', () => {
      const element = document.getElementById('exportContent');
      html2pdf()
        .set({
          margin:      10,
          filename:    `schedule_${semester}.pdf`,
          html2canvas: { scale: 2 },
          jsPDF:       { unit: 'pt', format: 'letter', orientation: 'portrait' }
        })
        .from(element)
        .save();
    });
  </script>
</body>
</html>
