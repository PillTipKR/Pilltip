package com.oauth2.Drug.DrugInfo.Service;

import com.oauth2.Drug.DrugInfo.Domain.DrugEffect;
import com.oauth2.Drug.DrugInfo.Repository.DrugEffectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrugEffectService {
    private final DrugEffectRepository drugEffectRepository;

    public DrugEffectService(DrugEffectRepository drugEffectRepository) {
        this.drugEffectRepository = drugEffectRepository;
    }

    public DrugEffect findById(Long id) {
        return drugEffectRepository.findById(id).orElse(null);
    }

    public DrugEffect save(DrugEffect drugEffect) {
        return drugEffectRepository.save(drugEffect);
    }

    public List<DrugEffect> saveAll(List<DrugEffect> cautions) {
        return drugEffectRepository.saveAll(cautions);
    }
}