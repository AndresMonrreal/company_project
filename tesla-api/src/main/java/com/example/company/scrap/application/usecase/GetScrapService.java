package com.example.company.scrap.application.usecase;

import java.util.List;

import com.example.company.profiles.domain.model.Profile;
import com.example.company.profiles.domain.port.out.ProfileRepositoryPort;
import com.example.company.scrap.domain.exception.ScrapNotFoundException;
import com.example.company.scrap.domain.model.ScrapRecord;
import com.example.company.scrap.domain.port.in.GetScrapUseCase;
import com.example.company.scrap.domain.port.in.ScrapResult;
import com.example.company.scrap.domain.port.out.ScrapRepositoryPort;
import com.example.company.shifts.domain.model.Shift;
import com.example.company.shifts.domain.port.out.ShiftRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetScrapService implements GetScrapUseCase {

    private final ScrapRepositoryPort scrapRepository;
    private final ShiftRepositoryPort shiftRepository;
    private final ProfileRepositoryPort profileRepository;

    public GetScrapService(ScrapRepositoryPort scrapRepository,
                           ShiftRepositoryPort shiftRepository,
                           ProfileRepositoryPort profileRepository) {
        this.scrapRepository = scrapRepository;
        this.shiftRepository = shiftRepository;
        this.profileRepository = profileRepository;
    }

    @Override
    public ScrapResult findById(Long id) {
        ScrapRecord record = scrapRepository.findById(id)
                .orElseThrow(() -> new ScrapNotFoundException(id));

        Shift shift = shiftRepository.findById(record.shiftId()).orElse(null);
        Profile profile = profileRepository.findById(record.profileId()).orElse(null);

        return new ScrapResult(
                record.id(),
                record.shiftId(),
                shift != null ? shift.name() : null,
                record.profileId(),
                profile != null ? profile.code() : null,
                record.operatorId(),
                record.quantity(),
                record.reason(),
                record.createdAt()
        );
    }

    @Override
    public List<ScrapResult> findByOperatorAndShift(Long operatorId, Long shiftId) {
        return scrapRepository.findByOperatorAndShift(operatorId, shiftId);
    }
}