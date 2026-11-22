package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import sayyeed.dev.atlantbackend.entities.products.ProductImageEntity;

import java.util.List;

public interface ProductImageRepository extends CrudRepository<ProductImageEntity, String> {

    @Query("from ProductImageEntity where productId = ?1 order by displayOrder")
    List<ProductImageEntity> findByProductIdOrderByDisplayOrder(String productId);

    @Query("from ProductImageEntity where productId = ?1")
    List<ProductImageEntity> findByProductId(String productId);
}
