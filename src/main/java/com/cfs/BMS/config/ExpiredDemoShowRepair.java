package com.cfs.BMS.config;

import com.cfs.BMS.entity.Show;
import com.cfs.BMS.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Repairs the bundled development catalogue when all sample shows have expired.
 * This is deliberately disabled outside the dev profile; production schedules must
 * be managed explicitly by an administrator.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class ExpiredDemoShowRepair implements ApplicationRunner {

    private final ShowRepository showRepository;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments args) {
        repairIfEntireDemoScheduleIsExpired();
    }

    @Transactional
    void repairIfEntireDemoScheduleIsExpired() {
        List<Show> shows = showRepository.findAll().stream()
                .sorted(Comparator.comparing(Show::getShowDate).thenComparing(Show::getStartTime))
                .toList();
        if (shows.isEmpty() || shows.stream().anyMatch(this::isFuture)) {
            return;
        }

        LocalDate nextDate = LocalDate.now(clock).plusDays(1);
        TreeSet<LocalDate> originalDates = new TreeSet<>(shows.stream().map(Show::getShowDate).toList());
        for (Show show : shows) {
            long offset = originalDates.headSet(show.getShowDate()).size();
            show.setShowDate(nextDate.plusDays(offset));
        }
        showRepository.saveAll(shows);
        log.info("Moved {} expired development shows to {} and later", shows.size(), nextDate);
    }

    private boolean isFuture(Show show) {
        return LocalDateTime.of(show.getShowDate(), show.getStartTime())
                .isAfter(LocalDateTime.now(clock));
    }
}
