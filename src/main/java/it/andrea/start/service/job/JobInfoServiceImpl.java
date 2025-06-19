package it.andrea.start.service.job;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.andrea.start.dto.JobInfoDTO;
import it.andrea.start.error.exception.job.JobControlException;
import it.andrea.start.error.exception.job.JobNotFoundException;
import it.andrea.start.error.exception.job.JobSchedulingException;
import it.andrea.start.error.exception.mapping.MappingToDtoException;
import it.andrea.start.mappers.job.JobInfoMapper;
import it.andrea.start.models.JobInfo;
import it.andrea.start.repository.JobInfoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobInfoServiceImpl implements JobInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(JobInfoServiceImpl.class);

    @Lazy
    private final Scheduler scheduler;
    private final JobInfoRepository jobInfoRepository;
    private final JobInfoMapper jobInfoMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public Collection<JobInfoDTO> listJobs() throws MappingToDtoException {
        List<JobInfo> jobs = jobInfoRepository.findAll();
        return jobInfoMapper.toDtos(jobs);
    }

    @PostConstruct
    @Transactional
    public void initializeScheduledJobs() {
        LOG.info("Avvio inizializzazione Job Quartz...");
        List<JobInfo> activeJobs = jobInfoRepository.findByIsActiveTrue();
        LOG.info("Trovati {} job attivi nel database da schedulare/verificare.", activeJobs.size());
        int scheduledCount = 0;
        int errorCount = 0;
        for (JobInfo jobInfo : activeJobs) {
            try {
                scheduleOrUpdateJobInternal(jobInfo);
                scheduledCount++;
            } catch (Exception e) {
                errorCount++;
                LOG.error("Errore durante la schedulazione iniziale del job {}/{}: {}",
                        jobInfo.getJobGroup(), jobInfo.getJobName(), e.getMessage(), e);
            }
        }
        LOG.info("Inizializzazione Job Quartz completata. Schedulati/Verificati: {}, Errori: {}", scheduledCount, errorCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleNewJob(String jobName, String jobGroup) {
        LOG.info("Tentativo di schedulare un nuovo job: {}/{}", jobGroup, jobName);
        JobInfo jobInfo = findJobInfoOrThrow(jobName, jobGroup);

        if (!jobInfo.isActive()) {
            LOG.warn("Il job {}/{} è marcato come inattivo nel DB. Non verrà schedulato. Aggiornare 'isActive' a true e riprovare se necessario.", jobGroup, jobName);
            throw new IllegalStateException("Il job " + jobGroup + "/" + jobName + " è inattivo e non può essere schedulato.");
        }

        try {
            scheduleOrUpdateJobInternal(jobInfo);
            LOG.info("Job {}/{} schedulato con successo.", jobGroup, jobName);
        } catch (Exception e) {
            LOG.error("Errore durante la schedulazione del job {}/{}: {}", jobGroup, jobName, e.getMessage(), e);
            throw new JobSchedulingException(jobGroup, jobName);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScheduleJob(String jobName, String jobGroup) {
        LOG.info("Tentativo di aggiornare la schedulazione per il job: {}/{}", jobGroup, jobName);
        JobInfo jobInfo = findJobInfoOrThrow(jobName, jobGroup);

        if (Boolean.TRUE.equals(jobInfo.isActive())) {
            LOG.info("Job {}/{} è attivo nel DB. Schedulazione/aggiornamento nello scheduler...", jobGroup, jobName);
            try {
                scheduleOrUpdateJobInternal(jobInfo);
                LOG.info("Schedulazione job {}/{} aggiornata con successo.", jobGroup, jobName);
            } catch (Exception e) {
                LOG.error("Errore durante l'aggiornamento della schedulazione del job {}/{}: {}", jobGroup, jobName, e.getMessage(), e);
                throw new JobSchedulingException(jobGroup, jobName);
            }
        } else {
            LOG.info("Job {}/{} è inattivo nel DB. Rimozione dallo scheduler (se esistente)...", jobGroup, jobName);
            deleteJobInternal(jobInfo.getJobName(), jobInfo.getJobGroup());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unScheduleJob(String jobName, String jobGroup) {
        LOG.info("Tentativo di de-schedulare (rimuovere e marcare come inattivo) il job: {}/{}", jobGroup, jobName);

        Optional<JobInfo> jobInfoOpt = jobInfoRepository.findByJobNameAndJobGroup(jobName, jobGroup);
        if (jobInfoOpt.isPresent()) {
            JobInfo jobInfo = jobInfoOpt.get();
            if (Boolean.TRUE.equals(jobInfo.isActive())) {
                jobInfo.setActive(false);
                jobInfoRepository.save(jobInfo);
                LOG.info("JobInfo {}/{} marcato come inattivo nel database.", jobGroup, jobName);
            } else {
                LOG.info("JobInfo {}/{} era già marcato come inattivo nel database.", jobGroup, jobName);
            }
        } else {
            LOG.warn("JobInfo non trovato per {}/{}. Impossibile aggiornare lo stato isActive nel DB.", jobGroup, jobName);
        }

        deleteJobInternal(jobName, jobGroup);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(String jobName, String jobGroup) {
        LOG.info("Tentativo di cancellare completamente il job: {}/{}", jobGroup, jobName);

        deleteJobInternal(jobName, jobGroup);

        Optional<JobInfo> jobInfoOpt = jobInfoRepository.findByJobNameAndJobGroup(jobName, jobGroup);
        if (jobInfoOpt.isPresent()) {
            jobInfoRepository.delete(jobInfoOpt.get());
            LOG.info("JobInfo {}/{} cancellato dal database.", jobGroup, jobName);
        } else {
            LOG.warn("JobInfo {}/{} non trovato nel database per la cancellazione.", jobGroup, jobName);
        }
    }

    @Override
    public void pauseJob(String jobName, String jobGroup) throws JobControlException {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        LOG.info("Tentativo di mettere in pausa il job: {}", jobKey);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                LOG.info("Job {} messo in pausa con successo.", jobKey);
            } else {
                LOG.warn("Tentativo di mettere in pausa un job non esistente nello scheduler: {}", jobKey);
            }
        } catch (SchedulerException e) {
            LOG.error("Errore durante la pausa del job {}: {}", jobKey, e.getMessage(), e);
            throw new JobControlException(jobName, jobGroup);
        }
    }

    @Override
    public void resumeJob(String jobName, String jobGroup) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        LOG.info("Tentativo di riprendere il job: {}", jobKey);
        try {
            Optional<JobInfo> jobInfoOpt = jobInfoRepository.findByJobNameAndJobGroup(jobName, jobGroup);
            if (jobInfoOpt.isEmpty() || !Boolean.TRUE.equals(jobInfoOpt.get().isActive())) {
                LOG.warn("Impossibile riprendere il job {}: non trovato nel DB o marcato come inattivo.", jobKey);
                throw new IllegalStateException("Cannot resume job " + jobKey + " as it is not found or inactive in the database.");
            }

            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
                LOG.info("Job {} ripreso con successo.", jobKey);
            } else {
                LOG.warn("Tentativo di riprendere un job non esistente nello scheduler: {}. Rischedulo il job.", jobKey);
                scheduleOrUpdateJobInternal(jobInfoOpt.get());
            }
        } catch (SchedulerException e) {
            LOG.error("Errore Scheduler durante la ripresa del job {}: {}", jobKey, e.getMessage(), e);
            throw new JobControlException(jobName, jobGroup);
        } catch (Exception e) {
            LOG.error("Errore generico durante la ripresa del job {}: {}", jobKey, e.getMessage(), e);
            throw new RuntimeException("Errore generico ripresa job " + jobKey, e);
        }
    }

    @Override
    public void startJobNow(String jobName, String jobGroup) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        LOG.info("Tentativo di avviare immediatamente (trigger) il job: {}", jobKey);
        try {
            Optional<JobInfo> jobInfoOpt = jobInfoRepository.findByJobNameAndJobGroup(jobName, jobGroup);
            if (jobInfoOpt.isEmpty() || !Boolean.TRUE.equals(jobInfoOpt.get().isActive())) {
                LOG.warn("Impossibile avviare il job {}: non trovato nel DB o marcato come inattivo.", jobKey);
                throw new IllegalStateException("Cannot trigger job " + jobKey + " as it is not found or inactive in the database.");
            }

            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey);
                LOG.info("Job {} avviato manualmente (triggerato) con successo.", jobKey);
            } else {
                LOG.warn("Tentativo di avviare manualmente un job non esistente nello scheduler: {}. Schedularlo prima se necessario.", jobKey);
            }
        } catch (SchedulerException e) {
            LOG.error("Errore Scheduler durante l'avvio manuale del job {}: {}", jobKey, e.getMessage(), e);
            throw new JobControlException(jobGroup, jobName);
        } catch (Exception e) {
            LOG.error("Errore generico durante l'avvio manuale del job {}: {}", jobKey, e.getMessage(), e);
            throw new RuntimeException("Errore generico avvio manuale job " + jobKey, e);
        }
    }

    private JobInfo findJobInfoOrThrow(String jobName, String jobGroup) throws JobNotFoundException {
        return jobInfoRepository.findByJobNameAndJobGroup(jobName, jobGroup)
                .orElseThrow(() -> {
                    LOG.error("JobInfo non trovato per {}/{}", jobGroup, jobName);
                    return new JobNotFoundException(jobGroup, jobName);
                });
    }

    private void scheduleOrUpdateJobInternal(JobInfo jobInfo) throws SchedulerException, ClassNotFoundException {
        JobKey jobKey = JobKey.jobKey(jobInfo.getJobName(), jobInfo.getJobGroup());
        LOG.debug("Preparazione schedulazione/aggiornamento per job {}", jobKey);

        JobDetail jobDetail = buildJobDetail(jobInfo, jobKey);
        Trigger trigger = buildTrigger(jobInfo, jobKey);

        boolean jobExists = scheduler.checkExists(jobKey);

        if (jobExists) {
            LOG.info("Job {} esiste già nello scheduler. Verrà rimosso e ricreato per applicare la configurazione corrente.", jobKey);
            boolean deleted = scheduler.deleteJob(jobKey);
            if (!deleted) {
                LOG.warn("Non è stato possibile rimuovere il job {} esistente prima di aggiornarlo.", jobKey);
            }
        } else {
            LOG.info("Schedulazione nuovo job: {}", jobKey);
        }

        Date scheduledTime = scheduler.scheduleJob(jobDetail, trigger);
        LOG.info("Job {} schedulato/aggiornato nello scheduler. Prossima esecuzione (stimata): {}", jobKey, scheduledTime);
    }

    private void deleteJobInternal(String jobName, String jobGroup) {
        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            if (scheduler.checkExists(jobKey)) {
                boolean deleted = scheduler.deleteJob(jobKey);
                if (deleted) {
                    LOG.info("Job {} cancellato con successo dallo scheduler.", jobKey);
                } else {
                    LOG.warn("Il job {} è stato trovato nello scheduler ma deleteJob() ha restituito false.", jobKey);
                }
            } else {
                LOG.info("Tentativo di cancellare un job non esistente nello scheduler: {}. Nessuna azione necessaria.", jobKey);
            }
        } catch (SchedulerException e) {
            LOG.error("Errore durante la cancellazione del job {} dallo scheduler: {}", jobKey, e.getMessage(), e);
            throw new JobControlException(jobGroup, jobName);
        }
    }

    private JobDetail buildJobDetail(JobInfo jobInfo, JobKey jobKey) throws ClassNotFoundException {
        JobDataMap jobDataMap = new JobDataMap();

        if (jobInfo.getJobDataMapJson() != null && !jobInfo.getJobDataMapJson().isBlank()) {
            try {
                Map<String, Object> data = objectMapper.readValue(jobInfo.getJobDataMapJson(), new TypeReference<>() {
                });
                jobDataMap.putAll(data);
                LOG.debug("JobDataMap popolata per {} da JSON: {}", jobKey, data);
            } catch (Exception e) {
                LOG.error("ERRORE nel parsing del JSON JobDataMap per {}: {}. JobDataMap potrebbe essere incompleta o vuota. JSON: '{}'",
                        jobKey, e.getMessage(), jobInfo.getJobDataMapJson(), e);
            }
        } else {
            LOG.debug("Nessun JobDataMap JSON fornito per {}", jobKey);
        }

        jobDataMap.put("jobName", jobInfo.getJobName());
        jobDataMap.put("jobGroup", jobInfo.getJobGroup());

        Class<? extends Job> jobClass;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Job> loadedClass = (Class<? extends Job>) Class.forName(jobInfo.getJobClass());
            jobClass = loadedClass;
        } catch (ClassNotFoundException e) {
            LOG.error("Classe Job non trovata: '{}' per job {}", jobInfo.getJobClass(), jobKey);
            throw e;
        }

        LOG.debug("Costruzione JobDetail per {} con classe {}", jobKey, jobClass.getName());
        
        // @formatter:off
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)
                .withDescription(jobInfo.getDescription())
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
        // @formatter:on
    }

    private Trigger buildTrigger(JobInfo jobInfo, JobKey jobKey) {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobName() + "-trigger", jobInfo.getJobGroup());
        String triggerDescription = "Trigger per " + jobKey.toString();

        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .withDescription(triggerDescription)
                .forJob(jobKey);

        if (jobInfo.isCronJob()) {
            if (jobInfo.getCronExpression() == null || jobInfo.getCronExpression().isBlank()) {
                LOG.error("Espressione CRON mancante per il job CRON {}", jobKey);
                throw new IllegalArgumentException("Espressione CRON mancante per il job " + jobKey);
            }
            if (!CronExpression.isValidExpression(jobInfo.getCronExpression())) {
                LOG.error("Espressione CRON non valida ('{}') per il job CRON {}", jobInfo.getCronExpression(), jobKey);
                throw new IllegalArgumentException("Espressione CRON non valida: " + jobInfo.getCronExpression());
            }

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(jobInfo.getCronExpression()) //
                    .withMisfireHandlingInstructionFireAndProceed(); 

            LOG.debug("Costruzione CronTrigger per {} con espressione '{}' e misfire policy '{}'",
                    jobKey, jobInfo.getCronExpression(), "FireAndProceed");

            triggerBuilder.withSchedule(scheduleBuilder);
        } else {
            if (jobInfo.getRepeatIntervalMillis() == null || jobInfo.getRepeatIntervalMillis() <= 0) {
                LOG.error("L'intervallo di ripetizione è nullo o non valido ({}) per il Simple job {}", jobInfo.getRepeatIntervalMillis(), jobKey);
                throw new IllegalArgumentException("Intervallo di ripetizione mancante o non valido per il job " + jobKey);
            }

            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule() //
                    .withIntervalInMilliseconds(jobInfo.getRepeatIntervalMillis());

            Integer repeatCountConfig = jobInfo.getRepeatCount(); 

            if (repeatCountConfig == null || repeatCountConfig < 0) { 
                scheduleBuilder.repeatForever();
                LOG.debug("SimpleTrigger per {} ripeterà all'infinito (intervallo {} ms).", jobKey, jobInfo.getRepeatIntervalMillis());
            } else if (repeatCountConfig == 0) {
                scheduleBuilder.withRepeatCount(0);
                LOG.debug("SimpleTrigger per {} eseguirà una sola volta (repeat count config 0, Quartz repeat count 0).", jobKey);
            } else {
                scheduleBuilder.withRepeatCount(repeatCountConfig - 1);
                LOG.debug("SimpleTrigger per {} eseguirà un totale di {} volte (repeat count config {}, Quartz repeat count {}).", jobKey,
                        repeatCountConfig, repeatCountConfig, repeatCountConfig - 1);
            }

            scheduleBuilder.withMisfireHandlingInstructionFireNow();
            LOG.debug("SimpleTrigger per {} userà misfire policy '{}'", jobKey, "FireNow");

            triggerBuilder.withSchedule(scheduleBuilder);

            if (jobInfo.getInitialDelayMillis() != null && jobInfo.getInitialDelayMillis() > 0) {
                Date startTime = new Date(System.currentTimeMillis() + jobInfo.getInitialDelayMillis());
                triggerBuilder.startAt(startTime);
                LOG.debug("Trigger per {} avrà un ritardo iniziale, partenza schedulata alle {}", jobKey, startTime);
            } else {
                triggerBuilder.startNow();
                LOG.debug("Trigger per {} partirà immediatamente (nessun ritardo iniziale).", jobKey);
            }
        }

        return triggerBuilder.build();
    }
}