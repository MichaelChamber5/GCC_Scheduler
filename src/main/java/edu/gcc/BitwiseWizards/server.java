package edu.gcc.BitwiseWizards;

import static spark.Spark.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import freemarker.template.Configuration;
import freemarker.template.Version;
import spark.ModelAndView;
import spark.template.freemarker.FreeMarkerEngine;

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
            String u = rq.queryParams("username");
            String p = rq.queryParams("confirmPassword");
            System.out.println("DEBUG register  user=" + u);
            if (u==null||p==null||u.isEmpty()||p.isEmpty()) { rs.redirect("/register?error=missing"); return null; }
            int id = dbm.insertUser(u, p);
            System.out.println("DEBUG register  id=" + id);
            rs.redirect(id==-1?"/register?error=fail":"/login");
            return null;
        });

        post("/login", (rq, rs) -> {
            String u = rq.queryParams("username");
            String p = Optional.ofNullable(rq.queryParams("confirmPassword"))
                    .orElse(rq.queryParams("password"));
            System.out.println("DEBUG login  u=" + u + "  p=" + p);
            if (u==null||p==null||u.trim().isEmpty()||p.trim().isEmpty()){
                rs.redirect("/login?error=Missing+credentials");
                return null;
            }
            int id = dbm.getUserID(u, p);
            System.out.println("DEBUG login  id=" + id);
            if (id>0){ rq.session().attribute("user", new User(id,u,p)); rs.redirect("/schedules"); }
            else     { rs.redirect("/login?error=invalid"); }
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
            try{
                User u = rq.session().attribute("user");
                if (u==null){ rs.status(401); return "unauth"; }

                int sid = Integer.parseInt(rq.queryParams("schedId"));
                System.out.println("DEBUG /api/schedule sid=" + sid);

                List<ScheduleItem> all = new ArrayList<>();
                all.addAll(dbm.getScheduleCourses(sid));
                all.addAll(dbm.getSchedulePersonalItems(sid));

                rs.type("application/json");
                return new GsonBuilder().create().toJson(all);

            }catch(Exception ex){
                System.out.println("DEBUG ERROR /api/schedule"); ex.printStackTrace();
                rs.status(500); return "server error";
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

                // parse and apply filters
                String deptParam = Optional.ofNullable(rq.queryParams("dept")).orElse("");
                String daysParam = Optional.ofNullable(rq.queryParams("days")).orElse("");
                List<Character> daysList = new ArrayList<>();
                for(char c : daysParam.toCharArray()){
                    daysList.add(c);
                }
                Date startDate = parseTime(rq.queryParams("start"));
                Date endDate   = parseTime(rq.queryParams("end"));

                // filter the search results
                results = search.filter(deptParam, daysList, startDate, endDate);



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
