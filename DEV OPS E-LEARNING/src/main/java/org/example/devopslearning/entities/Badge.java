package org.example.devopslearning.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "badges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    // Codes de badges prédéfinis utilisés par le BadgeEngineService
    public static final String PARTICIPATION_ACTIVE = "PARTICIPATION_ACTIVE";
    public static final String REGULARITE = "REGULARITE";
    public static final String MAITRISE_COURS = "MAITRISE_COURS";
}