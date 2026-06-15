package com.example.company.molding.application.usecase;

import com.example.company.molding.domain.exception.MoldingOutputNotFoundException;
import com.example.company.molding.domain.model.MoldingOutput;
import com.example.company.molding.domain.port.in.MoldingOutputResult;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputCommand;
import com.example.company.molding.domain.port.in.RegisterMoldingOutputUseCase;
import com.example.company.molding.domain.port.out.CuttingRecordExistsPort;
import com.example.company.molding.domain.port.out.MoldingOutputRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterMoldingOutputService implements RegisterMoldingOutputUseCase {

    private final MoldingOutputRepositoryPort moldingOutputRepository;
    private final CuttingRecordExistsPort cuttingRecordExistsPort;

    public RegisterMoldingOutputService(
            MoldingOutputRepositoryPort moldingOutputRepository,
            CuttingRecordExistsPort cuttingRecordExistsPort
    ) {
        this.moldingOutputRepository = moldingOutputRepository;
        this.cuttingRecordExistsPort = cuttingRecordExistsPort;
    }

    @Override
    @Transactional
    public MoldingOutputResult register(RegisterMoldingOutputCommand command) {
        if (!cuttingRecordExistsPort.existsById(command.cuttingRecordId())) {
            throw new MoldingOutputNotFoundException(command.cuttingRecordId());
        }

        MoldingOutput output = MoldingOutput.create(
                command.cuttingRecordId(),
                command.operatorId(),
                command.quantitySent()
        );

        MoldingOutput saved = moldingOutputRepository.save(output);

        return new MoldingOutputResult(
                saved.id(),
                saved.cuttingRecordId(),
                saved.quantitySent(),
                saved.sentAt()
        );
    }
}
