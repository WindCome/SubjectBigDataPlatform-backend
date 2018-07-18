package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "GG_ZJ_BQWRC", schema = "basic_data", catalog = "")
public class GgZjBqwrcEntity {
    private String id;
    private String modifyUserId;
    private Timestamp modifyTime;
    private String seqNo;
    private String nf;
    private String xm;
    private String xxmc;
    private String xxdm;
    private String rs;
    private String zsbh;
    private String lb;

    @Id
    @Column(name = "ID")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "MODIFY_USER_ID")
    public String getModifyUserId() {
        return modifyUserId;
    }

    public void setModifyUserId(String modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    @Basic
    @Column(name = "MODIFY_TIME")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "SEQ_NO")
    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    @Basic
    @Column(name = "NF")
    public String getNf() {
        return nf;
    }

    public void setNf(String nf) {
        this.nf = nf;
    }

    @Basic
    @Column(name = "XM")
    public String getXm() {
        return xm;
    }

    public void setXm(String xm) {
        this.xm = xm;
    }

    @Basic
    @Column(name = "XXMC")
    public String getXxmc() {
        return xxmc;
    }

    public void setXxmc(String xxmc) {
        this.xxmc = xxmc;
    }

    @Basic
    @Column(name = "XXDM")
    public String getXxdm() {
        return xxdm;
    }

    public void setXxdm(String xxdm) {
        this.xxdm = xxdm;
    }

    @Basic
    @Column(name = "RS")
    public String getRs() {
        return rs;
    }

    public void setRs(String rs) {
        this.rs = rs;
    }

    @Basic
    @Column(name = "ZSBH")
    public String getZsbh() {
        return zsbh;
    }

    public void setZsbh(String zsbh) {
        this.zsbh = zsbh;
    }

    @Basic
    @Column(name = "LB")
    public String getLb() {
        return lb;
    }

    public void setLb(String lb) {
        this.lb = lb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgZjBqwrcEntity that = (GgZjBqwrcEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(nf, that.nf) &&
                Objects.equals(xm, that.xm) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(rs, that.rs) &&
                Objects.equals(zsbh, that.zsbh) &&
                Objects.equals(lb, that.lb);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, nf, xm, xxmc, xxdm, rs, zsbh, lb);
    }
}
