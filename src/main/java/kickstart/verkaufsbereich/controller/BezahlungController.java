package kickstart.verkaufsbereich.controller;

import org.salespointframework.catalog.Product.ProductIdentifier;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.order.Cart;
import org.salespointframework.order.CartItem;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.payment.Cash;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kickstart.verkaufsbereich.model.Artikel;
import kickstart.verkaufsbereich.repository.ArtikelRepository;
import kickstart.verkaufsbereich.repository.InventoryItemRepository;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/bezahlung")
public class BezahlungController {

    private final ArtikelRepository artikelRepository;
    private final Cart cart;
    private final OrderManagement<Order> orderManagement;
    private final NachbestellungController nachbestellungController;
    private final InventoryItemRepository inventoryItemRepository;
    @Autowired
    private UserAccountManagement userAccountManagement;
    

    public BezahlungController(ArtikelRepository artikelRepository
        		,Cart cart 
                ,OrderManagement<Order> orderManagement
                , UserAccountManagement userAccountManagement
                ,NachbestellungController nachbestellungController
                ,InventoryItemRepository inventoryItemRepository) {
            this.artikelRepository = artikelRepository;
            this.cart = cart;
            this.orderManagement = orderManagement;
            this.userAccountManagement = userAccountManagement;
            this.nachbestellungController = nachbestellungController;
            this.inventoryItemRepository = inventoryItemRepository;
    }
    @PostMapping("/verkaufsbereich/{id}/sofortkauf")
    public String sofortKaufen(@PathVariable("id") ProductIdentifier id , HttpServletRequest request) {
    	 HttpSession session = request.getSession();
        Optional<Artikel> optionalArtikel = artikelRepository.findById(id);
        if (optionalArtikel.isPresent()) {
            Artikel artikel = optionalArtikel.get();
            Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
            UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
            if (inventoryItemOptional.isPresent()) {
            inventoryItem = inventoryItemOptional.get();
            inventoryItem.decreaseQuantity(Quantity.of(1));
            artikel.setQuantity(inventoryItem.getQuantity());

            // Save updated Artikel and InventoryItem
            artikelRepository.save(artikel);
            inventoryItemRepository.save(inventoryItem);
            }
        
            Optional<UserAccount> userAccountOptional = userAccountManagement.findByUsername("boss");
            if (userAccountOptional.isPresent()) {
                UserAccount userAccount = userAccountOptional.get();
                Order order = new Order(userAccount, Cash.CASH);
                cart.addItemsTo(order);
                orderManagement.save(order);
                orderManagement.save(order);
                session.setAttribute("cart", cart);
                nachbestellungController.checkReorderList();
                return "redirect:/bezahlung";
            } else {
                System.out.println("Error: Benutzeraccount nicht gefunden.");
                return "redirect:/warenkorb";
            }
        }else {
            System.out.println("Error: Artikel nicht gefunden.");
            return "redirect:/warenkorb";
        }
    }
       
