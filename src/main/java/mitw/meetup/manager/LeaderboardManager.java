package mitw.meetup.manager;

import lombok.Getter;
import mitw.meetup.UHCMeetup;
import mitw.meetup.player.database.UHCMeetupDatabase;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    @Getter
    private static LeaderboardManager instance;

    @Getter
    private final UHCMeetupDatabase database;

    @Getter
    private Map<String, Integer> ratingTop;
    @Getter
    private Map<String, Integer> winsTop;
    @Getter
    private Map<String, Integer> killsTop;
    @Getter
    private Map<String, Double> kdrTop;

    public LeaderboardManager() {
        instance = this;
        database = new UHCMeetupDatabase();
        updateTops();
    }

    public String getRatingPosition(final String name) {
        int i = new ArrayList<>(ratingTop.keySet()).indexOf(name);
        return i == -1 ? "unkwown" : i + "";
    }

    public void updateTops() {
        Bukkit.getScheduler().runTaskAsynchronously(UHCMeetup.getInstance(), () -> {
            {
                final Map<String, Integer> ratingTop = new HashMap<>();
                database.getTable().executeQuery("SELECT name, elo from meetup_player")
                        .dataSource(database.getDatabase().getDataSource())
                        .result(r -> {
                            if (r.isBeforeFirst()) {
                                while (r.next()) {
                                    try {
                                        final String name = r.getString("name");
                                        if (name != null && !ratingTop.containsKey(name)) {
                                            ratingTop.put(name, r.getInt("elo"));
                                        }
                                    } catch (final SQLException e) {
                                        if (e.getMessage() != null && !e.getMessage().contains("empty result")) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            return r;
                        })
                        .run();
                this.ratingTop = ratingTop.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(1000).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            }
            {
                final Map<String, Integer> top = new HashMap<>();
                database.getTable().executeQuery("SELECT name, wins from meetup_player")
                        .dataSource(database.getDatabase().getDataSource())
                        .result(r -> {
                            if (r.isBeforeFirst()) {
                                while (r.next()) {
                                    try {
                                        final String name = r.getString("name");
                                        if (name != null && !top.containsKey(name)) {
                                            top.put(name, r.getInt("wins"));
                                        }
                                    } catch (final SQLException e) {
                                        if (e.getMessage() != null && !e.getMessage().contains("empty result")) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            return r;
                        })
                        .run();
                this.winsTop = top.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            }
            {
                final Map<String, Integer> top = new HashMap<>();
                database.getTable().executeQuery("SELECT name, global_kills from meetup_player")
                        .dataSource(database.getDatabase().getDataSource())
                        .result(r -> {
                            if (r.isBeforeFirst()) {
                                while (r.next()) {
                                    try {
                                        final String name = r.getString("name");
                                        if (name != null && !top.containsKey(name)) {
                                            top.put(name, r.getInt("global_kills"));
                                        }
                                    } catch (final SQLException e) {
                                        if (e.getMessage() != null && !e.getMessage().contains("empty result")) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            return r;
                        })
                        .run();
                this.killsTop = top.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            }
            {
                final Map<String, Double> top = new HashMap<>();
                database.getTable().executeQuery("SELECT name, global_kills, deaths from meetup_player")
                        .dataSource(database.getDatabase().getDataSource())
                        .result(r -> {
                            if (r.isBeforeFirst()) {
                                while (r.next()) {
                                    try {
                                        final String name = r.getString("name");
                                        if (name != null && !top.containsKey(name)) {
                                            final int deaths = r.getInt("deaths"), kills = r.getInt("global_kills");

                                            top.put(name, ((double) deaths == 0 ? (double) kills : ((double) kills / deaths)));
                                        }
                                    } catch (final SQLException e) {
                                        if (e.getMessage() != null && !e.getMessage().contains("empty result")) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            return r;
                        })
                        .run();
                this.kdrTop = top.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(10).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            }
        });
    }

}
