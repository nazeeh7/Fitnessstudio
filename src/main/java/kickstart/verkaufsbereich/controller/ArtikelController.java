package kickstart.verkaufsbereich.controller;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.inventory.UniqueInventoryItem;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import kickstart.verkaufsbereich.model.Artikel;
import kickstart.verkaufsbereich.model.ReorderList;
import kickstart.verkaufsbereich.repository.ArtikelRepository;
import kickstart.verkaufsbereich.repository.InventoryItemRepository;
import kickstart.verkaufsbereich.repository.ReorderListRepository;

@Controller
public class ArtikelController {

	private final ArtikelRepository artikelRepository;
	private final InventoryItemRepository inventoryItemRepository;
	private final ReorderListRepository reorderListRepository;
	private final MonetaryAmount defaultPrice;
	private final NachbestellungController nachbestellungController;

	ArtikelController(ArtikelRepository artikelRepository, InventoryItemRepository inventoryItemRepository
			,ReorderListRepository reorderListRepository, NachbestellungController nachbestellungController) {
		this.artikelRepository = artikelRepository;
		this.inventoryItemRepository = inventoryItemRepository;;
		this.reorderListRepository = reorderListRepository;
		this.nachbestellungController = nachbestellungController;
		this.defaultPrice = Monetary.getDefaultAmountFactory().setCurrency("EUR").setNumber(0).create();
	}

	private Optional<UniqueInventoryItem> findInventoryItem(Artikel artikel) {
		return StreamSupport.stream(inventoryItemRepository.findAll().spliterator(), false)
				.filter(item -> item.getProduct().getId().equals(artikel.getId())).findFirst();
	}

	@GetMapping("/artikelListe")
	public String alleArtikel(Model model) {
		List<Artikel> artikelListe = new ArrayList<Artikel>();
		for (Artikel artikel : artikelRepository.findAll()) {
			artikelListe.add(artikel);
		}
		model.addAttribute("artikel", artikelListe);
		return "artikelListe";
	}
	
	@GetMapping("/kundenArtikelliste")
	public String kundenAlleArtikel(Model model) {
		List<Artikel> artikelListe = new ArrayList<Artikel>();
		for (Artikel artikel : artikelRepository.findAll()) {
			artikelListe.add(artikel);
		}
		model.addAttribute("artikel", artikelListe);
		return "kundenArtikelliste";
	}

	@GetMapping("/verkaufsbereich/neu")
	public String artikelForm(Model model) {
		model.addAttribute("artikel", new Artikel("Neuer Artikel", "", this.defaultPrice, ""));
		return "artikelForm";
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(MonetaryAmount.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) {
				MonetaryAmount value = Monetary.getDefaultAmountFactory().setCurrency("EUR")
						.setNumber(new BigDecimal(text)).create();
				setValue(value);
			}
		});
	}

	@PostMapping("/verkaufsbereich/neu")
	public String artikelHinzufuegen(@ModelAttribute Artikel artikel, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors() || artikel.getName() == null || artikel.getName().isEmpty()
				|| artikel.getPrice() == null) {
			model.addAttribute("artikel", artikel);
			return "artikelForm";
		}

		artikel = new Artikel(artikel.getName(), artikel.getBeschreibung(), artikel.getPrice(), artikel.getBildUrl(),
				artikel.getQuantity(), artikel.getReorderThreshold());
		artikelRepository.save(artikel);

		UniqueInventoryItem inventoryItem = new UniqueInventoryItem(artikel,artikel.getQuantity());
		inventoryItemRepository.save(inventoryItem);
		nachbestellungController.checkReorderList();
