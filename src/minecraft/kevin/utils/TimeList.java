package kevin.utils;

import java.util.HashMap;
import java.util.Map;

public class TimeList<T> {
    private final HashMap<Long, T> map = new HashMap<>();
    private final long keepTime;
    public boolean autoUpdate = true;

    public TimeList(long keepTime) {
        this.keepTime = keepTime;
    }

    public void add(T value) {
        long t = keepTime + System.currentTimeMillis();
        if (autoUpdate) update();
        while (!map.containsKey(t)) ++t;
        map.put(t, value);
    }

    public int size() {
        if (autoUpdate) update();
        return map.size();
    }

    public void update() {
        final long current = System.currentTimeMillis();
        synchronized (map) {
            final Long[] waiting = new Long[map.size()];
            int index = 0;
            for (Map.Entry<Long, T> entry : map.entrySet()) {
                Long t = entry.getKey();
                if (t < current) {
                    waiting[index] = t;
                    ++index;
                }
            }
            for (Long l : waiting) {
                map.remove(l);
            }
        }
    }
}
