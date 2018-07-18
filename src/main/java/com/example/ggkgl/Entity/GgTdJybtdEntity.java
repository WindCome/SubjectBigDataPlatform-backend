package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_TD_JYBTD", schema = "basic_data", catalog = "")
public class GgTdJybtdEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String zsbh;
    private String cydws;
    private String xxmc;
    private String xxdm;
    private String fzr;
    private String yjfx;
    private String zzsj;
    private String zzjf;
    private String pdsj;

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
    @Column(name = "ZSBH")
    public String getZsbh() {
        return zsbh;
    }

    public void setZsbh(String zsbh) {
        this.zsbh = zsbh;
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
    @Column(name = "FZR")
    public String getFzr() {
        return fzr;
    }

    public void setFzr(String fzr) {
        this.fzr = fzr;
    }

    @Basic
    @Column(name = "YJFX")
    public String getYjfx() {
        return yjfx;
    }

    public void setYjfx(String yjfx) {
        this.yjfx = yjfx;
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
    @Column(name = "ZZJF")
    public String getZzjf() {
        return zzjf;
    }

    public void setZzjf(String zzjf) {
        this.zzjf = zzjf;
    }

    @Basic
    @Column(name = "PDSJ")
    public String getPdsj() {
        return pdsj;
    }

    public void setPdsj(String pdsj) {
        this.pdsj = pdsj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GgTdJybtdEntity that = (GgTdJybtdEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(zsbh, that.zsbh) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(fzr, that.fzr) &&
                Objects.equals(yjfx, that.yjfx) &&
                Objects.equals(zzsj, that.zzsj) &&
                Objects.equals(zzjf, that.zzjf) &&
                Objects.equals(pdsj, that.pdsj);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, zsbh, cydws, xxmc, xxdm, fzr, yjfx, zzsj, zzjf, pdsj);
    }
}
