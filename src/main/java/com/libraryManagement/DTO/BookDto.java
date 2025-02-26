package com.libraryManagement.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookDto {

    private Long id;

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Author name cannot be empty")
    private String author;

    @NotBlank(message = "ISBN cannot be empty")
    private String isbn;

    @NotNull(message = "Published year cannot be null")
    @Positive(message = "Published year must be a positive number")
    private Integer publishedYear;

    @NotBlank(message = "Category cannot be empty")
    private String category;

    private Long userId;

    private Long borrowCount;

    public BookDto() {
    }

    public BookDto(Long id, String title, String author, int publishedYear, long borrowCount,String category) {

        this.id = id;
        this.title=title;
        this.author=author;
        this.publishedYear=publishedYear;
        this.borrowCount=borrowCount;
        this.category=category;
    }

    public BookDto(Long id, String title, String author, String category, int publishedYear, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.publishedYear = publishedYear;
    }
}
