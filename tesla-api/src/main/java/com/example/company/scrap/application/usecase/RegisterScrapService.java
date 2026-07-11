package com.example.company.scrap.application.usecase;

import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import com.example.company.scrap.domain.exception.ScrapProfileInactiveException;
import com.example.company.scrap.domain.exception.ScrapShiftInactiveException;
import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.RegisterScrapCommand;
import com.example.company.scrap.domain.port.in.RegisterScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterScrapService implements RegisterScrapUseCase {

    private final ScrapRepositoryPort scrapRepository;
    private final ShiftRepositoryPort shiftRepository;
    private final ProfileRepositoryPort profileRepository;

    public RegisterScrapService(ScrapRepositoryPort scrapRepository,
                                ShiftRepositoryPort shiftRepository,
                                ProfileRepositoryPort profileRepository) {
        this.scrapRepository = scrapRepository;
        this.shiftRepository = shiftRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    @Transactional
    public ScrapResult register(RegisterScrapCommand command) {
        Shift shift = shiftRepository.findActiveById(command.shiftId())
                .orElseThrow(() -> new ScrapShiftInactiveException(command.shiftId()));

        Profile profile = profileRepository.findActiveById(command.profileId())
                .orElseThrow(() -> new ScrapProfileInactiveException(command.profileId()));

        ScrapRecord record = ScrapRecord.create(command.shiftId(), command.profileId(),
                command.operatorId(), command.quantity(), command.reason());
        ScrapRecord saved = scrapRepository.save(record);

        return new ScrapResult(
                saved.id(),
                saved.shiftId(),
                shift.name(),
                saved.profileId(),
                profile.code(),
                saved.operatorId(),
                saved.quantity(),
                saved.reason(),
                saved.createdAt()
        );
    }
}