package com.example.GoldScraper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    @FXML private Label Currentprice; // Current price of gold
    @FXML private Label Uservalue; // Amount of gold user has in selected currency (After input)
    @FXML private TextField UserAmount; // Amount of gold user has in grams (input)
    @FXML private Button UserButton; // Button to calculate UserAmount
    private double pricePerGramInAED; // Calculated UserAmount

    public void initialize() {
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
                        double price = Double.parseDouble(cleanedPrice);

                        // Convert the price to grams and AED
                        pricePerGramInAED = (price * 3.67) / 31.1034768;

                        // Update the Currentprice label with the gold price in AED (UI updates must be done on the UI thread)
                        Platform.runLater(() -> {
                            Currentprice.setText(String.format("Current Price of Gold per Gram in AED: %.2f", pricePerGramInAED));
                        });
                        break; // Exit the loop once the price is found and processed
                    }
                }
            }
        }).start(); // Start the background thread
    }

    @FXML private void displayValue() {
        // Get the number of grams input by the user from the TextField (UserAmount)
        // Convert the string value to a double for calculations
        double userGrams = Double.parseDouble(UserAmount.getText());
        Uservalue.setText(String.format("You have AED %.2f in gold", userGrams*pricePerGramInAED)); //Display value
    }

}
