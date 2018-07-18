package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "GG_ZJ_XDNYKXJ", schema = "basic_data", catalog = "")
public class GgZjXdnykxjEntity {
    private String id;
    private String modifyUserId;
    private Timestamp modifyTime;
    private String seqNo;
    private String zwmc;
    private String sysmc;
    private String sxkxj;
    private String cydws;
    private String xmmc;
    private String xxdm;
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
    @Column(name = "ZWMC")
    public String getZwmc() {
        return zwmc;
    }

    public void setZwmc(String zwmc) {
        this.zwmc = zwmc;
    }

    @Basic
    @Column(name = "SYSMC")
    public String getSysmc() {
        return sysmc;
    }

    public void setSysmc(String sysmc) {
        this.sysmc = sysmc;
    }

    @Basic
    @Column(name = "SXKXJ")
    public String getSxkxj() {
        return sxkxj;
    }

    public void setSxkxj(String sxkxj) {
        this.sxkxj = sxkxj;
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
    @Column(name = "XMMC")
    public String getXmmc() {
        return xmmc;
    }

    public void setXmmc(String xmmc) {
        this.xmmc = xmmc;
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
        GgZjXdnykxjEntity that = (GgZjXdnykxjEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(zwmc, that.zwmc) &&
                Objects.equals(sysmc, that.sysmc) &&
                Objects.equals(sxkxj, that.sxkxj) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xmmc, that.xmmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(lb, that.lb);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, zwmc, sysmc, sxkxj, cydws, xmmc, xxdm, lb);
    }
}
