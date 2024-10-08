package dev.efekos.se.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class TopTenCache {

    private final Function<Integer, Map<UUID,Double>> fetch;
    private final List<Map<UUID,Double>> maps = new ArrayList<>();

    public TopTenCache(Function<Integer, Map<UUID, Double>> fetch) {
        this.fetch = fetch;
    }

    public Map<UUID,Double> get(int page){
        if(maps.size()-1<page||maps.get(page)==null) fetch(page);
        return maps.get(page);
    }

    private void fetch(int page){
        Map<UUID, Double> applied = fetch.apply(page);
        while (maps.size()<page+1) maps.add(null);
        maps.set(page, applied);
    }

    public void clearCache(){
        maps.clear();
    }


}
