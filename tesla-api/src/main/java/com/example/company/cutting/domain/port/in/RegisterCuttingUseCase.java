package com.example.company.cutting.domain.port.in;

public interface RegisterCuttingUseCase {

    CuttingResult register(RegisterCuttingCommand command);
}