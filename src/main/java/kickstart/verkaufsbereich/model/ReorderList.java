package kickstart.verkaufsbereich.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import org.salespointframework.quantity.Quantity;

import jakarta.persistence.Entity;

@Entity
public class ReorderList {
    
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private Artikel artikel;

    private int reorderThreshold;

    protected ReorderList() { }

    public ReorderList(Artikel artikel) {
        this.artikel = artikel;
    }

    public Long getId() {
        return id;
    }

    public Artikel getArtikel() {
        return artikel;
    }

    public void setArtikel(Artikel artikel) {
        this.artikel = artikel;
    }

    public int getReorderThreshold() {
		return reorderThreshold;
	}

	public void setReorderThreshold() {
		this.reorderThreshold = artikel.getReorderThreshold();
	}

	public boolean needsReordering() {
        return artikel.getQuantity().isLessThan(Quantity.of(reorderThreshold));
    }

}
