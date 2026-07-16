package com.example.social_network.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Bật cơ chế chạy tác vụ theo lịch (@Scheduled) cho toàn ứng dụng. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
