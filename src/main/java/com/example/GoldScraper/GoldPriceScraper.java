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
    private double pricePerGramInAED; // Calculated UserAmount in AED
    private double pricePerGramInCAD; // Calculated UserAmount in CAD
    private double pricePerGramInUSD; // Calculated UserAmount in USD
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
            // URL of the source page where the gold price is being fetched from
            String url = "https://www.cnbc.com/quotes/XAU=";

            Document doc = null; // Initialize Document to hold the parsed HTML content

            try {
                // Fetch the web page and return the HTML document using Jsoup
                doc = Jsoup.connect(url)
                        // Set a User-Agent header to simulate a browser request (safety first)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                        .get();
            } catch (IOException e) {
                // If there's an issue fetching the page, print the error stack trace
                e.printStackTrace();
            }

            // Check if the document is null, which means the fetch failed
            if (doc == null) {
                // Update the UI to notify the user of the error (must be done on the UI thread)
                Platform.runLater(() -> Currentprice.setText("Error fetching price!"));
                return; // Exit the method early since fetching failed
            }

            // Get all <script> tags from the HTML document
            Elements scriptTags = doc.getElementsByTag("script");

            // Define a regex pattern to find the "price":"some-value" in the script content
            Pattern pricePattern = Pattern.compile("\"price\":\"([\\d,\\.]+)\"");

            // Loop through each <script> tag to find the one containing the gold price
            for (Element script : scriptTags) {
                // If the script contains the word "price", proceed to extract the value
                if (script.data().contains("price")) {
                    // Apply the regex pattern to the script's content
                    Matcher matcher = pricePattern.matcher(script.data());

                    // If a match is found for the price pattern
                    if (matcher.find()) {
                        // Extract the matched price string (group 1 of the regex)
                        String priceStr = matcher.group(1);

                        // Clean the price by removing commas (if any)
                        String cleanedPrice = priceStr.replace(",", "");

                        // Convert the cleaned price string to a double
                        double pricePerOunceInUSD = Double.parseDouble(cleanedPrice);
                        pricePerGramInAED = (pricePerOunceInUSD * 3.67) / 31.1034768;
                        pricePerGramInCAD = (pricePerOunceInUSD * 1.36) / 31.1034768;
                        pricePerGramInUSD = pricePerOunceInUSD / 31.1034768;

                        // Get the selected currency from the ComboBox
                        String selectedCurrency = CurrencyCombo.getValue();

                        // Update the Currentprice label with the gold price in the selected currency
                        Platform.runLater(() -> {
                            Currentprice.setText(String.format("Current Price of Gold per Gram: %.2f", pricePerGramInAED));
                            CurrencyCombo.setVisible(true);
                        });

                        break; // Exit the loop once the price is found and processed
                    }
                }
            }
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