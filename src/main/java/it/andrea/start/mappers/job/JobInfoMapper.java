package it.andrea.start.mappers.job;

import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import it.andrea.start.dto.JobInfoDTO;
import it.andrea.start.error.exception.mapping.MappingToEntityException;
import it.andrea.start.mappers.AbstractMapper;
import it.andrea.start.models.job.JobInfo;
import jakarta.persistence.EntityManager;

@Component
public class JobInfoMapper extends AbstractMapper<JobInfoDTO, JobInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(JobInfoMapper.class);

    public JobInfoMapper(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public JobInfoDTO toDto(JobInfo entity) {
        if (entity == null) {
            return null;
        }
        JobInfoDTO dto = new JobInfoDTO();

        dto.setJobName(entity.getJobName());
        dto.setJobGroup(entity.getJobGroup());
        dto.setDescription(entity.getDescription());
        dto.setJobClass(entity.getJobClass());

        dto.setIsActive(entity.isActive());
        dto.setCronJob(entity.isCronJob());

        dto.setCronExpression(entity.getCronExpression());
        dto.setRepeatIntervalMillis(entity.getRepeatIntervalMillis());
        dto.setInitialDelayMillis(entity.getInitialDelayMillis());
        dto.setRepeatCount(entity.getRepeatCount());

        dto.setJobDataMapJson(entity.getJobDataMapJson());

        return dto;
    }

    @Override
    public void toEntity(JobInfoDTO dto, JobInfo entity) {
        if (dto == null || entity == null) {
            LOG.warn("Tentativo di mappare da DTO a Entità con input nullo.");
            if (dto == null) {
                throw new MappingToEntityException("Il DTO sorgente non può essere nullo.");
            }
            throw new MappingToEntityException("L'entità destinazione non può essere nulla.");
        }

        entity.setDescription(dto.getDescription());
        entity.setJobClass(dto.getJobClass());

        entity.setActive(dto.getIsActive());
        entity.setCronJob(dto.getCronJob());

        entity.setCronExpression(dto.getCronExpression());
        entity.setRepeatIntervalMillis(dto.getRepeatIntervalMillis());
        entity.setInitialDelayMillis(dto.getInitialDelayMillis());

        if (dto.getRepeatCount() != null) {
            entity.setRepeatCount(dto.getRepeatCount());
        } else {
            entity.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            LOG.debug("RepeatCount nullo nel DTO, impostato valore di default REPEAT_INDEFINITELY (-1) nell'entità.");
        }

        entity.setJobDataMapJson(dto.getJobDataMapJson());
    }

}