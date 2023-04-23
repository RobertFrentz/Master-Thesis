package helpers;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeCustomFormatter {
    public static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
}
