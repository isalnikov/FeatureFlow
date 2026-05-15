package com.featureflow.data.entity;

import com.featureflow.domain.valueobject.Role;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "team_members")
public class TeamMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Column(name = "person_id", nullable = false)
    private UUID personId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "availability_percent", nullable = false)
    private Double availabilityPercent = 1.0;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TeamEntity getTeam() { return team; }
    public void setTeam(TeamEntity team) { this.team = team; }
    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Double getAvailabilityPercent() { return availabilityPercent; }
    public void setAvailabilityPercent(Double availabilityPercent) { this.availabilityPercent = availabilityPercent; }
}
