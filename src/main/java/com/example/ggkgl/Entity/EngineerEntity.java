package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "engineer", schema = "basic_data", catalog = "")
public class EngineerEntity {
    private int id;
    private String name;
    private String link;
    private String introduce;
    private Date evaluateTime;
    private String expertCategory;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
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
    @Column(name = "link")
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Basic
    @Column(name = "introduce")
    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    @Basic
    @Column(name = "evaluate_time")
    public Date getEvaluateTime() {
        return evaluateTime;
    }

    public void setEvaluateTime(Date evaluateTime) {
        this.evaluateTime = evaluateTime;
    }

    @Basic
    @Column(name = "expert_category")
    public String getExpertCategory() {
        return expertCategory;
    }

    public void setExpertCategory(String expertCategory) {
        this.expertCategory = expertCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EngineerEntity that = (EngineerEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(link, that.link) &&
                Objects.equals(introduce, that.introduce) &&
                Objects.equals(evaluateTime, that.evaluateTime) &&
                Objects.equals(expertCategory, that.expertCategory);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, link, introduce, evaluateTime, expertCategory);
    }
}
