package sayyeed.dev.atlantbackend.dto.products;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageDTO {

    private String id;

    private String attachId;

    private String path;

    private Integer displayOrder;
}
