package com.example.company.cutting.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CuttingQuantitiesTest {

    @Test
    void createsQuantitiesWhenInitialEqualsGoodPlusScrap() {
        CuttingQuantities quantities = new CuttingQuantities(100, 97, 3);

        assertThat(quantities.initialQuantity()).isEqualTo(100);
        assertThat(quantities.goodQuantity()).isEqualTo(97);
        assertThat(quantities.scrapQuantity()).isEqualTo(3);
    }

    @Test
    void rejectsQuantitiesThatDoNotMatchCuttingRule() {
        assertThatThrownBy(() -> new CuttingQuantities(100, 98, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("initial_quantity must equal good_quantity + scrap_quantity");
    }
}