//        ReorderList reorderList = new ReorderList(artikel);
//        if (artikel.needsReordering()) {
//            reorderListRepository.save(reorderList);
//        }

		return "redirect:/artikelListe";
	}

	@GetMapping("/verkaufsbereich/{id}")
	public String artikelDetails(@PathVariable("id") ProductIdentifier id, Model model) {
		Optional<Artikel> artikelOptional = artikelRepository.findById(id);

		if (!artikelOptional.isPresent()) {
			return "redirect:/artikelListe";
		}
		Artikel artikel = artikelOptional.get();
		model.addAttribute("artikel", artikel);

		Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
		if (inventoryItemOptional.isPresent()) {
			UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
			model.addAttribute("quantity", inventoryItem.getQuantity());
		} else {
			model.addAttribute("quantity", 0);
		}

		return "artikelDetails";
	}

	@GetMapping("/verkaufsbereich/{id}/bearbeiten")
	public String artikelBearbeitenForm(@PathVariable("id") ProductIdentifier id, Model model) {
		Optional<Artikel> optionalArtikel = artikelRepository.findById(id);
		if (optionalArtikel.isPresent()) {
			Artikel artikel = optionalArtikel.get();
			model.addAttribute("artikel", artikel);
			return "ArtikleBearbeiten";
		} else {
			return "redirect:/artikel";
		}
	}

	@PostMapping("/verkaufsbereich/{id}/bearbeiten")
	public String artikelBearbeiten(@PathVariable("id") ProductIdentifier id, @Valid Artikel artikel,
				BindingResult bindingResult, Model model) {
			if (bindingResult.hasErrors()) {
				model.addAttribute("artikel", artikel);
				return "ArtikleBearbeiten";
			} else {
				Optional<Artikel> optionalArtikel = artikelRepository.findById(id);
				if (optionalArtikel.isPresent()) {
					Artikel bearbeiteterArtikel = optionalArtikel.get();
					bearbeiteterArtikel.setName(artikel.getName());
					bearbeiteterArtikel.setBeschreibung(artikel.getBeschreibung());
					bearbeiteterArtikel.setPrice(artikel.getPrice());
					bearbeiteterArtikel.setBildUrl(artikel.getBildUrl());
					bearbeiteterArtikel.setQuantity(artikel.getQuantity());
					bearbeiteterArtikel.setReorderThreshold(artikel.getReorderThreshold());
					artikelRepository.save(bearbeiteterArtikel);
					
					Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(bearbeiteterArtikel);
					if (inventoryItemOptional.isPresent()) {
					    UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
					    inventoryItemRepository.delete(inventoryItem);

					    UniqueInventoryItem newInventoryItem = new UniqueInventoryItem(bearbeiteterArtikel, artikel.getQuantity());
					    inventoryItemRepository.save(newInventoryItem);
					    
					    // Überprüfen, ob der Artikel bereits in der Nachbestellungsliste ist
					    Optional<ReorderList> existingReorderListOpt = reorderListRepository.findByArtikelId(bearbeiteterArtikel.getId());
					    
					    if (existingReorderListOpt.isPresent()) {
					        // Wenn der Artikel bereits in der Nachbestellungsliste ist, aktualisieren Sie ihn
					        ReorderList existingReorderList = existingReorderListOpt.get();
					        if (bearbeiteterArtikel.needsReordering()) {
					            existingReorderList.setArtikel(bearbeiteterArtikel);
					            reorderListRepository.save(existingReorderList);
					        }
					    } else {
					    	nachbestellungController.checkReorderList();

					    }
					}
				}

			}
			return "redirect:/artikelListe";
			
		}


	@GetMapping("/verkaufsbereich/{id}/loeschen")
	public String artikelLoeschen(@PathVariable("id") ProductIdentifier id) {
	    
	    Optional<Artikel> optionalArtikel = artikelRepository.findById(id);
	    if (optionalArtikel.isPresent()) {
	        Artikel artikel = optionalArtikel.get();
	        
	        // Find the associated ReorderList and delete it
	        List<ReorderList> reorderLists = reorderListRepository.findAllByArtikel(artikel);
	        for (ReorderList reorderList : reorderLists) {
	            reorderListRepository.delete(reorderList);
	        }

	        // Find the associated UniqueInventoryItem and delete it
	        Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
	        if (inventoryItemOptional.isPresent()) {
	            inventoryItemRepository.delete(inventoryItemOptional.get());
	        }
	        // Delete the article itself
	        artikelRepository.delete(artikel);
	    }

	    return "redirect:/artikelListe";
	}


	public void decreaseQuantity(Artikel artikel, long purchasedQuantity) {
		Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
		if (inventoryItemOptional.isPresent()) {
			UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
			long currentQuantity = inventoryItem.getQuantity().getAmount().longValueExact();
			if (currentQuantity >= purchasedQuantity) {
				long newQuantity = currentQuantity - purchasedQuantity;
				UniqueInventoryItem newInventoryItem = new UniqueInventoryItem(artikel, Quantity.of(newQuantity));
				inventoryItemRepository.save(newInventoryItem);
			} else {
				throw new IllegalArgumentException("Not enough items in stock.");
			}
		} else {
			throw new IllegalArgumentException("Article not found in inventory.");
		}
	}

}
