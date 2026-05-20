import java.util.*;

public class SeatDataCache {

    private static final Map<String, List<SeatInfo>> seatMap = new HashMap<>();
    private static final Map<String, Boolean> soldOutMap = new HashMap<>();

    public static void put(String date, List<SeatInfo> seats, boolean allSoldOut) {
        seatMap.put(date, seats);
        soldOutMap.put(date, allSoldOut);
    }

    public static List<SeatInfo> getSeats(String date) {
        return seatMap.get(date);
    }

    public static Boolean isSoldOut(String date) {
        return soldOutMap.getOrDefault(date, false);
    }

    public static boolean hasData(String date) {
        return seatMap.containsKey(date);
    }
}