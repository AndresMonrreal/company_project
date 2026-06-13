package com.example.company.cutting.domain.model;

public record CuttingQuantities(
        int initialQuantity,
        int goodQuantity,
        int scrapQuantity
) {

    public CuttingQuantities {
        if (initialQuantity <= 0) {
            throw new IllegalArgumentException("initial_quantity must be greater than zero");
        }
        if (goodQuantity < 0) {
            throw new IllegalArgumentException("good_quantity must not be negative");
        }
        if (scrapQuantity < 0) {
            throw new IllegalArgumentException("scrap_quantity must not be negative");
        }
        if (initialQuantity != goodQuantity + scrapQuantity) {
            throw new IllegalArgumentException("initial_quantity must equal good_quantity + scrap_quantity");
        }
    }
}
