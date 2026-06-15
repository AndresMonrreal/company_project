package com.example.company.scrap.application.usecase;

import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.RegisterScrapCommand;
import com.example.company.scrap.domain.port.in.RegisterScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.CuttingRecordExistsPort;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterScrapService implements RegisterScrapUseCase {

    private final ScrapRepositoryPort scrapRepository;
    private final CuttingRecordExistsPort cuttingRecordExistsPort;

    public RegisterScrapService(ScrapRepositoryPort scrapRepository, CuttingRecordExistsPort cuttingRecordExistsPort) {
        this.scrapRepository = scrapRepository;
        this.cuttingRecordExistsPort = cuttingRecordExistsPort;
    }

    @Override
    @Transactional
    public ScrapResult register(RegisterScrapCommand command) {
        if (!cuttingRecordExistsPort.existsById(command.cuttingRecordId())) {
            throw new IllegalArgumentException("Cutting record not found: " + command.cuttingRecordId());
        }

        ScrapRecord scrapRecord = ScrapRecord.create(
                command.cuttingRecordId(),
                command.quantity(),
                command.reason()
        );

        ScrapRecord saved = scrapRepository.save(scrapRecord);

        return new ScrapResult(
                saved.id(),
                saved.cuttingRecordId(),
                saved.quantity(),
                saved.reason(),
                saved.createdAt()
        );
    }
}
