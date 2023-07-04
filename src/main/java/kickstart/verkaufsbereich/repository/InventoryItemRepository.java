package kickstart.verkaufsbereich.repository;

import org.salespointframework.inventory.UniqueInventoryItem;
import org.springframework.data.repository.CrudRepository;

public interface InventoryItemRepository extends CrudRepository<UniqueInventoryItem, Long> {

}
