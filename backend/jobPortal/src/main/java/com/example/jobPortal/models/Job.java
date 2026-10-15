package com.example.jobPortal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "jobs")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String title;

    private String description;

    private String location;

    private Double salary;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    private Integer experienceRequired;

    private LocalDateTime postedDate;

    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @ManyToMany
    @JoinTable(
            name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Application> applications;

    @PrePersist
    protected void onCreate()
    {
        postedDate = LocalDateTime.now();
        status = JobStatus.ACTIVE;
    }
}
