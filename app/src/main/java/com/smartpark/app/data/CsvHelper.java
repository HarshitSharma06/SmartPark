package com.smartpark.app.data;

import android.content.Context;
import android.os.Environment;

import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.util.DateTimeUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvHelper {

    public static File exportHistoryToCsv(Context context, List<HashMap<String, String>> historyList) throws Exception {
        File docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docDir != null && !docDir.exists()) {
            docDir.mkdirs();
        }

        File csvFile = new File(docDir, "smartpark_history.csv");
        FileWriter writer = new FileWriter(csvFile);

        // Header
        writer.append("lot_id,name,price,viewed_at\n");

        for (HashMap<String, String> item : historyList) {
            String lotId = item.get("lot_id") != null ? item.get("lot_id") : "";
            String name = item.get("name") != null ? item.get("name").replace(",", " ") : "";
            String price = item.get("price") != null ? item.get("price").replaceAll("[^0-9]", "") : "0";
            String viewedAt = item.get("viewed_at") != null ? item.get("viewed_at") : "";

            writer.append(lotId).append(",")
                    .append(name).append(",")
                    .append(price).append(",")
                    .append(viewedAt).append("\n");
        }

        writer.flush();
        writer.close();
        return csvFile;
    }

    public static List<HashMap<String, String>> importHistoryFromCsv(InputStream inputStream) throws Exception {
        List<HashMap<String, String>> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false; // Skip CSV header
                continue;
            }
            String[] tokens = line.split(",");
            if (tokens.length >= 4) {
                HashMap<String, String> map = new HashMap<>();
                map.put("lot_id", tokens[0].trim());
                map.put("name", tokens[1].trim());
                map.put("price", "₹" + tokens[2].trim() + "/hr");
                map.put("viewed_at", tokens[3].trim());
                list.add(map);
            }
        }
        reader.close();
        return list;
    }

    public static File saveParkingReceipt(Context context, ParkingLot lot, int hours, int totalFee) throws Exception {
        File docDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (docDir != null && !docDir.exists()) {
            docDir.mkdirs();
        }

        String fileName = "receipt_" + System.currentTimeMillis() + ".txt";
        File receiptFile = new File(docDir, fileName);

        String receiptText = "=====================================\n" +
                "         SMARTPARK RECEIPT           \n" +
                "=====================================\n" +
                "Lot Name: " + lot.getName() + "\n" +
                "Address:  " + lot.getAddress() + "\n" +
                "Rate:     " + lot.getFormattedPrice() + "\n" +
                "Duration: " + hours + " hour(s)\n" +
                "Total Fee: ₹" + totalFee + "\n" +
                "Date/Time: " + DateTimeUtil.getCurrentDateTimeString() + "\n" +
                "=====================================\n" +
                "    Thank you for using SmartPark!   \n" +
                "=====================================\n";

        FileOutputStream fos = new FileOutputStream(receiptFile);
        fos.write(receiptText.getBytes(StandardCharsets.UTF_8));
        fos.flush();
        fos.close();

        return receiptFile;
    }
}