    @PostMapping("/verkaufsbereich/{id}/einzelArtikelkaufen")
    public String einzelArtikelkaufen(@PathVariable("id") ProductIdentifier id, HttpServletRequest request) {
        Optional<Artikel> optionalArtikel = artikelRepository.findById(id);
        HttpSession session = request.getSession();
        Boolean flag = (Boolean) session.getAttribute("flag");
        if (optionalArtikel.isPresent()) {
            Artikel artikel = optionalArtikel.get();
            Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);
            UniqueInventoryItem inventoryItem = inventoryItemOptional.get();

            // Use Iterator to avoid ConcurrentModificationException
            Iterator<CartItem> iterator = cart.iterator();
            while (iterator.hasNext()) {
                CartItem cartItem = iterator.next();

                // Check if the current cart item matches the product id in the path variable
                if (cartItem.getProduct().getId().equals(id)) {
                    // Get Inventory Item
                    inventoryItemOptional = findInventoryItem((Artikel) cartItem.getProduct());

                    if (inventoryItemOptional.isPresent()) {
                        inventoryItem = inventoryItemOptional.get();
                        inventoryItem.decreaseQuantity(cartItem.getQuantity());
                        artikel.setQuantity(inventoryItem.getQuantity());

                        // Save updated Artikel and InventoryItem
                        artikelRepository.save(artikel);
                        inventoryItemRepository.save(inventoryItem);

                        // Removed clearing cart and session flag management
                    }
                }
            }
            Optional<UserAccount> userAccountOptional = userAccountManagement.findByUsername("boss");
            if (userAccountOptional.isPresent()) {
                UserAccount userAccount = userAccountOptional.get();
                Order order = new Order(userAccount, Cash.CASH);

                // Add items to order but do not remove them from cart
                cart.addItemsTo(order);

                // Save order and cart in session for later use in abschliessen method
                orderManagement.save(order);
                session.setAttribute("cart", cart);

                nachbestellungController.checkReorderList();
                return "redirect:/bezahlung";
            } else {
                System.out.println("Error: Benutzeraccount nicht gefunden.");
                return "redirect:/warenkorb";
            }
        } else {
            System.out.println("Error: Artikel nicht gefunden.");
            return "redirect:/warenkorb";
        }
    }

    @PostMapping("/kaufen")
    public String kaufen(HttpServletRequest request) {
        Optional<UserAccount> userAccountOptional = userAccountManagement.findByUsername("boss");

        if (!cart.isEmpty() && userAccountOptional.isPresent()) {
            UserAccount userAccount = userAccountOptional.get();

            Order order = new Order(userAccount, Cash.CASH);

            // Use Iterator to avoid ConcurrentModificationException
            Iterator<CartItem> iterator = cart.iterator();
            while (iterator.hasNext()) {
                CartItem cartItem = iterator.next();
                Optional<Artikel> optionalArtikel = artikelRepository.findById((ProductIdentifier) cartItem.getProduct().getId());

                if (optionalArtikel.isPresent()) {
                    Artikel artikel = optionalArtikel.get();
                    Optional<UniqueInventoryItem> inventoryItemOptional = findInventoryItem(artikel);

                    if (inventoryItemOptional.isPresent()) {
                        UniqueInventoryItem inventoryItem = inventoryItemOptional.get();
                        inventoryItem.decreaseQuantity(cartItem.getQuantity());
                        artikel.setQuantity(inventoryItem.getQuantity());

                        // Save updated Artikel and InventoryItem
                        artikelRepository.save(artikel);
                        inventoryItemRepository.save(inventoryItem);
                    }
                }
            }

            cart.addItemsTo(order);
            orderManagement.save(order);
        
            // Save cart in session for later use in abschliessen method
            HttpSession session = request.getSession();
            session.setAttribute("cart", cart);
            nachbestellungController.checkReorderList();
            return "redirect:/bezahlung";
        } else {
            System.out.println("Error: Benutzeraccount nicht gefunden oder Warenkorb ist leer.");
            return "redirect:/warenkorb";
        }
    }



    // Method to get Inventory Item for a given Artikel
    private Optional<UniqueInventoryItem> findInventoryItem(Artikel artikel) {
        return StreamSupport.stream(inventoryItemRepository.findAll().spliterator(), false)
                .filter(item -> item.getProduct().getId().equals(artikel.getId())).findFirst();
    }


    @PostMapping("/abschliessen")
    public String abschliessen(@RequestParam("name") String name, 
                               @RequestParam("cardNumber") String cardNumber,
                               @RequestParam("expiryDate") String expiryDate, 
                               @RequestParam("cvv") String cvv,
                               Model model,
                               HttpServletRequest request) {

	   if (name.isEmpty() || cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
	        return "bezahlung";
	    }
	    // Überprüfen Sie, ob der Name nur Buchstaben enthält und nicht leer ist.
	   else if (!name.matches("^[a-zA-ZäöüÄÖÜß ]+$")) {
	        model.addAttribute("errorMessage", "Der Name darf nur Buchstaben enthalten und darf nicht leer sein.");
	        return "bezahlung";
	    }

        // Überprüfen Sie, ob die Kartennummer nur aus Ziffern besteht und 16 Ziffern lang ist.
	   else if (!cardNumber.matches("\\d{16}")) {
            model.addAttribute("errorMessage", "Kartennummer muss 16 Ziffern enthalten.");
            return "bezahlung";
        }

        // Überprüfen Sie, ob das Ablaufdatum das Format MM/YY hat.
	   else if (!expiryDate.matches("(0[1-9]|1[0-2])\\/[0-9]{2}")) {
            model.addAttribute("errorMessage", "Ablaufdatum muss das Format MM/YY haben.");
            return "bezahlung";
        }

        // Überprüfen Sie, ob die CVV nur aus Ziffern besteht und 3 Ziffern lang ist.
	   else if (!cvv.matches("\\d{3}")) {
            model.addAttribute("errorMessage", "CVV muss 3 Ziffern enthalten.");
            return "bezahlung";
        }
       HttpSession session = request.getSession();
       Cart cart = (Cart) session.getAttribute("cart");
       cart.clear();

       return "bezahilungconfirmation";
   }


    @GetMapping
    public String showPaymentPage() {
        return "bezahlung";
    }
    
}
