package com.example.GoldScraper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoldPriceScraper extends Application {

    @FXML
    private Label Currentprice; // Current price of gold
    @FXML
    private Label Uservalue; // Amount of gold user has in selected currency (After input)
    @FXML
    private TextField UserAmount; // Amount of gold user has in grams (input)
    @FXML
    private Button UserButton; // Button to calculate UserAmount
    @FXML
    private ComboBox<String> CurrencyCombo; //Lets user pick currency
    @FXML
    private Label LastUpdatedLabel; // Label for last updated time
    private double pricePerGramInAED; // Calculated UserAmount in AED
    private double pricePerGramInCAD; // Calculated UserAmount in CAD
    private double pricePerGramInUSD; // Calculated UserAmount in USD
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Scheduler for periodic refresh

    private String userCurrency;

    public void initialize() {
        CurrencyCombo.getItems().addAll("AED", "USD", "CAD"); // add currency options to the ComboBox
        CurrencyCombo.setValue("AED");  // Set AED as the default selected currency
        CurrencyCombo.setVisible(false);
        CurrencyCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            // Call displayValue method to update the price based on the new currency
            updateDisplayedPrice();
        });
        UserButton.setOnAction(event -> displayValue()); // Initialize action
        UserButton.setOnAction(event -> displayValue()); // Initialize action
        scheduler.scheduleAtFixedRate(() -> {
            try {
                fetchGoldPrice();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    private void parsevalues() {
        double grams = Double.parseDouble(UserAmount.getText()); // Parse what the user enters for input
    }

    public static void main(String[] args) {
        launch(args); // Launch JavaFX application
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GoldPriceScraper.fxml"));
        Parent root = loader.load();

        // Set up the scene and stage
        primaryStage.setTitle("Omar's Gold Exchange");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // Get the controller and call fetchGoldPrice after the FXML is loaded
        GoldPriceScraper controller = loader.getController();
        controller.fetchGoldPrice();  // Call the method after FXML is initialized
    }

    // Method to fetch gold price from the website
    // Method to fetch gold price from the website
    public void fetchGoldPrice() throws IOException {
        // Start a new thread to fetch the gold price in the background to avoid blocking the UI
        new Thread(() -> {
            // URL for USD gold price, AED gold price, and conversion rate
            String urlUSD = "https://www.cnbc.com/quotes/XAU=";
            String urlAED = "https://dubaicityofgold.com/";
            String urlUSDToCAD = "https://www.xe.com/currencyconverter/convert/?Amount=1&From=USD&To=CAD";

            Document docUSD = null;
            Document docAED = null;
            Document docConversion = null;

            // Fetch the USD gold price
            try {
                docUSD = Jsoup.connect(urlUSD).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36").get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Fetch the AED gold price
            try {
                docAED = Jsoup.connect(urlAED).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36").get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Fetch the USD to CAD conversion rate
            try {
                docConversion = Jsoup.connect(urlUSDToCAD).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36").get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Parse the USD gold price from the fetched document
            Elements scriptTags = docUSD.getElementsByTag("script");
            Pattern pricePattern = Pattern.compile("\"price\":\"([\\d,\\.]+)\"");
            for (Element script : scriptTags) {
                if (script.data().contains("price")) {
                    Matcher matcher = pricePattern.matcher(script.data());
                    if (matcher.find()) {
                        String priceStr = matcher.group(1).replace(",", "");
                        double pricePerOunceInUSD = Double.parseDouble(priceStr);
                        pricePerGramInUSD = pricePerOunceInUSD / 31.1034768; // Convert to grams
                        break; // Exit the loop once the price is found
                    }
                }
            }

            // Parse the AED gold price from the second website
            Elements priceElementsAED = docAED.select("ul.goldtable li");
            for (Element element : priceElementsAED) {
                if (element.text().contains("24K")) {
                    Pattern patternAED = Pattern.compile("AED\\s*(\\d+\\.\\d+)");
                    Matcher matcherAED = patternAED.matcher(element.text());
                    if (matcherAED.find()) {
                        String priceStrAED = matcherAED.group(1);
                        double pricePerGramInAEDFromSite = Double.parseDouble(priceStrAED);
                        pricePerGramInAED = pricePerGramInAEDFromSite;
                        break; // Exit the loop once the AED price is found
                    }
                }
            }

            // Parse the USD to CAD conversion rate
            Elements conversionElements = docConversion.select("p.sc-63d8b7e3-1.bMdPIi");

// Extract the conversion rate text and clean it
            String conversionRateStr = conversionElements.first().text().replaceAll("[^\\d.]", "");

// Convert the cleaned string to a double
            double conversionRate = Double.parseDouble(conversionRateStr);

// Calculate the price in CAD
            pricePerGramInCAD = pricePerGramInUSD * conversionRate;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a z");
            dateFormat.setTimeZone(TimeZone.getDefault()); // Set to the local timezone
            String lastUpdatedTime = dateFormat.format(new Date());

            // Update the UI with the fetched prices
            Platform.runLater(() -> {
                updateDisplayedPrice();
                LastUpdatedLabel.setText("Last Updated: " + lastUpdatedTime);
                CurrencyCombo.setVisible(true);
            });
        }).start(); // Start the background thread
    }

    private void updateDisplayedPrice() {
        // Get the selected currency from the ComboBox
        String selectedCurrency = CurrencyCombo.getValue();

        // Update the Currentprice label based on the selected currency
        Platform.runLater(() -> {
            switch (selectedCurrency) {
                case "AED":
                    Currentprice.setText(String.format("Current Price of Gold per Gram: %.2f", pricePerGramInAED));
                    break;
                case "USD":
                    Currentprice.setText(String.format("Current Price of Gold per Gram: %.2f", pricePerGramInUSD));
                    break;
                case "CAD":
                    Currentprice.setText(String.format("Current Price of Gold per Gram: %.2f", pricePerGramInCAD));
                    break;
                default:
                    Currentprice.setText("Error: Invalid currency selected!");
            }
        });
    }

    @FXML
    private void displayValue() {
        // Get the user input for the amount of gold in grams
        double userGrams = Double.parseDouble(UserAmount.getText());

        // Get the selected currency from the ComboBox
        String selectedCurrency = CurrencyCombo.getValue();

        // Calculate and display the value based on the selected currency
        Platform.runLater(() -> {
            if (selectedCurrency.equals("AED")) {
                Uservalue.setText(String.format("You have AED %.2f in Gold", userGrams * pricePerGramInAED));
            } else if (selectedCurrency.equals("USD")) {
                Uservalue.setText(String.format("You have USD %.2f in Gold", userGrams * (pricePerGramInUSD))); // USD equivalent
            } else if (selectedCurrency.equals("CAD")) {
                Uservalue.setText(String.format("You have CAD %.2f in Gold", userGrams * (pricePerGramInCAD))); // CAD equivalent
            }
        });
    }
}