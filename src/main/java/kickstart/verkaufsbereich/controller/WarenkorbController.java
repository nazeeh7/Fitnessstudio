package kickstart.verkaufsbereich.controller;

import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.quantity.Quantity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import kickstart.verkaufsbereich.model.Artikel;
import kickstart.verkaufsbereich.repository.ArtikelRepository;

import java.util.Iterator;
import java.util.Optional;

@Controller
@RequestMapping("/warenkorb")
public class WarenkorbController {

	private final ArtikelRepository artikelRepository;
	private final Cart cart;

	public WarenkorbController(ArtikelRepository artikelRepository, Cart cart, OrderManagement<Order> orderManagement) {
	    this.artikelRepository = artikelRepository;
	    this.cart = cart;
	}

	@PostMapping("/verkaufsbereich/{id}/hinzufuegen")
	public String artikelZumWarenkorbHinzufuegen(@PathVariable("id") ProductIdentifier id) {
	    Optional<Artikel> optionalArtikel = artikelRepository.findById(id);
	    if (optionalArtikel.isPresent()) {
		Artikel artikel = optionalArtikel.get();
		cart.addOrUpdateItem(artikel, Quantity.of(1));
	    }
	    return "redirect:/kundenArtikelliste";
	}

	@PostMapping("/verkaufsbereich/{id}/entfernen")
	public String artikelVomWarenkorbEntfernen(@PathVariable("id") ProductIdentifier id) {
	    Iterator<CartItem> iterator = cart.iterator();
	    while(iterator.hasNext()){
		CartItem item = iterator.next();
		if(item.getProduct().getId().equals(id)){
		    iterator.remove();
		    break;
		}
	    }
	    return "redirect:/warenkorb";
	}

	@GetMapping
	public String warenkorbAnzeigen(Model model) {
	    model.addAttribute("cart", cart);
	    return "warenkorb";
	}
}
