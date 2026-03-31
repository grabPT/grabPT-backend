package com.grabpt.repository.ProProfileRepository;

import com.grabpt.domain.entity.Address;
import com.grabpt.domain.entity.Category;
import com.grabpt.domain.entity.ProProfile;
import com.grabpt.domain.entity.Review;
import com.grabpt.domain.entity.Users;
import com.grabpt.domain.enums.SortType;
import com.grabpt.dto.request.ProSearchRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class ProProfileRepositoryCustomImpl implements ProProfileRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ProProfile> searchProfiles(ProSearchRequest request, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<ProProfile> query = cb.createQuery(ProProfile.class);
        Root<ProProfile> root = query.from(ProProfile.class);

        Join<ProProfile, Users> userJoin = root.join("user", JoinType.LEFT);
        Join<Users, Address> addressJoin = userJoin.join("address", JoinType.LEFT);
        Join<ProProfile, Category> categoryJoin = root.join("category", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, query, root, userJoin, addressJoin, categoryJoin, request);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(buildOrderBy(cb, query, root, request.getSortBy()));
        query.distinct(true);
        query.select(root);

        List<ProProfile> results = em.createQuery(query)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        long total = countProfiles(cb, request);

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(
        CriteriaBuilder cb,
        CriteriaQuery<?> query,
        Root<ProProfile> root,
        Join<ProProfile, Users> userJoin,
        Join<Users, Address> addressJoin,
        Join<ProProfile, Category> categoryJoin,
        ProSearchRequest request) {

        List<Predicate> predicates = new ArrayList<>();

        if (hasText(request.getKeyword())) {
            String kw = "%" + request.getKeyword() + "%";
            predicates.add(cb.or(
                cb.like(userJoin.get("nickname"), kw),
                cb.like(root.get("center"), kw)
            ));
        }

        if (hasText(request.getCategoryCode())) {
            predicates.add(cb.equal(categoryJoin.get("code"), request.getCategoryCode()));
        }

        if (hasText(request.getCity())) {
            predicates.add(cb.like(addressJoin.get("city"), "%" + request.getCity() + "%"));
        }

        if (hasText(request.getDistrict())) {
            predicates.add(cb.like(addressJoin.get("district"), "%" + request.getDistrict() + "%"));
        }

        if (request.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerSession"), request.getMinPrice()));
        }

        if (request.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("pricePerSession"), request.getMaxPrice()));
        }

        if (request.getMinRating() != null) {
            Subquery<Double> avgSub = query.subquery(Double.class);
            Root<Review> reviewRoot = avgSub.from(Review.class);
            avgSub.select(cb.avg(reviewRoot.get("rating")));
            avgSub.where(cb.equal(reviewRoot.get("proProfile"), root));
            predicates.add(cb.greaterThanOrEqualTo(avgSub, request.getMinRating()));
        }

        return predicates;
    }

    private List<Order> buildOrderBy(
        CriteriaBuilder cb,
        CriteriaQuery<?> query,
        Root<ProProfile> root,
        SortType sortBy) {

        List<Order> orders = new ArrayList<>();

        if (sortBy == null) sortBy = SortType.RATING;

        switch (sortBy) {
            case RATING -> {
                Subquery<Double> ratingSub = query.subquery(Double.class);
                Root<Review> ratingRoot = ratingSub.from(Review.class);
                ratingSub.select(cb.avg(ratingRoot.get("rating")));
                ratingSub.where(cb.equal(ratingRoot.get("proProfile"), root));
                orders.add(cb.desc(cb.coalesce(ratingSub, 0.0)));
            }
            case PRICE_ASC -> orders.add(cb.asc(root.get("pricePerSession")));
            case PRICE_DESC -> orders.add(cb.desc(root.get("pricePerSession")));
            case REVIEW_COUNT -> {
                Subquery<Long> countSub = query.subquery(Long.class);
                Root<Review> countRoot = countSub.from(Review.class);
                countSub.select(cb.count(countRoot));
                countSub.where(cb.equal(countRoot.get("proProfile"), root));
                orders.add(cb.desc(cb.coalesce(countSub, 0L)));
            }
        }

        orders.add(cb.desc(root.get("id")));
        return orders;
    }

    private long countProfiles(CriteriaBuilder cb, ProSearchRequest request) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProProfile> root = countQuery.from(ProProfile.class);

        Join<ProProfile, Users> userJoin = root.join("user", JoinType.LEFT);
        Join<Users, Address> addressJoin = userJoin.join("address", JoinType.LEFT);
        Join<ProProfile, Category> categoryJoin = root.join("category", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, countQuery, root, userJoin, addressJoin, categoryJoin, request);
        countQuery.select(cb.countDistinct(root));
        countQuery.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(countQuery).getSingleResult();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
