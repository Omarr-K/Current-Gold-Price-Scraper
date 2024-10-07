module com.example.selflearning {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;

    opens com.example.GoldScraper to javafx.fxml;
    exports com.example.GoldScraper;
}