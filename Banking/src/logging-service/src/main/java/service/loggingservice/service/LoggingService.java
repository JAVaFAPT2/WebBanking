package service.loggingservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import service.loggingservice.repository.LogRepository;
import service.shared.models.Log;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private final LogRepository logRepository;

    public LoggingService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Logs an INFO level message both to the console and persists it in the database.
     *
     * @param message the log message
     * @return the saved Log entity
     */
    public Log logInfo(String message) {
        Log log = new Log();
        log.setLevel("INFO");
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        // Save the log record to the database
        Log savedLog = logRepository.save(log);
        // Log to the console using SLF4J
        logger.info(message);
        return savedLog;
    }

    /**
     * Logs an ERROR level message both to the console and persists it in the database.
     *
     * @param message the log message
     * @return the saved Log entity
     */
    public Log logError(String message) {
        Log log = new Log();
        log.setLevel("ERROR");
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        // Save the log record to the database
        Log savedLog = logRepository.save(log);
        // Log to the console using SLF4J
        logger.error(message);
        return savedLog;
    }

    /**
     * Retrieves a log entry by its UUID.
     *
     * @param id the UUID of the log entry
     * @return the Log entity if found, otherwise null
     */
    public Log getLog(UUID id) {
        return logRepository.findById(id).orElse(null);
    }

    /**
     * Retrieves a log entry by its timestamp.
     *
     * @param timestamp the timestamp to search for
     * @return the Log entity if found, otherwise null
     */
    public Log getLogByDate(LocalDateTime timestamp) {
        Optional<Log> logOpt = logRepository.findByTimestamp(timestamp);
        return logOpt.orElse(null);
    }
}