package greet;

public class Event {
    int eventId;
    String date, time, description, location;

    public Event(int eventId, String date, String time,
                 String description, String location) {
        this.eventId = eventId;
        this.date = date;
        this.time = time;
        this.description = description;
        this.location = location;
    }
}
