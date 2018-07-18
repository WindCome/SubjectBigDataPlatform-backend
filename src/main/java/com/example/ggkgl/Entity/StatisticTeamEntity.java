package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "statistic_team", schema = "basic_data", catalog = "")
public class StatisticTeamEntity {
    private String id;
    private String name;
    private String teamCategory;
    private String unitId;
    private String disciplineId;
    private Integer evaluateTime;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "team_category")
    public String getTeamCategory() {
        return teamCategory;
    }

    public void setTeamCategory(String teamCategory) {
        this.teamCategory = teamCategory;
    }

    @Basic
    @Column(name = "unit_id")
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    @Basic
    @Column(name = "discipline_id")
    public String getDisciplineId() {
        return disciplineId;
    }

    public void setDisciplineId(String disciplineId) {
        this.disciplineId = disciplineId;
    }

    @Basic
    @Column(name = "evaluate_time")
    public Integer getEvaluateTime() {
        return evaluateTime;
    }

    public void setEvaluateTime(Integer evaluateTime) {
        this.evaluateTime = evaluateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticTeamEntity that = (StatisticTeamEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(teamCategory, that.teamCategory) &&
                Objects.equals(unitId, that.unitId) &&
                Objects.equals(disciplineId, that.disciplineId) &&
                Objects.equals(evaluateTime, that.evaluateTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, teamCategory, unitId, disciplineId, evaluateTime);
    }
}
