package kickstart.verkaufsbereich.model;

import javax.money.Monetary;
import org.salespointframework.core.DataInitializer;
import org.springframework.stereotype.Component;

import kickstart.verkaufsbereich.repository.ArtikelRepository;
import kickstart.verkaufsbereich.repository.InventoryItemRepository;
import kickstart.verkaufsbereich.repository.ReorderListRepository;

import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.quantity.Quantity;

import java.util.Arrays;

@Component
public class ArtikelDataInitializer implements DataInitializer {

    private final ArtikelRepository artikelRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ReorderListRepository reorderListRepository;

    public ArtikelDataInitializer(ArtikelRepository artikelRepository, InventoryItemRepository inventoryItemRepository, ReorderListRepository reorderListRepository) {
        this.artikelRepository = artikelRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.reorderListRepository = reorderListRepository;
    }

    @Override
    public void initialize() {
        if(artikelRepository.findAll().iterator().hasNext()) return;

        Artikel artikel1 = new Artikel("Protein Shake", "Protein Shake für nach dem Training", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(19.99).create(), "/images/protein_shake.jpg",Quantity.of(12),10);
        Artikel artikel2 = new Artikel("Fitness Band", "Elastisches Fitness Band für Workouts", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(9.99).create(), "/images/fitness_band.png",Quantity.of(10),10);
        Artikel artikel3 = new Artikel("Yoga Mat", "Komfortable Yoga Matte", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(29.99).create(), "/images/yoga_mat.png",Quantity.of(20),20);
        Artikel artikel4 = new Artikel("Laufschuhe", "Hochwertige Laufschuhe für optimale Performance", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(79.99).create(), "/images/laufschuhe.png",Quantity.of(6),5);
        Artikel artikel5 = new Artikel("Hanteln", "Set von 2 Hanteln für Krafttraining", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(39.99).create(), "/images/hanteln.png",Quantity.of(15),15);
        Artikel artikel6 = new Artikel("Trainingshandschuhe", "Bequeme und robuste Trainingshandschuhe", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(14.99).create(), "/images/handschuhe.png",Quantity.of(10),10);
        Artikel artikel7 = new Artikel("Trainingsmatte", "Dämpfende Trainingsmatte für Zuhause", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(24.99).create(), "/images/matte.png",Quantity.of(12),10);
        Artikel artikel8 = new Artikel("Balance Ball", "Balance Ball für ein vielseitiges Training", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(19.99).create(), "/images/balanceball.png",Quantity.of(31),30);
        Artikel artikel9 = new Artikel("Fahrradhelm", "Sicherer und leichter Fahrradhelm", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(49.99).create(), "/images/helm.png",Quantity.of(10),10);
        Artikel artikel10 = new Artikel("Wasserflasche", "Praktische Wasserflasche für das Training", Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(9.99).create(), "/images/flasche.jpg",Quantity.of(52),50);
        artikelRepository.saveAll(Arrays.asList(artikel1, artikel2, artikel3, artikel4, artikel5, artikel6, artikel7, artikel8, artikel9, artikel10));

        artikelRepository.findAll().forEach(artikel -> {
            UniqueInventoryItem item = new UniqueInventoryItem(artikel, artikel.getQuantity());
            inventoryItemRepository.save(item);
            
            ReorderList reorderList = new ReorderList(artikel);
            if (artikel.needsReordering()) {
                reorderListRepository.save(reorderList);
            }
        });

    }
}



