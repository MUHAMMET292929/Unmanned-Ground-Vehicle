package com.gurhan.xfit;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntrenmanlarController {
    @FXML
    private ListView<String> listKasGruplari;

    @FXML
    private ListView<String> listEgzersizler;

    @FXML
    private VBox vboxHareketler;

    @FXML
    private Button btnGeriGitc;

    @FXML
    private DatePicker datePickerTarih;

    @FXML
    private void handleAntrenmanOlustur(ActionEvent event) {
        // 1. TARİH VERİSİNİ AL (Kutudan çekiyoruz)
        if (datePickerTarih.getValue() == null) {
            System.out.println("Kirvem hata: Önce bir tarih seçmelisin!");
            // Buraya istersen bir uyarı penceresi (Alert) koyabilirsin
            return;
        }
        String secilenTarih = datePickerTarih.getValue().toString();

        // 2. VBOX İÇİNDEKİ HAREKETLERİ LİSTEYE TOPLA
        List<String> eklendiHareketler = new ArrayList<>();

        // VBox içindeki her bir elemanı (Label'ları) tek tek gezip yazılarını alıyoruz
        for (Node node : vboxHareketler.getChildren()) {
            if (node instanceof Label) {
                Label lbl = (Label) node;
                eklendiHareketler.add(lbl.getText());
            }
        }

        // 3. KONTROL: Liste boş mu?
        if (eklendiHareketler.isEmpty()) {
            System.out.println("Kirvem hata: Programda hiç hareket yok!");
            return;
        }

        // verileri mongoDB için hazırhale getirme
        Map<String, Object> antrenmanVerisi = new HashMap<>();
        antrenmanVerisi.put("tarih", secilenTarih);
        antrenmanVerisi.put("hareketler", eklendiHareketler);
        // Buraya login olan kullanıcının ID'sini de ekleyebilirsin:
        // antrenmanVerisi.put("user", "gurhan123");

        // 5. SONUÇ (Şimdilik konsola basıyoruz)
        System.out.println("--- DB'YE GİTMEYE HAZIR PAKET ---");
        System.out.println("Tarih: " + antrenmanVerisi.get("tarih"));
        System.out.println("Hareketler: " + antrenmanVerisi.get("hareketler"));

        // Arkadaşın MongoDB bağlantısını yapınca buraya şu gelecek:
        // databaseManager.save(antrenmanVerisi);
    }

    @FXML
    private void handleGeriGit(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gurhan/xfit/secondary.fxml"));
            Parent anaEkranRoot = loader.load();

            // 2. Mevcut Pencereyi (Stage) Al
            // Butona basılan event'i kullanarak mevcut pencereyi yakalarız.
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // 3. Yeni Sahneyi (Scene) Oluştur ve Yükle
            Scene anaEkranScene = new Scene(anaEkranRoot);

            // Fırsatın olursa Ana Ekranın CSS'ini de buradan yükleyebilirsin:
            // anaEkranScene.getStylesheets().add(getClass().getResource("/com/gurhan/xfit/anaekran.css").toExternalForm());

            // 4. Pencereyi Yeni Scene ile Güncelle ve Göster
            window.setScene(anaEkranScene);
            window.show();

        } catch (IOException e) {
            System.err.println("Ana Ekran yüklenemedi. FXML yolu veya dosya adını kontrol edin.");
            e.printStackTrace();
        }
    }

    // Her kas grubuna ait egzersizleri tutan map
    private Map<String, ObservableList<String>> egzersizMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Kas gruplarını ekle
        ObservableList<String> kasGruplari = FXCollections.observableArrayList(
                "Ön Kol", "Arka Kol", "Omuz", "Göğüs", "Sırt", "Karın", "Bacak"
        );
        listKasGruplari.setItems(kasGruplari);

// Kas gruplarına karşılık gelen hareketler
        Map<String, List<String>> hareketlerMap = new HashMap<>();
        hareketlerMap.put("Ön Kol", List.of("Barbell Curl", "Incline Dumbell Curl", "Preacher Curl", "Hammer Curl", "Reverse Biceps Curl"));
        hareketlerMap.put("Arka Kol", List.of("Pushdown", "Overhead Triceps Extension", "Kickback", "French Press", "Close Grip Bench Press", "Rope Pushdown"));
        hareketlerMap.put("Omuz", List.of("Overhead Press", "Front Raise", "Rear Delt Fly", "Face Pull", "Dumbell Lateral Raise"));
        hareketlerMap.put("Göğüs", List.of("Barbell Bench Press", "Dips", "Chest Press Machine", "Inclined Dumbbell Fly", "Push-Up", "Machine pectoral fly", "Cable crossover"));
        hareketlerMap.put("Sırt", List.of("Lat Pulldown", "Chin Up", "Seated Cable Row", "Dumbbell Row", "Back Extension", "Pull-Up"));
        hareketlerMap.put("Karın", List.of("Abdominal Crunch", "Legs Up Crunch", "Vertical Leg Crunch", "Lying Knee Sit Up Crunch", "Seated Knee Up", "Side Crunch", "Plank", "Side Plank"));
        hareketlerMap.put("Bacak", List.of("Squat", "Leg Press", "Standing Leg Curl", "Leg Extension", "Lunge", "Deadlift"));


// Soldaki ListView seçim değiştiğinde sağdaki ListView güncellensin
        listKasGruplari.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<String> secilenHareketler = hareketlerMap.getOrDefault(newVal, new ArrayList<>());
                listEgzersizler.setItems(FXCollections.observableArrayList(secilenHareketler));
            }
        });
        listEgzersizler.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String secilenHareket = listEgzersizler.getSelectionModel().getSelectedItem();

                if (secilenHareket != null) {
                    Label label = new Label(secilenHareket);
                    label.setStyle("-fx-font-size: 16px; " + "-fx-padding: 5px; " + "-fx-text-fill: #39FF14;");

                    label.setOnMouseClicked(innerEvent -> {

                        if (event.getClickCount() == 2) {
                            vboxHareketler.getChildren().remove(label);
                        }
                    });

                    vboxHareketler.getChildren().add(label);
                }
            }
        });
    }
}
