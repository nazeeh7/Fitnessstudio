package kickstart.verkaufsbereich.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.useraccount.UserAccountManagement;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import kickstart.verkaufsbereich.model.Artikel;
import kickstart.verkaufsbereich.model.ReorderList;
import kickstart.verkaufsbereich.repository.ArtikelRepository;
import kickstart.verkaufsbereich.repository.InventoryItemRepository;
import kickstart.verkaufsbereich.repository.ReorderListRepository;

@Controller
public class NachbestellungController {

	private final ReorderListRepository reorderListRepository;
	private final InventoryItemRepository inventoryItemRepository;
	private final ArtikelRepository artikelRepository;

	public NachbestellungController(ReorderListRepository reorderListRepository,
			InventoryItemRepository inventoryItemRepository, ArtikelRepository artikelRepository

			, UserAccountManagement userAccountManagement) {
		this.reorderListRepository = reorderListRepository;
		this.inventoryItemRepository = inventoryItemRepository;
		this.artikelRepository = artikelRepository;
	}

	private Optional<UniqueInventoryItem> findInventoryItem(Artikel artikel) {
		return StreamSupport.stream(inventoryItemRepository.findAll().spliterator(), false)
				.filter(item -> item.getProduct().getId().equals(artikel.getId())).findFirst();
	}

	@GetMapping("/nachbestellung")
	public String alleNachbestellungen(Model model) {
		model.addAttribute("nachbestellung", reorderListRepository.findAll());
		return "nachbestellungsliste";
	}

	public void checkReorderList() {

		// Loop through all the articles in the repository
		for (Artikel artikel : artikelRepository.findAll()) {
			// Check if the article needs reordering
			if (artikel.needsReordering()) {
				// Check if the reorder list already contains the article
				Optional<ReorderList> existingReorderLists = reorderListRepository.findByArtikel(artikel);
				if (existingReorderLists.isEmpty()) {
					// If not, create a new ReorderList object and save it to the repository
					ReorderList reorderList = new ReorderList(artikel);
					reorderListRepository.save(reorderList);
				}
			}
		}
	}

	@GetMapping("/verkaufsbereich/checkReorderList")
	public String checkReorderList(Model model) {
		List<Artikel> reorderNeeded = new ArrayList<>();

		for (ReorderList reorderItem : reorderListRepository.findAll()) {
			Artikel artikel = reorderItem.getArtikel();
			Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
			if (inventoryItemOptional.isPresent()) {
				UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
				if (inventoryItem.getQuantity().getAmount().longValueExact() < artikel.getReorderThreshold()) {
					reorderNeeded.add(artikel);
				}
			}
		}

		model.addAttribute("reorderNeeded", reorderNeeded);
		return "nachbestellungsliste";
	}

	public boolean isReorderNeeded() {
	    List<Artikel> reorderNeeded = new ArrayList<>();

	    for (ReorderList reorderItem : reorderListRepository.findAll()) {
	        Artikel artikel = reorderItem.getArtikel();
	        Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
	        if (inventoryItemOptional.isPresent()) {
	            UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
	            if (inventoryItem.getQuantity().getAmount().longValueExact() < artikel.getReorderThreshold()) {
	                reorderNeeded.add(artikel);
	            }
	        }
	    }
	    return !reorderNeeded.isEmpty();
	}


}
