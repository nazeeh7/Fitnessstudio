package kickstart.verkaufsbereich.model;

import javax.money.MonetaryAmount;

import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Metric;
import org.salespointframework.quantity.Quantity;

import jakarta.persistence.Entity;

@Entity
public class Artikel extends  Product {
    private String beschreibung;
    private String bildUrl;
    private Quantity quantity;
    private int reorderThreshold;

	private Artikel() {
    }

    public Artikel(String name, String beschreibung, MonetaryAmount price, String bildUrl, Quantity quantity, int reorderThreshold ) {
        super(name, price, Metric.UNIT);
        this.beschreibung = beschreibung;
        this.bildUrl = bildUrl;
        this.quantity = quantity;
        this.reorderThreshold = reorderThreshold;
    }

	public Artikel(String name, String beschreibung, MonetaryAmount price, String bildUrl ) {
        super(name, price, Metric.UNIT);
        this.beschreibung = beschreibung;
        this.bildUrl = bildUrl;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getBildUrl() {
        return bildUrl;
    }

    public void setBildUrl(String bildUrl) {
        this.bildUrl = bildUrl;
    }
    public Quantity getQuantity() {
  		return quantity;
  	}

  	public void setQuantity(Quantity quantity) {
  		this.quantity = quantity;
  	}
  	
    public String getIdAsString() {
        return getId().toString();
    }
    public int getReorderThreshold() {
		return reorderThreshold;
	}

	public void setReorderThreshold(int reorderThreshold) {
		this.reorderThreshold = reorderThreshold;
	}
    public boolean needsReordering() {
        return this.getQuantity().isLessThan(Quantity.of(reorderThreshold));
    }

}