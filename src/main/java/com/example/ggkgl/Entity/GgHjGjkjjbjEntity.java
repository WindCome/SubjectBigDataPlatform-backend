package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_HJ_GJKJJBJ", schema = "basic_data", catalog = "")
public class GgHjGjkjjbjEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String xmbh;
    private String xmmc;
    private String wcr;
    private String tjdw;
    private String pdsj;
    private String hjdj;
    private String cydws;
    private String xxmc;
    private String xxdm;
    private String xmmcZz;

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
    @Column(name = "XMBH")
    public String getXmbh() {
        return xmbh;
    }

    public void setXmbh(String xmbh) {
        this.xmbh = xmbh;
    }

    @Basic
    @Column(name = "XMMC")
    public String getXmmc() {
        return xmmc;
    }

    public void setXmmc(String xmmc) {
        this.xmmc = xmmc;
    }

    @Basic
    @Column(name = "WCR")
    public String getWcr() {
        return wcr;
    }

    public void setWcr(String wcr) {
        this.wcr = wcr;
    }

    @Basic
    @Column(name = "TJDW")
    public String getTjdw() {
        return tjdw;
    }

    public void setTjdw(String tjdw) {
        this.tjdw = tjdw;
    }

    @Basic
    @Column(name = "PDSJ")
    public String getPdsj() {
        return pdsj;
    }

    public void setPdsj(String pdsj) {
        this.pdsj = pdsj;
    }

    @Basic
    @Column(name = "HJDJ")
    public String getHjdj() {
        return hjdj;
    }

    public void setHjdj(String hjdj) {
        this.hjdj = hjdj;
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
    @Column(name = "XMMC_ZZ")
    public String getXmmcZz() {
        return xmmcZz;
    }

    public void setXmmcZz(String xmmcZz) {
        this.xmmcZz = xmmcZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgHjGjkjjbjEntity that = (GgHjGjkjjbjEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(xmbh, that.xmbh) &&
                Objects.equals(xmmc, that.xmmc) &&
                Objects.equals(wcr, that.wcr) &&
                Objects.equals(tjdw, that.tjdw) &&
                Objects.equals(pdsj, that.pdsj) &&
                Objects.equals(hjdj, that.hjdj) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(xmmcZz, that.xmmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, xmbh, xmmc, wcr, tjdw, pdsj, hjdj, cydws, xxmc, xxdm, xmmcZz);
    }
}
