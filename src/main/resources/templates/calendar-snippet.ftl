<#-- File: calendar-snippet.ftl -->
<div id="calendar-snippet">
  <h3>Your Current Schedule</h3>
  <#-- Suppose you pass in a "schedule" model: a list of items the user has added -->
  <ul>
    <#list schedule as item>
      <li>${item.name}</li>
    </#list>
  </ul>
  <p>(This is just a placeholder for your real calendar UI.
     If you want a table or time slots, put them here.)</p>
</div>