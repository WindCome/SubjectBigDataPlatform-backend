package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_PT_GJSYZX", schema = "basic_data", catalog = "")
public class GgPtGjsyzxEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String cydws;
    private String zxmc;
    private String lb;
    private String nf;
    private String xx;
    private String xxdm;
    private String dqjd;
    private String zxmcZz;

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
    public String getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(String modifyTime) {
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
    @Column(name = "CYDWS")
    public String getCydws() {
        return cydws;
    }

    public void setCydws(String cydws) {
        this.cydws = cydws;
    }

    @Basic
    @Column(name = "ZXMC")
    public String getZxmc() {
        return zxmc;
    }

    public void setZxmc(String zxmc) {
        this.zxmc = zxmc;
    }

    @Basic
    @Column(name = "LB")
    public String getLb() {
        return lb;
    }

    public void setLb(String lb) {
        this.lb = lb;
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
    @Column(name = "XX")
    public String getXx() {
        return xx;
    }

    public void setXx(String xx) {
        this.xx = xx;
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
    @Column(name = "DQJD")
    public String getDqjd() {
        return dqjd;
    }

    public void setDqjd(String dqjd) {
        this.dqjd = dqjd;
    }

    @Basic
    @Column(name = "ZXMC_ZZ")
    public String getZxmcZz() {
        return zxmcZz;
    }

    public void setZxmcZz(String zxmcZz) {
        this.zxmcZz = zxmcZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgPtGjsyzxEntity that = (GgPtGjsyzxEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(zxmc, that.zxmc) &&
                Objects.equals(lb, that.lb) &&
                Objects.equals(nf, that.nf) &&
                Objects.equals(xx, that.xx) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(dqjd, that.dqjd) &&
                Objects.equals(zxmcZz, that.zxmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, cydws, zxmc, lb, nf, xx, xxdm, dqjd, zxmcZz);
    }
}
