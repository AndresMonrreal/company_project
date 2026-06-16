package com.example.company.scrap.application.usecase;

import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.RegisterScrapCommand;
import com.example.company.scrap.domain.port.in.RegisterScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterScrapService implements RegisterScrapUseCase {

    private final ScrapRepositoryPort scrapRepository;

    public RegisterScrapService(ScrapRepositoryPort scrapRepository) {
        this.scrapRepository = scrapRepository;
    }

    @Override
    @Transactional
    public ScrapResult register(RegisterScrapCommand command) {
        ScrapRecord record = ScrapRecord.create(command.cuttingRecordId(), command.quantity(),
                command.reason());
        ScrapRecord saved = scrapRepository.save(record);
        return toResult(saved);
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