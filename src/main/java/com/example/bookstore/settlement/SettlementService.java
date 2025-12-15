
package com.example.bookstore.settlement;

import com.example.bookstore.common.ApiException;
import com.example.bookstore.common.ErrorCode;
import com.example.bookstore.settlement.dto.SettlementDtos.*;
import com.example.bookstore.user.User;
import com.example.bookstore.user.UserRepository;
import com.example.bookstore.user.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public SettlementService(SettlementRepository settlementRepository,
                             SettlementItemRepository settlementItemRepository,
                             JdbcTemplate jdbcTemplate,
                             UserRepository userRepository) {
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @Transactional
    public CreateSettlementResponse create(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "period_end는 period_start 이후여야 합니다.");
        }

        LocalDate endPlus = end.plusDays(1);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                select oi.seller_id as seller_id,
                       sum(oi.subtotal_cents) as gross_cents
                  from order_items oi
                  join orders o on o.id = oi.order_id
                 where o.created_at >= ?
                   and o.created_at < ?
                   and o.status in ('paid','shipped','delivered')
                 group by oi.seller_id
                """,
                Date.valueOf(start), Date.valueOf(endPlus)
        );

        List<Long> createdIds = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Long sellerId = ((Number) row.get("seller_id")).longValue();
            int gross = ((Number) row.get("gross_cents")).intValue();

            int bps = fetchCommissionBps(sellerId);
            int commission = calcCommission(gross, bps);
            int net = gross - commission;

            Settlement s = new Settlement();
            s.setSellerId(sellerId);
            s.setPeriodStart(start);
            s.setPeriodEnd(end);
            s.setStatus(SettlementStatus.pending);
            s.setTotalGrossCents(gross);
            s.setTotalCommissionCents(commission);
            s.setTotalNetCents(net);

            Settlement saved = settlementRepository.save(s);
            createdIds.add(saved.getId());

            List<Map<String, Object>> items = jdbcTemplate.queryForList(
                    """
                    select oi.id as order_item_id,
                           oi.subtotal_cents as gross_cents
                      from order_items oi
                      join orders o on o.id = oi.order_id
                     where o.created_at >= ?
                       and o.created_at < ?
                       and o.status in ('paid','shipped','delivered')
                       and oi.seller_id = ?
                    """,
                    Date.valueOf(start), Date.valueOf(endPlus), sellerId
            );

            List<SettlementItem> toSave = new ArrayList<>();
            for (Map<String, Object> it : items) {
                Long orderItemId = ((Number) it.get("order_item_id")).longValue();
                int itemGross = ((Number) it.get("gross_cents")).intValue();
                int itemCommission = calcCommission(itemGross, bps);
                int itemNet = itemGross - itemCommission;

                SettlementItem si = new SettlementItem();
                si.setSettlementId(saved.getId());
                si.setOrderItemId(orderItemId);
                si.setSellerId(sellerId);
                si.setGrossCents(itemGross);
                si.setCommissionCents(itemCommission);
                si.setNetCents(itemNet);
                toSave.add(si);
            }
            settlementItemRepository.saveAll(toSave);
        }

        return new CreateSettlementResponse(createdIds.size(), createdIds);
    }

    @Transactional
    public Page<Settlement> list(SettlementStatus status, Pageable pageable) {
        if (status == null) return settlementRepository.findAll(pageable);
        return settlementRepository.findByStatus(status, pageable);
    }

    /**
     * SELLER: 본인 정산 목록
     */
    @Transactional
    public Page<Settlement> listForSeller(Long sellerUserId, SettlementStatus status, Pageable pageable) {
        Long sellerId = requireSellerId(sellerUserId);
        if (status == null) return settlementRepository.findBySellerId(sellerId, pageable);
        return settlementRepository.findBySellerIdAndStatus(sellerId, status, pageable);
    }

    @Transactional
    public SettlementDetailDto detail(Long settlementId) {
        Settlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없습니다."));
        List<SettlementItemDto> items = settlementItemRepository.findBySettlementId(settlementId).stream()
                .map(i -> new SettlementItemDto(
                        i.getId(),
                        i.getOrderItemId(),
                        i.getSellerId(),
                        i.getGrossCents(),
                        i.getCommissionCents(),
                        i.getNetCents(),
                        i.getCreatedAt()
                )).toList();

        SettlementSummaryDto summary = toSummary(s);
        return new SettlementDetailDto(summary, items);
    }

    /**
     * SELLER: 본인 정산 상세
     */
    @Transactional
    public SettlementDetailDto detailForSeller(Long sellerUserId, Long settlementId) {
        Long sellerId = requireSellerId(sellerUserId);
        Settlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없습니다."));

        if (!Objects.equals(s.getSellerId(), sellerId)) {
            // 보안상 404로 숨길 수도 있지만, 과제 기준은 403도 OK
            throw new ApiException(ErrorCode.FORBIDDEN, "본인 정산만 조회할 수 있습니다.");
        }

        List<SettlementItemDto> items = settlementItemRepository
                .findBySettlementIdAndSellerId(settlementId, sellerId)
                .stream()
                .map(i -> new SettlementItemDto(
                        i.getId(),
                        i.getOrderItemId(),
                        i.getSellerId(),
                        i.getGrossCents(),
                        i.getCommissionCents(),
                        i.getNetCents(),
                        i.getCreatedAt()
                )).toList();

        return new SettlementDetailDto(toSummary(s), items);
    }

    @Transactional
    public SettlementSummaryDto sellerConfirm(Long sellerUserId, Long settlementId) {
        Long sellerId = requireSellerId(sellerUserId);
        Settlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없습니다."));

        if (!Objects.equals(s.getSellerId(), sellerId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "본인 정산만 확인할 수 있습니다.");
        }
        if (s.getStatus() == SettlementStatus.paid) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 지급 완료된 정산입니다.");
        }
        if (s.getSellerConfirmedAt() == null) {
            throw new ApiException(ErrorCode.CONFLICT, "판매자 확인이 필요합니다.");
        }
        if (s.getStatus() == SettlementStatus.cancelled) {
            throw new ApiException(ErrorCode.CONFLICT, "취소된 정산은 확인할 수 없습니다.");
        }
        if (s.getSellerConfirmedAt() == null) {
            s.setSellerConfirmedAt(Instant.now());
        }
        return toSummary(s);
    }

public SettlementSummaryDto approve(Long settlementId) {
        Settlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없습니다."));
        if (s.getStatus() == SettlementStatus.paid) {
            throw new ApiException(ErrorCode.CONFLICT, "이미 지급 완료된 정산입니다.");
        }
        if (s.getSellerConfirmedAt() == null) {
            throw new ApiException(ErrorCode.CONFLICT, "판매자 확인이 필요합니다.");
        }
        s.setStatus(SettlementStatus.approved);
        return toSummary(s);
    }

    @Transactional
    public SettlementSummaryDto pay(Long settlementId) {
        Settlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "정산을 찾을 수 없습니다."));
        if (s.getStatus() != SettlementStatus.approved) {
            throw new ApiException(ErrorCode.CONFLICT, "승인된 정산만 지급할 수 있습니다.");
        }
        s.setStatus(SettlementStatus.paid);
        s.setPaidAt(Instant.now());
        return toSummary(s);
    }

    public SettlementSummaryDto toSummary(Settlement s) {
        return new SettlementSummaryDto(
                s.getId(),
                s.getSellerId(),
                s.getPeriodStart(),
                s.getPeriodEnd(),
                s.getStatus(),
                s.getTotalGrossCents(),
                s.getTotalCommissionCents(),
                s.getTotalNetCents(),
                s.getPaidAt(),
                s.getSellerConfirmedAt(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }

    private int fetchCommissionBps(Long sellerId) {
        Integer bps = jdbcTemplate.query(
                "select commission_bps from sellers where id = ?",
                rs -> rs.next() ? rs.getInt(1) : null,
                sellerId
        );
        return bps == null ? 0 : bps;
    }

    private int calcCommission(int grossCents, int bps) {
        long v = (long) grossCents * (long) bps;
        return (int) (v / 10000L); // floor
    }

    private Long requireSellerId(Long sellerUserId) {
        if (sellerUserId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
        }
        User me = userRepository.findByIdAndDeletedAtIsNull(sellerUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다."));
        if (me.getRole() != UserRole.seller || me.getSellerId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "판매자 권한이 없습니다.");
        }
        return me.getSellerId();
    }
}
