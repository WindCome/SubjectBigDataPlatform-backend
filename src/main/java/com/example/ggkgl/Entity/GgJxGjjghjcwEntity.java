package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_JX_GJJGHJCW", schema = "basic_data", catalog = "")
public class GgJxGjjghjcwEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String sm;
    private String zyzz;
    private String cydws;
    private String cbs;
    private String bz;
    private String bzlx;
    private String xxmc;
    private String xxdm;
    private String smZz;

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
    @Column(name = "SM")
    public String getSm() {
        return sm;
    }

    public void setSm(String sm) {
        this.sm = sm;
    }

    @Basic
    @Column(name = "ZYZZ")
    public String getZyzz() {
        return zyzz;
    }

    public void setZyzz(String zyzz) {
        this.zyzz = zyzz;
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
    @Column(name = "CBS")
    public String getCbs() {
        return cbs;
    }

    public void setCbs(String cbs) {
        this.cbs = cbs;
    }

    @Basic
    @Column(name = "BZ")
    public String getBz() {
        return bz;
    }

    public void setBz(String bz) {
        this.bz = bz;
    }

    @Basic
    @Column(name = "BZLX")
    public String getBzlx() {
        return bzlx;
    }

    public void setBzlx(String bzlx) {
        this.bzlx = bzlx;
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
    @Column(name = "SM_ZZ")
    public String getSmZz() {
        return smZz;
    }

    public void setSmZz(String smZz) {
        this.smZz = smZz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgJxGjjghjcwEntity that = (GgJxGjjghjcwEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(sm, that.sm) &&
                Objects.equals(zyzz, that.zyzz) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(cbs, that.cbs) &&
                Objects.equals(bz, that.bz) &&
                Objects.equals(bzlx, that.bzlx) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(smZz, that.smZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, sm, zyzz, cydws, cbs, bz, bzlx, xxmc, xxdm, smZz);
    }
}
