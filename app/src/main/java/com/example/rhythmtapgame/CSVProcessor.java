package com.example.rhythmtapgame;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CSVProcessor {

    private static final String TAG = "CSVProcessor";
    private final Map<String, List<Integer>> tilePositionsMap = new HashMap<>();

    public CSVProcessor() {
    }

    public void loadCSV(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to open file!" + e);
        } finally {
            try {
                inputStream.close();
                reader.close();
            } catch (IOException e) {
                Log.e(TAG, "Unable to close file!" + e);
            }
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        if (parts.length < 4) {
            Log.e(TAG, "Invalid line format: " + line);
            return;
        }

        String songName = parts[0].trim();
        String level = parts[1].trim().split("lvl")[1].trim();
        String variation = parts[2].trim().split("v")[1].trim();
        String indexes = parts[3].trim();

        String key = songName + "_" + level + "_" + variation;
        List<Integer> tileIndexes = parseIndexes(indexes);

        if (!tilePositionsMap.containsKey(key)) {
            tilePositionsMap.put(key, new ArrayList<>());
        }

        Objects.requireNonNull(tilePositionsMap.get(key)).addAll(tileIndexes);

    }

    private List<Integer> parseIndexes(String indexes) {
        List<Integer> result = new ArrayList<>();
        indexes = indexes.replaceAll("[\\[\\]\"]", "");
        String[] indexArray = indexes.split(",");
        for (String index : indexArray) {
            try {
                result.add(Integer.parseInt(index.trim()));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Unable to parse int!" + e);
            }
        }
        return result;
    }

    public List<Integer> getTilePositions(String songName, int level, int variation) {
        String key;
        if (level % 6 == 0) {
            key = songName + "_" + 1 + "_" + variation;
        } else if (level >= 14) {
            key = songName + "_" + 14 + "_" + variation;
        } else {
            key = songName + "_" + level + "_" + variation;
        }
        return tilePositionsMap.get(key);
    }
}
