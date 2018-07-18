package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "statistic_expert", schema = "basic_data", catalog = "")
public class StatisticExpertEntity {
    private String id;
    private String name;
    private String expertCategory;
    private String unit;
    private Integer evaluateTime;
    private String disciplineId;
    private String unitId;

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
    @Column(name = "expert_category")
    public String getExpertCategory() {
        return expertCategory;
    }

    public void setExpertCategory(String expertCategory) {
        this.expertCategory = expertCategory;
    }

    @Basic
    @Column(name = "unit")
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Basic
    @Column(name = "evaluate_time")
    public Integer getEvaluateTime() {
        return evaluateTime;
    }

    public void setEvaluateTime(Integer evaluateTime) {
        this.evaluateTime = evaluateTime;
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
    @Column(name = "unit_id")
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticExpertEntity that = (StatisticExpertEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(expertCategory, that.expertCategory) &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(evaluateTime, that.evaluateTime) &&
                Objects.equals(disciplineId, that.disciplineId) &&
                Objects.equals(unitId, that.unitId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, expertCategory, unit, evaluateTime, disciplineId, unitId);
    }
}
