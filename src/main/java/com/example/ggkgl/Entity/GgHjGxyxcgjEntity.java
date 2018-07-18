package com.example.ggkgl.Entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GG_HJ_GXYXCGJ", schema = "basic_data", catalog = "")
public class GgHjGxyxcgjEntity {
    private String id;
    private String modifyUserId;
    private String modifyTime;
    private String seqNo;
    private String zsbh;
    private String bz;
    private String jzmc;
    private String dj;
    private String xmmc;
    private String zywcr;
    private String pdsj;
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
    @Column(name = "ZSBH")
    public String getZsbh() {
        return zsbh;
    }

    public void setZsbh(String zsbh) {
        this.zsbh = zsbh;
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
    @Column(name = "JZMC")
    public String getJzmc() {
        return jzmc;
    }

    public void setJzmc(String jzmc) {
        this.jzmc = jzmc;
    }

    @Basic
    @Column(name = "DJ")
    public String getDj() {
        return dj;
    }

    public void setDj(String dj) {
        this.dj = dj;
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
    @Column(name = "ZYWCR")
    public String getZywcr() {
        return zywcr;
    }

    public void setZywcr(String zywcr) {
        this.zywcr = zywcr;
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
        GgHjGxyxcgjEntity that = (GgHjGxyxcgjEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(modifyUserId, that.modifyUserId) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(seqNo, that.seqNo) &&
                Objects.equals(zsbh, that.zsbh) &&
                Objects.equals(bz, that.bz) &&
                Objects.equals(jzmc, that.jzmc) &&
                Objects.equals(dj, that.dj) &&
                Objects.equals(xmmc, that.xmmc) &&
                Objects.equals(zywcr, that.zywcr) &&
                Objects.equals(pdsj, that.pdsj) &&
                Objects.equals(cydws, that.cydws) &&
                Objects.equals(xxmc, that.xxmc) &&
                Objects.equals(xxdm, that.xxdm) &&
                Objects.equals(xmmcZz, that.xmmcZz);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, modifyUserId, modifyTime, seqNo, zsbh, bz, jzmc, dj, xmmc, zywcr, pdsj, cydws, xxmc, xxdm, xmmcZz);
    }
}
