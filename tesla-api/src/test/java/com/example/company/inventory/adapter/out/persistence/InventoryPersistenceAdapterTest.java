package com.example.company.inventory.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.company.inventory.domain.model.AvailableInventoryResult;

@ExtendWith(MockitoExtension.class)
class InventoryPersistenceAdapterTest {

    @Mock
    private InventoryItemSpringRepository inventoryRepository;

    private InventoryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryPersistenceAdapter(inventoryRepository);
    }

    @Test
    void returnsEmptyWhenRepositoryReturnsEmptyArray_regressionForBug500() {
        when(inventoryRepository.findAvailableByLot("LOTE-INEXISTENTE"))
                .thenReturn(Optional.of(new Object[0]));

        Optional<AvailableInventoryResult> result = adapter.findAvailableByLot("LOTE-INEXISTENTE");

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyWhenRepositoryReturnsTrueEmptyOptional() {
        when(inventoryRepository.findAvailableByLot("LOTE-INEXISTENTE"))
                .thenReturn(Optional.empty());

        Optional<AvailableInventoryResult> result = adapter.findAvailableByLot("LOTE-INEXISTENTE");

        assertThat(result).isEmpty();
    }

    @Test
    void mapsFlatRowCorrectly() {
        Object[] row = {10L, "EXTE00038", "EXTE00036-SA", "L-2026-19", 100};
        when(inventoryRepository.findAvailableByLot("L-2026-19")).thenReturn(Optional.of(row));

        Optional<AvailableInventoryResult> result = adapter.findAvailableByLot("L-2026-19");

        assertThat(result).contains(
                new AvailableInventoryResult(10L, "EXTE00038", "EXTE00036-SA", "L-2026-19", 100));
    }

    @Test
    void mapsHibernateWrappedRowCorrectly() {
        Object[] innerRow = {10L, "EXTE00038", "EXTE00036-SA", "L-2026-19", 100};
        Object[] wrappedRow = {innerRow};
        when(inventoryRepository.findAvailableByLot("L-2026-19")).thenReturn(Optional.of(wrappedRow));

        Optional<AvailableInventoryResult> result = adapter.findAvailableByLot("L-2026-19");

        assertThat(result).contains(
                new AvailableInventoryResult(10L, "EXTE00038", "EXTE00036-SA", "L-2026-19", 100));
    }
}