package edu.gcc.BitwiseWizards;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.GsonBuilder;
import freemarker.template.Configuration;
import freemarker.template.Version;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;
import org.mindrot.jbcrypt.BCrypt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class server {

    private static NewDatabaseManager dbm;

    public static void main(String[] args) {

        dbm = new NewDatabaseManager();       // ctor prints its own messages
        port(4567);
        staticFileLocation("/public");

        Configuration cfg = new Configuration(new Version(2, 3, 31));
        cfg.setClassForTemplateLoading(server.class, "/templates");
        FreeMarkerEngine fm = new FreeMarkerEngine(cfg);

        /* ------------------------------------------------------------------ */
        /*  LOGIN / REGISTER                                                  */
        /* ------------------------------------------------------------------ */
        get("/", (rq, rs) -> { rs.redirect("/login"); return null; });

        get("/login",    (rq, rs) -> new ModelAndView(new HashMap<>(), "login.ftl"), fm);
        get("/register", (rq, rs) -> new ModelAndView(new HashMap<>(), "register.ftl"), fm);

        post("/register", (rq, rs) -> {
            String u = rq.queryParams("user");
            String p = rq.queryParams("confirmPassword");
            System.out.println("DEBUG register  user=" + u);
            if (u==null||p==null||u.isEmpty()||p.isEmpty()) { rs.redirect("/register?error=missing"); return null; }
            // hash it here:
            String hashed = BCrypt.hashpw(p, BCrypt.gensalt());
            int id = dbm.insertUser(u, hashed);
            System.out.println("DEBUG register  id=" + id);
            rs.redirect(id==-1?"/register?error=fail":"/login");
            return null;
        });

        post("/login", (rq, rs) -> {
            String email = rq.queryParams("user");
            String plain  = rq.queryParams("password");
            if (email==null || plain==null || email.isBlank() || plain.isBlank()) {
                rs.redirect("/login?error=missing");
                return null;
            }

            // 1) pull the BCrypt hash
            String hash = dbm.getPasswordHashByEmail(email);
            // 2) verify it
            if (hash == null || !BCrypt.checkpw(plain, hash)) {
                rs.redirect("/login?error=invalid");
                return null;
            }
            // 3) fetch the user ID by email only
            int id = dbm.getUserIDByEmail(email);
            if (id <= 0) {
                rs.redirect("/login?error=invalid");
                return null;
            }

            // 4) success!
            rq.session().attribute("user", new User(id, email, /* you can pass null here or the hash */ null));
            rs.redirect("/schedules");
            return null;
        });


        /* ------------------------------------------------------------------ */
        /*  SCHEDULE LIST PAGE                                                */
        /* ------------------------------------------------------------------ */
        get("/schedules", (rq, rs) -> {
            User user = rq.session().attribute("user");
            if (user==null){ rs.redirect("/login"); return null; }

            List<Schedule> list = dbm.getAllUserSchedules(user.getId());
            System.out.println("DEBUG /schedules list size=" + list.size());

            Map<String,Object> model = new HashMap<>();
            model.put("user", user);
            model.put("schedules", list);
            return new ModelAndView(model, "schedules.ftl");
        }, fm);

        post("/schedules", (rq, rs) -> {
            User u = rq.session().attribute("user");
            if (u==null){ rs.redirect("/login"); return null; }
            String name = rq.queryParams("schedName");
            System.out.println("DEBUG create schedule '" + name + "'");
            if (name!=null && !name.trim().isEmpty())
                dbm.insertUserSchedule(u.getId(), name.trim());
            rs.redirect("/schedules");
            return null;
        });

        // DELETE A SCHEDULE
        post("/delete-schedule", (rq, rs) -> {
            User user = rq.session().attribute("user");
            if (user == null) {
                rs.redirect("/login");
                return null;
            }

            // parse the submitted schedule ID
            int schedId = Integer.parseInt(rq.queryParams("schedId"));
            System.out.println("DEBUG /delete-schedule  schedId=" + schedId);

            // remove all courses & personal items then the schedule itself
            dbm.deleteUserSchedule(user.getId(), schedId);

            // back to the list view
            rs.redirect("/schedules");
            return null;
        });


        /* ------------------------------------------------------------------ */
        /*  SINGLE SCHEDULE PAGE                               */
        /* ------------------------------------------------------------------ */
        get("/schedules/:schedId", (rq, rs) -> {
            try {
                User user = rq.session().attribute("user");
                if (user==null){ rs.redirect("/login"); return null; }

                int sid = Integer.parseInt(rq.params(":schedId"));
                System.out.println("DEBUG /schedules/:schedId  sid=" + sid);

                Schedule sched = dbm.getSchduleByID(user.getId(), sid);
                System.out.println("DEBUG   scheduleObj=" + sched);

                if (sched==null){ halt(404,"Schedule not found"); }

                List<CourseItem>  courses = dbm.getScheduleCourses(sid);
                List<ScheduleItem>personal = dbm.getSchedulePersonalItems(sid);
                System.out.println("DEBUG   courseCount=" + courses.size() +
                        "  personalCount=" + personal.size());

                Map<String,Object> model = new HashMap<>();
                model.put("user", user);
                model.put("scheduleName", sched.getName());
                model.put("schedId", sid);
                // send list only if you still need it server-side
                model.put("schedule", new ArrayList<ScheduleItem>(){{ addAll(courses); addAll(personal); }});
                return new ModelAndView(model, "calendar.ftl");

            } catch (Exception e){
                System.out.println("DEBUG ERROR in /schedules/:schedId");
                e.printStackTrace();
                throw e;   // Spark will render the 500 page
            }
        }, fm);

        /* ------------------------------------------------------------------ */
        /*  API ENDPOINTS                                                     */
        /* ------------------------------------------------------------------ */
        get("/api/schedule", (rq, rs) -> {
            try {
                User u = rq.session().attribute("user");
                if (u == null) {
                    rs.status(401);
                    return "unauth";
                }

                int sid = Integer.parseInt(rq.queryParams("schedId"));
                String sem = Optional.ofNullable(rq.queryParams("semester")).orElse("");
                System.out.println("DEBUG /api/schedule sid=" + sid + " sem=" + sem);

                List<ScheduleItem> all = new ArrayList<>();
                all.addAll(dbm.getScheduleCourses(sid));         // these are CourseItem instances
                all.addAll(dbm.getSchedulePersonalItems(sid));   // these are ScheduleItem

                List<Map<String, Object>> out = new ArrayList<>();
                for (ScheduleItem si : all) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", si.getId());
                    m.put("name", si.getName());
                    m.put("meetingTimes", si.getMeetingTimes());
                    m.put("semester", sem);
                    m.put("type", si instanceof CourseItem ? "course" : "personal");


                    if (si instanceof CourseItem) {
                        CourseItem c = (CourseItem) si;
                        m.put("credits", c.getCredits());
                        m.put("location", c.getLocation());
                        m.put("courseNumber", c.getCourseNumber());
                        m.put("section", c.getSection());
                        m.put("description", c.getDescription());
                        m.put("professors", c.getProfessors());
                    } else {
                        // personal items don’t have those fields
                        m.put("credits", null);
                        m.put("location", "NA");
                        m.put("courseNumber", "NA");
                        m.put("section", "NA");
                        m.put("description", "NA");
                        m.put("professors", Collections.emptyList());
                    }

                    out.add(m);
                }

                rs.type("application/json");
                return new GsonBuilder().create().toJson(out);

            }
            catch (Exception ex) {
                System.out.println("DEBUG ERROR /api/schedule"); ex.printStackTrace();
                rs.status(500);
                return "server error";
            }
        });



        post("/add-course", (rq, rs) -> {
            try {
                User u = rq.session().attribute("user");
                if (u == null) { halt(401); }

                int sid = Integer.parseInt(rq.queryParams("schedId"));
                int cid = Integer.parseInt(rq.queryParams("courseId"));
                System.out.println("DEBUG /add-course sid=" + sid + " cid=" + cid);

                // the candidate course we want to add
                CourseItem newCourse = dbm.getCourseByID(cid);
                if (newCourse == null) {
                    rs.status(404);
                    return new Gson().toJson(Map.of("error", "Course not found"));
                }

                //checks for conflicts
                List<ScheduleItem> existing = new ArrayList<>();
                existing.addAll(dbm.getScheduleCourses(sid));
                existing.addAll(dbm.getSchedulePersonalItems(sid));

                for (ScheduleItem si : existing) {
                    if (si.conflicts(newCourse)) {                      // <-- your method
                        System.out.println("DEBUG   conflict with item " + si.getId());
                        rs.status(409);  // 409 = Conflict
                        return new Gson().toJson(
                                Map.of("error", "Course conflicts with " + si.getName()));
                    }
                }

                // no conflict was found
                dbm.addCourseToSchedule(sid, cid);
                rs.type("application/json");
                return new Gson().toJson(Map.of("success", true));

            } catch (Exception ex) {
                System.out.println("DEBUG ERROR /add-course"); ex.printStackTrace();
                rs.status(500);
                return new Gson().toJson(Map.of("error", "Server error while adding"));
            }
        });

        post("/remove-course", (rq, rs) -> {
            try{
                User u = rq.session().attribute("user");
                if (u==null){ halt(401); }

                int sid = Integer.parseInt(rq.queryParams("schedId"));
                int itemId = Integer.parseInt(rq.queryParams("scheduleItemId"));
                System.out.println("DEBUG /remove-course sid="+sid+" itemId="+itemId);

                dbm.removeCourseFromSchedule(sid,itemId);

                rs.type("application/json");
                return new Gson().toJson(Collections.singletonMap("success", true));

            }catch(Exception ex){
                System.out.println("DEBUG ERROR /remove-course"); ex.printStackTrace();
                rs.status(500);
                return new Gson().toJson(Collections.singletonMap("error","remove failed"));
            }
        });

        // 1) Add Personal Item
        post("/add-item", (rq, rs) -> {
            User u = rq.session().attribute("user");
            if (u == null) halt(401);

            int sid      = Integer.parseInt(rq.queryParams("schedId"));
            String name  = rq.queryParams("name");
            String mtRaw = rq.queryParams("meetingTimes");

            // parse the JSON string into our Map<Character,List<Integer>>
            JsonObject mtJson = JsonParser.parseString(mtRaw).getAsJsonObject();
            Map<Character, List<Integer>> meetingTimes = new HashMap<>();
            for (var entry : mtJson.entrySet()) {
                char day = entry.getKey().charAt(0);
                JsonArray arr = entry.getValue().getAsJsonArray();
                meetingTimes.put(day, List.of(arr.get(0).getAsInt(), arr.get(1).getAsInt()));
            }

            // conflict detection
            List<ScheduleItem> existing = new ArrayList<>();
            existing.addAll(dbm.getScheduleCourses(sid));
            existing.addAll(dbm.getSchedulePersonalItems(sid));
            ScheduleItem candidate = new ScheduleItem(name, meetingTimes);
            candidate.setName(name);
            candidate.setMeetingTimes(meetingTimes);
            for (ScheduleItem si: existing) {
                if (si.conflicts(candidate)) {
                    rs.status(409);
                    return new Gson().toJson(Map.of("error", "Conflicts with “" + si.getName() + "”"));
                }
            }

            // persist and return new ID
            int newId = dbm.addPersonalItemToSchedule(sid, name, meetingTimes);
            rs.type("application/json");
            return new Gson().toJson(Map.of("success", true, "itemId", newId));
        });

// 2) Remove Personal Item
        post("/remove-item", (rq, rs) -> {
            User u = rq.session().attribute("user");
            if (u == null) halt(401);

            int sid    = Integer.parseInt(rq.queryParams("schedId"));
            int itemId = Integer.parseInt(rq.queryParams("itemId"));

            dbm.removePersonalItemFromSchedule(sid, itemId);
            rs.type("application/json");
            return new Gson().toJson(Map.of("success", true));
        });

        /* --------------  SEARCH -------------- */
        get("/search", (rq, rs) -> {
            try{
                User u = rq.session().attribute("user");
                if (u==null){ rs.status(401); return "unauth"; }

                String q        = Optional.ofNullable(rq.queryParams("q")).orElse("");
                String semester = Optional.ofNullable(rq.queryParams("semester")).orElse("");
                int sid = -1;
                try{ sid=Integer.parseInt(rq.queryParams("schedId")); }catch(Exception ignored){}
                System.out.println("DEBUG /search q='"+q+"' sem="+semester+" sid="+sid);

                Search search = new Search(dbm);
                List<CourseItem> results = search.search(q, semester);

                Set<Integer> existing = (sid>0)? new HashSet<>(dbm.getScheduleCourseIds(sid)) : Collections.emptySet();
                results.forEach(c -> c.setOnSchedule(existing.contains(c.getId())));

                rs.type("application/json");
                return new GsonBuilder().create().toJson(results);

            }catch(Exception ex){
                System.out.println("DEBUG ERROR /search"); ex.printStackTrace();
                rs.status(500); return "server error";
            }
        });
    }


    private static Date parseTime(String s){
        if (s==null||s.trim().isEmpty()) return null;
        try{
            while(s.length()<4) s="0"+s;
            return new SimpleDateFormat("HH:mm").parse(s.substring(0,2)+":"+s.substring(2));
        }catch(ParseException e){ return null; }
    }
}
