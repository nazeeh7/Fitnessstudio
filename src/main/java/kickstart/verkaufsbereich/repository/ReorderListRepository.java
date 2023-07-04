package kickstart.verkaufsbereich.repository;

import java.util.List;
import java.util.Optional;

import org.salespointframework.catalog.Product.ProductIdentifier;
import org.springframework.data.repository.CrudRepository;

import kickstart.verkaufsbereich.model.Artikel;
import kickstart.verkaufsbereich.model.ReorderList;

public interface ReorderListRepository extends CrudRepository<ReorderList, Long> {

	Optional<ReorderList> findByArtikel(Artikel artikel);
	List<ReorderList> findAllByArtikel(Artikel artikel);
	Optional<ReorderList> findByArtikelId(ProductIdentifier artikelId);
	
}
