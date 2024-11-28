package com.example.currencyconverter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Scanner;

public class CurrencyConverter {

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Currency Converter ===");

            displaySupportedCurrencies(client);

            System.out.print("Type currency to convert from (e.g., USD, EUR): ");
            String convertFrom = scanner.nextLine().trim().toUpperCase();

            System.out.print("Type currency to convert to (e.g., USD, EUR): ");
            String convertTo = scanner.nextLine().trim().toUpperCase();

            System.out.print("Type quantity to convert: ");
            BigDecimal quantity;
            try {
                quantity = scanner.nextBigDecimal();
            } catch (Exception e) {
                System.err.println("Invalid quantity entered. Please enter a valid number.");
                return;
            }

            // Validate currency codes format (basic check)
            if (!isValidCurrencyCode(convertFrom) || !isValidCurrencyCode(convertTo)) {
                System.err.println("Invalid currency code(s) entered. Please use standard 3-letter currency codes.");
                return;
            }

            // Construct API URL with the correct base
            String urlString = "https://api.frankfurter.app/latest?from=" + convertFrom;

            // Build HTTP request
            Request request = new Request.Builder()
                    .url(urlString)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Request failed with status code: " + response.code());
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    System.err.println("Error Body: " + errorBody);
                    return;
                }
                //response body is converted  to string 
                String stringResponse = response.body().string();

                // Parse JSON response
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(stringResponse);
                } catch (JSONException e) {
                    System.err.println("Failed to parse JSON response.");
                    e.printStackTrace();
                    return;
                }

                if (!jsonObject.has("rates")) {
                    System.err.println("Unexpected JSON structure: 'rates' key not found.");
                    return;
                }

                JSONObject ratesObject = jsonObject.getJSONObject("rates");

                if (!ratesObject.has(convertTo)) {
                    System.err.println("Invalid 'convertTo' currency code or rate not available: " + convertTo);
                    return;
                }

                // Get the exchange rate
                BigDecimal rate = ratesObject.getBigDecimal(convertTo);

                // Calculate the result
                BigDecimal result = rate.multiply(quantity);

                // Format and display the result
                System.out.println(quantity + " " + convertFrom + " = " + result + " " + convertTo);
            } catch (IOException e) {
                System.err.println("Network error occurred while making the API request.");
                e.printStackTrace();
            }
        } finally {
            // Shutdown OkHttpClient to clean up resources
            client.dispatcher().executorService().shutdown();
            client.connectionPool().evictAll();

        }
    }

    /**
     * Fetches and displays the list of supported currencies from the Frankfurter
     * API.
     *
     * @param client The OkHttpClient instance to use for the request.
     */
    private static void displaySupportedCurrencies(OkHttpClient client) {
        String urlString = "https://api.frankfurter.app/currencies";

        // Build HTTP request
        Request request = new Request.Builder()
                .url(urlString)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Failed to fetch supported currencies. Status code: " + response.code());
                return;
            }

            String stringResponse = response.body().string();
            JSONObject jsonObject = new JSONObject(stringResponse);

            System.out.println("\n=== Supported Currencies ===");
            for (String code : jsonObject.keySet()) {
                System.out.println(code + " - " + jsonObject.getString(code));
            }
            System.out.println("============================\n");
        } catch (IOException | JSONException e) {
            System.err.println("Error occurred while fetching supported currencies.");
            e.printStackTrace();
        }
    }

    /**
     * Validates if the provided currency code is in the correct format.
     * This is a basic validation checking if the code has exactly 3 uppercase
     * letters.
     *
     * @param code The currency code to validate.
     * @return True if valid, false otherwise.
     */
    private static boolean isValidCurrencyCode(String code) {
        return code != null && code.matches("^[A-Z]{3}$");
    }
}
