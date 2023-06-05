package cart.db.repository;

import cart.db.dao.CouponDao;
import cart.db.dao.MemberCouponDao;
import cart.db.dao.MemberDao;
import cart.db.entity.CouponEntity;
import cart.db.entity.MemberCouponEntity;
import cart.db.entity.MemberEntity;
import cart.domain.Member;
import cart.domain.coupon.Coupon;
import cart.domain.coupon.MemberCoupon;
import cart.domain.coupon.repository.MemberCouponRepository;
import cart.exception.CouponException;
import cart.exception.ExceptionType;
import cart.exception.MemberException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcMemberCouponRepository implements MemberCouponRepository {

    private final CouponDao couponDao;
    private final MemberCouponDao memberCouponDao;
    private final MemberDao memberDao;

    public JdbcMemberCouponRepository(CouponDao couponDao, MemberCouponDao memberCouponDao, MemberDao memberDao) {
        this.couponDao = couponDao;
        this.memberCouponDao = memberCouponDao;
        this.memberDao = memberDao;
    }

    @Override
    public Optional<MemberCoupon> findById(Long id) {
        Optional<MemberCouponEntity> savedMemberCoupon = memberCouponDao.findById(id);
        if (savedMemberCoupon.isEmpty()) {
            return Optional.empty();
        }
        MemberCouponEntity memberCouponEntity = savedMemberCoupon.get();
        MemberCoupon memberCoupon = toDomain(memberCouponEntity);
        return Optional.of(memberCoupon);
    }

    private MemberCoupon toDomain(MemberCouponEntity memberCoupon) {
        Coupon coupon = toCoupon(memberCoupon.getCouponId());
        Member member = toMember(memberCoupon.getMemberId());
        return new MemberCoupon(memberCoupon.getId(), member, coupon, memberCoupon.getExpiredDate());
    }

    private Coupon toCoupon(Long couponId) {
        return couponDao.findById(couponId)
                .map(CouponEntity::toDomain)
                .orElseThrow(() -> new CouponException(ExceptionType.NOT_FOUND_COUPON));
    }

    private Member toMember(Long memberId) {
        return memberDao.findById(memberId)
                .map(MemberEntity::toDomain)
                .orElseThrow(() -> new MemberException(ExceptionType.NOT_FOUND_MEMBER));
    }

    @Override
    public MemberCoupon save(MemberCoupon memberCoupon) {
        MemberCouponEntity memberCouponEntity = toEntity(memberCoupon);
        Long id = memberCouponDao.save(memberCouponEntity);
        return new MemberCoupon(id, memberCoupon.getMember(), memberCoupon.getCoupon(), memberCoupon.getExpiredDate());
    }

    private MemberCouponEntity toEntity(MemberCoupon memberCoupon) {
        return new MemberCouponEntity(
                memberCoupon.getMember().getId(),
                memberCoupon.getCoupon().getId(),
                memberCoupon.getExpiredDate()
        );
    }

    @Override
    public void delete(MemberCoupon memberCoupon) {
        memberCouponDao.deleteById(memberCoupon.getId());
    }

    @Override
    public List<MemberCoupon> findNotExpired(Member member) {
        List<MemberCouponEntity> memberCouponEntities = memberCouponDao.findByMemberId(member.getId());
        return memberCouponEntities.stream()
                .map(this::toDomain)
                .filter(MemberCoupon::isNotExpired)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<MemberCoupon> memberCoupons) {
        List<MemberCouponEntity> memberCouponEntities = memberCoupons.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        memberCouponDao.saveAll(memberCouponEntities);
    }
}
