package com.gymtracker.gym.templateExecrcises.service;

import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.templateExecrcises.repository.TemplateExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateExerciseService {

    private TemplateExerciseRepository templateExerciseRepository;
}
