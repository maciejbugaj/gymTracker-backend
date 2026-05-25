package com.gymtracker.gym.templateExecrcises.service;

import com.gymtracker.gym.templateExecrcises.repository.TemplateExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TemplateExerciseService {

    private TemplateExerciseRepository templateExerciseRepository;
}
