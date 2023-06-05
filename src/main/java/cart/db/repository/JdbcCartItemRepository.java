package cart.db.repository;

import static java.util.stream.Collectors.toList;

import cart.db.dao.CartItemDao;
import cart.db.entity.CartItemEntity;
import cart.db.entity.MemberEntity;
import cart.db.entity.ProductEntity;
import cart.domain.CartItem;
import cart.domain.Member;
import cart.domain.Product;
import cart.domain.repository.CartItemRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcCartItemRepository implements CartItemRepository {

    private final CartItemDao cartItemDao;

    public JdbcCartItemRepository(CartItemDao cartItemDao) {
        this.cartItemDao = cartItemDao;
    }

    @Override
    public List<CartItem> findAllByMemberId(Long id) {
        List<CartItemEntity> cartItemEntities = cartItemDao.findByMemberId(id);
        return cartItemEntities.stream()
                .map(CartItemEntity::toDomain)
                .collect(toList());
    }

    @Override
    public CartItem save(CartItem cartItem) {
        Long id = cartItemDao.save(
                new CartItemEntity(
                        cartItem.getId(),
                        toEntity(cartItem.getProduct()),
                        toEntity(cartItem.getMember()),
                        cartItem.getQuantity()
                )
        );
        return new CartItem(id, cartItem.getQuantity(), cartItem.getProduct(), cartItem.getMember());
    }

    private MemberEntity toEntity(Member member) {
        return new MemberEntity(
                member.getId(),
                member.getEmail(),
                member.getPassword()
        );
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.getId(),
                product.getName(),
                product.getPrice().getValue(),
                product.getImageUrl()
        );
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        return cartItemDao.findById(id)
                .map(CartItemEntity::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        cartItemDao.deleteById(id);
    }

    @Override
    public void updateQuantity(CartItem cartItem) {
        cartItemDao.updateQuantity(
                new CartItemEntity(
                        cartItem.getId(),
                        toEntity(cartItem.getProduct()),
                        toEntity(cartItem.getMember()),
                        cartItem.getQuantity()
                )
        );
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        cartItemDao.deleteAllByIds(ids);
    }

    @Override
    public List<CartItem> findByIds(List<Long> ids) {
        return cartItemDao.findAllByIds(ids).stream()
                .map(CartItemEntity::toDomain)
                .collect(toList());
    }
}
