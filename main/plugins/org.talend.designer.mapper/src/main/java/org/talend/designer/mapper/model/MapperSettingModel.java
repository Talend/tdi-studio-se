// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.mapper.model;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class MapperSettingModel implements Cloneable {

    /** For MapReduce only. */
    private boolean isReplicatedJoin;

    private boolean isDieOnError;

    private boolean isLookInParallel;

    private boolean isEnableAutoConvertType;

    private String tempDataDir;

    private String rowBufferSize;

    private int levenshteinWeight;

    private int JaccardWeight;


    /**
     * @return the MapReduce lookup tables should be precalculated and distributed across nodes in the cluster.
     */
    public boolean isReplicatedJoin() {
        return this.isReplicatedJoin;
    }

    /**
     * @param replicatedJoin whether the MapReduce lookup tables should be precalculated and distributed across nodes in
     * the cluster.
     */
    public void setReplicatedJoin(boolean isReplicatedJoin) {
        this.isReplicatedJoin = isReplicatedJoin;
    }

    /**
     * Getter for isDieOnError.
     *
     * @return the isDieOnError
     */
    public boolean isDieOnError() {
        return this.isDieOnError;
    }

    /**
     * Sets the isDieOnError.
     *
     * @param isDieOnError the isDieOnError to set
     */
    public void setDieOnError(boolean isDieOnError) {
        this.isDieOnError = isDieOnError;
    }

    /**
     * Getter for isLookInParallel.
     *
     * @return the isLookInParallel
     */
    public boolean isLookInParallel() {
        return this.isLookInParallel;
    }

    /**
     * Sets the isLookInParallel.
     *
     * @param isLookInParallel the isLookInParallel to set
     */
    public void setLookInParallel(boolean isLookInParallel) {
        this.isLookInParallel = isLookInParallel;
    }

    public boolean isEnableAutoConvertType() {
        return this.isEnableAutoConvertType;
    }

    public void setEnableAutoConvertType(boolean isEnableAutoConvertType) {
        this.isEnableAutoConvertType = isEnableAutoConvertType;
    }

    /**
     * Getter for tempDataDir.
     *
     * @return the tempDataDir
     */
    public String getTempDataDir() {
        return this.tempDataDir;
    }

    /**
     * Sets the tempDataDir.
     *
     * @param tempDataDir the tempDataDir to set
     */
    public void setTempDataDir(String tempDataDir) {
        this.tempDataDir = tempDataDir;
    }

    /**
     * Getter for rowBufferSize.
     *
     * @return the rowBufferSize
     */
    public String getRowBufferSize() {
        return this.rowBufferSize;
    }

    /**
     * Sets the rowBufferSize.
     *
     * @param rowBufferSize the rowBufferSize to set
     */
    public void setRowBufferSize(String rowBufferSize) {
        this.rowBufferSize = rowBufferSize;
    }

    public int hasDifferNumWith(MapperSettingModel other) {
        int num = 0;
        if (other == null) {
            return 4;
        }
        if (this.isReplicatedJoin != other.isReplicatedJoin) {
            num++;
        }
        if (this.isDieOnError != other.isDieOnError) {
            num++;
        }
        if (this.isLookInParallel != other.isLookInParallel) {
            num++;
        }
        if (this.isEnableAutoConvertType != other.isEnableAutoConvertType) {
            num++;
        }
        if (this.tempDataDir == null) {
            if (other.tempDataDir != null) {
                num++;
            }
        } else if (!this.tempDataDir.equals(other.tempDataDir)) {
            num++;
        }
        if (this.rowBufferSize == null) {
            if (other.rowBufferSize != null) {
                num++;
            }
        } else if (!this.rowBufferSize.equals(other.rowBufferSize)) {
            num++;
        }

        return num;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapperSettingModel other = (MapperSettingModel) obj;
        if (this.isDieOnError != other.isDieOnError) {
            return false;
        }
        if (this.isReplicatedJoin != other.isReplicatedJoin) {
            return false;
        }
        if (this.isLookInParallel != other.isLookInParallel) {
            return false;
        }
        if (this.isEnableAutoConvertType != other.isEnableAutoConvertType) {
            return false;
        }
        if (this.tempDataDir == null) {
            if (other.tempDataDir != null) {
                return false;
            }
        } else if (!this.tempDataDir.equals(other.tempDataDir)) {
            return false;
        }
        if (this.rowBufferSize == null) {
            if (other.rowBufferSize != null) {
                return false;
            }
        } else if (!this.rowBufferSize.equals(other.rowBufferSize)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        final int prime = 31;

        hash = hash * prime + this.tempDataDir == null ? 0 : this.tempDataDir.hashCode();
        hash = hash * prime + this.rowBufferSize == null ? 0 : this.rowBufferSize.hashCode();

        return hash;
    }

    @Override
    public MapperSettingModel clone() throws CloneNotSupportedException {
        return (MapperSettingModel) super.clone();
    }

    /**
     * Getter for levenshteinWeight.
     *
     * @return the levenshteinWeight
     */
    public int getLevenshteinWeight() {
        return levenshteinWeight;
    }

    /**
     * Sets the levenshteinWeight.
     *
     * @param levenshteinWeight the levenshteinWeight to set
     */
    public void setLevenshteinWeight(int levenshteinWeight) {
        this.levenshteinWeight = levenshteinWeight;
    }

    /**
     * Getter for jaccardWeight.
     *
     * @return the jaccardWeight
     */
    public int getJaccardWeight() {
        return JaccardWeight;
    }

    /**
     * Sets the jaccardWeight.
     *
     * @param jaccardWeight the jaccardWeight to set
     */
    public void setJaccardWeight(int jaccardWeight) {
        this.JaccardWeight = jaccardWeight;
    }

}
