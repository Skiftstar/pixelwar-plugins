package Kyu.ServerCoreBungee.Bansystem.HelperClasses;

import java.util.ArrayList;
import java.util.List;

public class BanReason {

    private String reason;
    private List<BanTime> bantimes = new ArrayList<>();

    public BanReason(String reason, List<BanTime> bantimes) {
        this.reason = reason;
        this.bantimes = bantimes;
    }

    public String getReason() {
        return reason;
    }

    public BanTime getBantime(int index) {
        if (index < 0) index = 0;
        if (index > bantimes.size() - 1) index = bantimes.size() - 1;
        System.out.println(index);
        bantimes.forEach(e -> System.out.println(e.getBanType().toString()));
        return bantimes.get(index);
    }
}
