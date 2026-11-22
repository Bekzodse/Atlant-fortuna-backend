package sayyeed.dev.atlantbackend.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import sayyeed.dev.atlantbackend.mapper.ProductPublicMapper;
import sayyeed.dev.atlantbackend.entities.products.ProductEntity;
import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends CrudRepository<ProductEntity, String> {

    // Admin queries - return full entities
    @Query("from ProductEntity where category = ?1 order by createdAt desc")
    List<ProductEntity> findByCategory(ProductCategoryEnum category);

    @Query("from ProductEntity where brand = ?1 order by createdAt desc")
    List<ProductEntity> findByBrand(String brand);

    @Query("from ProductEntity order by createdAt desc")
    List<ProductEntity> findAllOrderByCreatedAtDesc();

    // Public queries with language support - return mappers
    @Query("SELECT p.id AS id, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.nameUz " +
            "   WHEN 'ru' THEN p.nameRu " +
            "   WHEN 'en' THEN p.nameEn " +
            "   ELSE p.nameEn " +
            "END AS name, " +
            "p.price AS price, " +
            "p.brand AS brand, " +
            "p.articleNumber AS articleNumber, " +
            "p.category AS category, " +
            "p.discount AS discount, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.paymentMethodUz " +
            "   WHEN 'ru' THEN p.paymentMethodRu " +
            "   WHEN 'en' THEN p.paymentMethodEn " +
            "   ELSE p.paymentMethodEn " +
            "END AS paymentMethod, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.deliveryUz " +
            "   WHEN 'ru' THEN p.deliveryRu " +
            "   WHEN 'en' THEN p.deliveryEn " +
            "   ELSE p.deliveryEn " +
            "END AS delivery, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.descriptionUz " +
            "   WHEN 'ru' THEN p.descriptionRu " +
            "   WHEN 'en' THEN p.descriptionEn" +
            "   ELSE p.descriptionEn " +
            "END AS description, " +
            "p.createdAt AS createdAt " +
            "FROM ProductEntity p " +
            "WHERE p.id = :id")
    Optional<ProductPublicMapper> getByIdAndLang(@Param("id") String id, @Param("lang") String lang);

    @Query("SELECT p.id AS id, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.nameUz " +
            "   WHEN 'ru' THEN p.nameRu " +
            "   WHEN 'en' THEN p.nameEn " +
            "   ELSE p.nameEn " +
            "END AS name, " +
            "p.price AS price, " +
            "p.brand AS brand, " +
            "p.articleNumber AS articleNumber, " +
            "p.category AS category, " +
            "p.discount AS discount, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.paymentMethodUz " +
            "   WHEN 'ru' THEN p.paymentMethodRu " +
            "   WHEN 'en' THEN p.paymentMethodEn " +
            "   ELSE p.paymentMethodEn " +
            "END AS paymentMethod, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.deliveryUz " +
            "   WHEN 'ru' THEN p.deliveryRu " +
            "   WHEN 'en' THEN p.deliveryEn " +
            "   ELSE p.deliveryEn " +
            "END AS delivery, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.descriptionUz " +
            "   WHEN 'ru' THEN p.descriptionRu " +
            "   WHEN 'en' THEN p.descriptionEn" +
            "   ELSE p.descriptionEn " +
            "END AS description, " +
            "p.createdAt AS createdAt " +
            "FROM ProductEntity p " +
            "ORDER BY p.createdAt DESC")
    List<ProductPublicMapper> getAllByLang(@Param("lang") String lang);

    @Query("SELECT p.id AS id, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.nameUz " +
            "   WHEN 'ru' THEN p.nameRu " +
            "   WHEN 'en' THEN p.nameEn " +
            "   ELSE p.nameEn " +
            "END AS name, " +
            "p.price AS price, " +
            "p.brand AS brand, " +
            "p.articleNumber AS articleNumber, " +
            "p.category AS category, " +
            "p.discount AS discount, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.paymentMethodUz " +
            "   WHEN 'ru' THEN p.paymentMethodRu " +
            "   WHEN 'en' THEN p.paymentMethodEn " +
            "   ELSE p.paymentMethodEn " +
            "END AS paymentMethod, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.deliveryUz " +
            "   WHEN 'ru' THEN p.deliveryRu " +
            "   WHEN 'en' THEN p.deliveryEn " +
            "   ELSE p.deliveryEn " +
            "END AS delivery, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.descriptionUz " +
            "   WHEN 'ru' THEN p.descriptionRu " +
            "   WHEN 'en' THEN p.descriptionEn" +
            "   ELSE p.descriptionEn " +
            "END AS description, " +
            "p.createdAt AS createdAt " +
            "FROM ProductEntity p " +
            "WHERE p.category = :category " +
            "ORDER BY p.createdAt DESC")
    List<ProductPublicMapper> getByCategoryAndLang(@Param("category") ProductCategoryEnum category, @Param("lang") String lang);

    @Query("SELECT p.id AS id, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.nameUz " +
            "   WHEN 'ru' THEN p.nameRu " +
            "   WHEN 'en' THEN p.nameEn " +
            "   ELSE p.nameEn " +
            "END AS name, " +
            "p.price AS price, " +
            "p.brand AS brand, " +
            "p.articleNumber AS articleNumber, " +
            "p.category AS category, " +
            "p.discount AS discount, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.paymentMethodUz " +
            "   WHEN 'ru' THEN p.paymentMethodRu " +
            "   WHEN 'en' THEN p.paymentMethodEn " +
            "   ELSE p.paymentMethodEn " +
            "END AS paymentMethod, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.deliveryUz " +
            "   WHEN 'ru' THEN p.deliveryRu " +
            "   WHEN 'en' THEN p.deliveryEn " +
            "   ELSE p.deliveryEn " +
            "END AS delivery, " +
            "CASE :lang " +
            "   WHEN 'uz' THEN p.descriptionUz " +
            "   WHEN 'ru' THEN p.descriptionRu " +
            "   WHEN 'en' THEN p.descriptionEn" +
            "   ELSE p.descriptionEn " +
            "END AS description, " +
            "p.createdAt AS createdAt " +
            "FROM ProductEntity p " +
            "WHERE p.brand = :brand " +
            "ORDER BY p.createdAt DESC")
    List<ProductPublicMapper> getByBrandAndLang(@Param("brand") String brand, @Param("lang") String lang);
}
