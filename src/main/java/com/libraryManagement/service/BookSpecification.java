package com.libraryManagement.service;


import com.libraryManagement.model.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {
    public static Specification<Book> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("category")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), pattern)
            );
        };
    }
}
