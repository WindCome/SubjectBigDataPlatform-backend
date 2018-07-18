package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "GG_ZJ_JYBRC", schema = "basic_data", catalog = "")
public class GgZjJybrcEntity {
    private String id;
    private String modifyUserId;
    private Timestamp modifyTime;
    private String seqNo;
    private String zsbh;
    private String xm;
    private String cydws;
    private String xmmc;
    private String xxdm;
    private String zzjf;
    private String zzsj;
    private String pdsj;
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
    @Column(name = "ZSBH")
    public String getZsbh() {
        return zsbh;
    }

    public void setZsbh(String zsbh) {
        this.zsbh = zsbh;
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
    @Column(name = "ZZJF")
    public String getZzjf() {
        return zzjf;
    }

    public void setZzjf(String zzjf) {
        this.zzjf = zzjf;
    }

    @Basic
    @Column(name = "ZZSJ")
    public String getZzsj() {
        return zzsj;
    }

    public void setZzsj(String zzsj) {
        this.zzsj = zzsj;
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
        GgZjJybrcEntity that = (GgZjJybrcEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(zsbh, that.zsbh) &&
                Objects.equals(xm, that.xm) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xmmc, that.xmmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(zzjf, that.zzjf) &&
                Objects.equals(zzsj, that.zzsj) &&
                Objects.equals(pdsj, that.pdsj) &&
                Objects.equals(lb, that.lb);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, zsbh, xm, cydws, xmmc, xxdm, zzjf, zzsj, pdsj, lb);
    }
}
