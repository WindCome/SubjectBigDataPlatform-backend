package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "statistic_platform", schema = "basic_data", catalog = "")
public class StatisticPlatformEntity {
    private String id;
    private String name;
    private String platformCategory;
    private String unitId;
    private Integer evaluateTime;
    private String disciplineId;

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
    @Column(name = "platform_category")
    public String getPlatformCategory() {
        return platformCategory;
    }

    public void setPlatformCategory(String platformCategory) {
        this.platformCategory = platformCategory;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticPlatformEntity that = (StatisticPlatformEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(platformCategory, that.platformCategory) &&
                Objects.equals(unitId, that.unitId) &&
                Objects.equals(evaluateTime, that.evaluateTime) &&
                Objects.equals(disciplineId, that.disciplineId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, platformCategory, unitId, evaluateTime, disciplineId);
    }
}
