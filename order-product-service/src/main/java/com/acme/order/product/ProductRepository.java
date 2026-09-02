package com.acme.order.product;
import com.acme.order.api.product.ProductDtos.SkuView;import com.acme.order.common.core.*;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.stereotype.Repository;
@Repository public class ProductRepository {
 private final JdbcTemplate jdbc; public ProductRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public SkuView sku(long id){return jdbc.query("SELECT s.id,s.spu_id,p.name,s.sku_name,s.price_cent,s.status FROM product_sku s JOIN product_spu p ON p.id=s.spu_id WHERE s.id=?",rs->{if(!rs.next())throw new BizException(ErrorCode.SKU_NOT_FOUND,"SKU不存在");return new SkuView(rs.getLong(1),rs.getLong(2),rs.getString(3),rs.getString(4),rs.getLong(5),rs.getInt(6)==1);},id);}
}
