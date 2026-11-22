package sayyeed.dev.atlantbackend.mapper;

import sayyeed.dev.atlantbackend.enums.ProductCategoryEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ProductPublicMapper {
    String getId();

    String getName();

    BigDecimal getPrice();

    String getBrand();

    String getArticleNumber();

    ProductCategoryEnum getCategory();

    Boolean getDiscount();

    String getPaymentMethod();

    String getDelivery();

    String getDescription();

    LocalDateTime getCreatedAt();
}
