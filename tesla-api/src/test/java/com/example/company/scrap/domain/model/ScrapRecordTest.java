package com.example.company.scrap.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class ScrapRecordTest {

    @Test
    void createsScrapRecordWithRequiredFields() {
        ScrapRecord record = ScrapRecord.create(1L, 2L, 3L, 5, "Defect");

        assertThat(record.id()).isNull();
        assertThat(record.shiftId()).isEqualTo(1L);
        assertThat(record.profileId()).isEqualTo(2L);
        assertThat(record.operatorId()).isEqualTo(3L);
        assertThat(record.quantity()).isEqualTo(5);
        assertThat(record.reason()).isEqualTo("Defect");
        assertThat(record.createdAt()).isNull();
    }

    @Test
    void restoresScrapRecordWithAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 15, 10, 30);
        ScrapRecord record = ScrapRecord.restore(10L, 1L, 2L, 3L, 8, "Scratch", now);

        assertThat(record.id()).isEqualTo(10L);
        assertThat(record.shiftId()).isEqualTo(1L);
        assertThat(record.profileId()).isEqualTo(2L);
        assertThat(record.operatorId()).isEqualTo(3L);
        assertThat(record.quantity()).isEqualTo(8);
        assertThat(record.reason()).isEqualTo("Scratch");
        assertThat(record.createdAt()).isEqualTo(now);
    }
}