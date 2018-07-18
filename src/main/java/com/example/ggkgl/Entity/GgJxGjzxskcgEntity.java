package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_JX_GJZXSKCG", schema = "basic_data", catalog = "")
public class GgJxGjzxskcgEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String xk;
    private String cgmc;
    private String sqr;
    private String sbdw;
    private String xxdm;
    private String nf;
    private String cgmcZz;

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
    @Column(name = "XK")
    public String getXk() {
        return xk;
    }

    public void setXk(String xk) {
        this.xk = xk;
    }

    @Basic
    @Column(name = "CGMC")
    public String getCgmc() {
        return cgmc;
    }

    public void setCgmc(String cgmc) {
        this.cgmc = cgmc;
    }

    @Basic
    @Column(name = "SQR")
    public String getSqr() {
        return sqr;
    }

    public void setSqr(String sqr) {
        this.sqr = sqr;
    }

    @Basic
    @Column(name = "SBDW")
    public String getSbdw() {
        return sbdw;
    }

    public void setSbdw(String sbdw) {
        this.sbdw = sbdw;
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
    @Column(name = "NF")
    public String getNf() {
        return nf;
    }

    public void setNf(String nf) {
        this.nf = nf;
    }

    @Basic
    @Column(name = "CGMC_ZZ")
    public String getCgmcZz() {
        return cgmcZz;
    }

    public void setCgmcZz(String cgmcZz) {
        this.cgmcZz = cgmcZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgJxGjzxskcgEntity that = (GgJxGjzxskcgEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(xk, that.xk) &&
                Objects.equals(cgmc, that.cgmc) &&
                Objects.equals(sqr, that.sqr) &&
                Objects.equals(sbdw, that.sbdw) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(nf, that.nf) &&
                Objects.equals(cgmcZz, that.cgmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, xk, cgmc, sqr, sbdw, xxdm, nf, cgmcZz);
    }
}
