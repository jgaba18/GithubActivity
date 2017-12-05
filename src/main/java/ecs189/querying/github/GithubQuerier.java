package ecs189.querying.github;

import ecs189.querying.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Vincent on 10/1/2017.
 */
public class GithubQuerier {

    private static final String BASE_URL = "https://api.github.com/users/";
    private static final int MAX_PUSH_EVENTS = 10;  // SET to desired number of push Events

    public static String eventsAsHTML(String user) throws IOException, ParseException {
        List<JSONObject> response = getEvents(user);
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");

        // Add type of event as header
        String heading = String.format("%s's last %d PUSH events\n", user, MAX_PUSH_EVENTS);
        sb.append("<h1 class=\"heading\">");
        sb.append(heading);

        for (int i = 0; i < response.size(); i++) {
            JSONObject event = response.get(i);
            JSONObject payload = event.getJSONObject("payload");
            JSONArray commits = payload.getJSONArray("commits");

            int commitLen = commits.length();
            String [] commitsStr = new String[commitLen];
            int commitcurr = 0;
            while(commitcurr < commitLen){
                JSONObject obj = commits.getJSONObject(commitcurr);
                commitsStr[commitcurr] = obj.getString("sha");
                commitcurr++;
            }

            System.out.println("**************");
            System.out.println(Arrays.toString(commitsStr));
            System.out.println("**************");


            // Get created_at date, and format it in a more pleasant style
            String creationDate = event.getString("created_at");
            SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            SimpleDateFormat outFormat = new SimpleDateFormat("dd MMM, yyyy");
            Date date = inFormat.parse(creationDate);
            String formatted = outFormat.format(date);


            // Add formatted date
            sb.append("<h3>");
            sb.append(" Date:   ");
            sb.append(formatted);
            sb.append("< /h3>");

            //sb.append("<br />");
            // Add collapsible JSON textbox (don't worry about this for the homework; it's just a nice CSS thing I like)
            sb.append("<a data-toggle=\"collapse\" href=\"#event-" + i + "\">JSON</a>");
            sb.append("<div id=event-" + i + " class=\"collapse\" style=\"height: auto;\"> <pre>");
            sb.append(event.toString());
            sb.append("</pre> </div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private static List<JSONObject> getEvents(String user) throws IOException {
        List<JSONObject> eventList = new ArrayList<JSONObject>();
        int page = 1;
        String url = BASE_URL + user + "/events";

        JSONArray events = getEventPage(page, url);

        int numofPushEvents = 0;

        while(events.length() != 0 && numofPushEvents < 10) {
            for (int i = 0; i < events.length() && numofPushEvents < 10; i++) {
                JSONObject jObj = events.getJSONObject(i);
                String eventType = jObj.getString("type");

                if (eventType.equals("PushEvent")) {
                    eventList.add(jObj);
                    numofPushEvents++;
                }

            }
            page++;
            events = getEventPage(page, url);
        }

        System.out.println(eventList);
        return eventList;
    }

    private static JSONArray getEventPage(int pageNum, String baseURL) throws IOException {
        String pushURL = String.format("%s?page=%s", baseURL, Integer.toString(pageNum));
        URL url = new URL(pushURL);
        JSONObject pushPage = Util.queryAPI(url);
        JSONArray eventPageList = pushPage.getJSONArray("root");
        return eventPageList;
    }
}
