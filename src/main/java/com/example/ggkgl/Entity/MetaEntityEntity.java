package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "META_ENTITY", schema = "basic_data", catalog = "")
public class MetaEntityEntity {
    private int id;
    private String name;
    private String chineseName;

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
    @Column(name = "chinese_name")
    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaEntityEntity that = (MetaEntityEntity) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(chineseName, that.chineseName);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, chineseName);
    }
}
