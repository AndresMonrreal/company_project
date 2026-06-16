package com.example.company.molding.application.usecase;

import com.example.company.molding.domain.model.MoldingOutput;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputCommand;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputUseCase;
import com.example.company.molding.domain.port.out.MoldingOutputRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterMoldingOutputService implements RegisterMoldingOutputUseCase {

    private final MoldingOutputRepositoryPort moldingRepository;

    public RegisterMoldingOutputService(MoldingOutputRepositoryPort moldingRepository) {
        this.moldingRepository = moldingRepository;
    }

    @Override
    @Transactional
    public MoldingOutputResult register(RegisterMoldingOutputCommand command) {
        MoldingOutput output = MoldingOutput.create(
                command.cuttingRecordId(),
                command.quantitySent(),
                command.operatorId()
        );
        MoldingOutput saved = moldingRepository.save(output);
        return toResult(saved);
    }

    private MoldingOutputResult toResult(MoldingOutput output) {
        return new MoldingOutputResult(
                output.id(),
                output.cuttingRecordId(),
                output.quantitySent(),
                output.operatorId(),
                output.sentAt()
        );
    }
}
