package com.rustam.modern_dentistry.service.settings.teeth;

import com.rustam.modern_dentistry.dao.entity.settings.teeth.Teeth;
import com.rustam.modern_dentistry.dao.entity.settings.teeth.TeethExamination;
import com.rustam.modern_dentistry.dao.repository.settings.teeth.TeethRepository;
import com.rustam.modern_dentistry.dto.request.create.CreateTeethRequest;
import com.rustam.modern_dentistry.dto.request.read.TeethRequest;
import com.rustam.modern_dentistry.dto.request.update.UpdateTeethRequest;
import com.rustam.modern_dentistry.dto.response.read.ExaminationResponse;
import com.rustam.modern_dentistry.dto.response.read.TeethOperationResponse;
import com.rustam.modern_dentistry.dto.response.read.TeethResponse;
import com.rustam.modern_dentistry.dto.response.update.TeethUpdateResponse;
import com.rustam.modern_dentistry.exception.custom.ExistsException;
import com.rustam.modern_dentistry.exception.custom.NoTeethFoundException;
import com.rustam.modern_dentistry.exception.custom.NotFoundException;
import com.rustam.modern_dentistry.mapper.settings.teeth.TeethMapper;
import com.rustam.modern_dentistry.util.specification.settings.teeth.TeethSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeethService {

    TeethRepository teethRepository;
    TeethMapper teethMapper;

    public void create(CreateTeethRequest createTeethRequest) {
        boolean existsTeethByToothNo = existsTeethByToothNo(createTeethRequest.getToothNo());
        if (existsTeethByToothNo) {
            throw new ExistsException(("This tooth number is already available."));
        }
        Teeth teeth = Teeth.builder()
                .toothNo(createTeethRequest.getToothNo())
                .toothType(createTeethRequest.getToothType())
                .toothLocation(createTeethRequest.getToothLocation())
                .build();
        teethRepository.save(teeth);
    }

    @Transactional(readOnly = true)
    public List<TeethResponse> read() {
        List<Teeth> teeth = teethRepository.findAllWithExaminations();
        return teeth.stream()
                .map(teethEntity -> TeethResponse.builder()
                        .id(teethEntity.getId())
                        .toothNo(teethEntity.getToothNo())
                        .toothType(teethEntity.getToothType())
                        .toothLocation(teethEntity.getToothLocation())
                        .examinations(
                                teethEntity.getToothExaminations().stream()
                                        .map(te -> ExaminationResponse.builder()
                                                .id(te.getExamination().getId())
                                                .typeName(te.getExamination().getTypeName())
                                                .status(te.getExamination().getStatus())
                                                .build())
                                        .collect(Collectors.toList())
                        )
                        .operations(
                                teethEntity.getToothOperations().stream()
                                        .map(to -> TeethOperationResponse.builder()
                                                .id(to.getId())
                                                .operationName(to.getOpTypeItem().getOperationName())
                                                .status(to.getStatus())
                                                .build())
                                        .collect(Collectors.toList())
                        )
                        .build())
                .collect(Collectors.toList());
    }

    public Teeth findById(Long id) {
        return teethRepository.findById(id)
                .orElseThrow(() -> new NoTeethFoundException("no teeth found"));
    }

    public boolean existsTeethByToothNo(Long toothNo) {
        return teethRepository.existsTeethByToothNo(toothNo);
    }

    public TeethUpdateResponse update(UpdateTeethRequest updateTeethRequest) {
        Teeth teeth = findById(updateTeethRequest.getId());
        boolean existsTeethByToothNo = existsTeethByToothNo(updateTeethRequest.getToothNo());
        if (existsTeethByToothNo) {
            throw new ExistsException(("This tooth number is already available."));
        }
        if (updateTeethRequest.getToothNo() != null) {
            teeth.setToothNo(updateTeethRequest.getToothNo());
        }
        if (updateTeethRequest.getToothLocation() != null) {
            teeth.setToothLocation(updateTeethRequest.getToothLocation());
        }
        if (updateTeethRequest.getToothType() != null) {
            teeth.setToothType(updateTeethRequest.getToothType());
        }
        teethRepository.save(teeth);
        return teethMapper.toUpdateResponse(teeth);
    }

    public List<TeethResponse> search(TeethRequest teethRequest) {
        List<Teeth> teeth = teethRepository.findAll(TeethSpecification.filterBy(teethRequest.getToothNo(), teethRequest.getToothType(), teethRequest.getToothLocation()));
        return teeth.stream()
                .map(teethMapper::toTeethResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        Teeth teeth = findById(id);
        teethRepository.delete(teeth);
    }

    public List<ExaminationResponse> readAllByToothNo(Long toothNo) {
        return teethRepository.findExaminationsByToothNo(toothNo);
    }

//    public List<TeethExamination> dentalExaminationForTeethOrThrow(List<Long> toothNos) {
//        List<TeethExamination> examinations = teethRepository.dentalExaminationForTeeth(toothNos);
//        Set<Long> foundToothNos = examinations.stream()
//                .map(te -> te.getTeeth().getToothNo())
//                .collect(Collectors.toSet());
//
//        toothNos.stream()
//                .filter(toothNo -> !foundToothNos.contains(toothNo))
//                .findAny()
//                .ifPresent(missing -> {
//                    throw new NotFoundException("Tooth " + missing + " does not exist in the database!");
//                });
//
//        return examinations;
//    }

    public List<Teeth> findAllById(List<Long> teethList) {
        if (teethList == null || teethList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Long> validTeeth = teethList.stream().filter(java.util.Objects::nonNull).toList();
        if (validTeeth.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return teethRepository.findAllById(validTeeth);
    }
}
