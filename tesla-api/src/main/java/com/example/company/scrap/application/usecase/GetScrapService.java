package com.example.company.scrap.application.usecase;

import java.util.List;

import com.example.company.scrap.domain.exception.ScrapNotFoundException;
import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.GetScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetScrapService implements GetScrapUseCase {

    private final ScrapRepositoryPort scrapRepository;

    public GetScrapService(ScrapRepositoryPort scrapRepository) {
        this.scrapRepository = scrapRepository;
    }

    @Override
    public ScrapResult findById(Long id) {
        return scrapRepository.findById(id)
                .map(this::toResult)
                .orElseThrow(() -> new ScrapNotFoundException(id));
    }

    @Override
    public List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        return scrapRepository.findByOperatorAndShift(operatorId, shiftId)
                .stream()
                .map(this::toResult)
                .toList();
    }

    private ScrapResult toResult(ScrapRecord record) {
        return new ScrapResult(
                record.id(),
                record.cuttingRecordId(),
                record.quantity(),
                record.reason(),
                record.createdAt()
        );
    }
}