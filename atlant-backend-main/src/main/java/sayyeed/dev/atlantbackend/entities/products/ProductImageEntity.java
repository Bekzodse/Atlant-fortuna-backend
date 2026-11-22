package sayyeed.dev.atlantbackend.entities.products;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sayyeed.dev.atlantbackend.entities.AttachEntity;

@Entity
@Table(name = "product_image")
@Getter
@Setter
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "product_id")
    private String productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity product;

    @Column(name = "attach_id")
    private String attachId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attach_id", insertable = false, updatable = false)
    private AttachEntity attachment;

    @Column(name = "display_order")
    private Integer displayOrder;
}
