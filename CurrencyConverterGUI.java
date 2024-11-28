package com.example.currencyconverter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;

public class CurrencyConverterGUI extends JFrame {

    private final OkHttpClient client = new OkHttpClient();
    private JTextField amountField;
    private JComboBox<String> fromCurrencyCombo;
    private JComboBox<String> toCurrencyCombo;
    private JTextArea resultArea;

    public CurrencyConverterGUI() {
        setTitle("Currency Converter");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2));

        amountField = new JTextField();
        fromCurrencyCombo = new JComboBox<>();
        toCurrencyCombo = new JComboBox<>();
        resultArea = new JTextArea();

        JButton convertButton = new JButton("Convert");
        convertButton.addActionListener(new ConvertAction());

        // Add components to the frame
        add(new JLabel("Amount:"));
        add(amountField);
        add(new JLabel("From Currency:"));
        add(fromCurrencyCombo);
        add(new JLabel("To Currency:"));
        add(toCurrencyCombo);
        add(convertButton);
        add(new JScrollPane(resultArea));

        loadSupportedCurrencies();

        setVisible(true);
    }

    private void loadSupportedCurrencies() {
        String urlString = "https://api.frankfurter.app/currencies";
        Request request = new Request.Builder().url(urlString).get().build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String stringResponse = response.body().string();
                JSONObject jsonObject = new JSONObject(stringResponse);

                for (String code : jsonObject.keySet()) {
                    fromCurrencyCombo.addItem(code);
                    toCurrencyCombo.addItem(code);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch currencies.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ConvertAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String convertFrom = (String) fromCurrencyCombo.getSelectedItem();
            String convertTo = (String) toCurrencyCombo.getSelectedItem();
            BigDecimal quantity;

            try {
                quantity = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(CurrencyConverterGUI.this, "Invalid amount.");
                return;
            }

            if (convertFrom != null && convertTo != null) {
                String urlString = "https://api.frankfurter.app/latest?from=" + convertFrom;

                Request request = new Request.Builder().url(urlString).get().build();
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String stringResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(stringResponse);
                        BigDecimal rate = jsonObject.getJSONObject("rates").getBigDecimal(convertTo);
                        BigDecimal result = rate.multiply(quantity);
                        resultArea.setText(quantity + " " + convertFrom + " = " + result + " " + convertTo);
                    } else {
                        resultArea.setText("Failed to fetch conversion rate.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CurrencyConverterGUI::new);
    }
}
