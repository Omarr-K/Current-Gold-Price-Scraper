module com.example.GoldScraper {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;

    opens com.example.GoldScraper to javafx.fxml;
    exports com.example.GoldScraper;
}