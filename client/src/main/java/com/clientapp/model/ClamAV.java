package com.clientapp.model;

import lombok.Builder;

@Builder
public class ClamAV {
    public String directoryPath;
    public Boolean recursiveCheck;
    public Boolean infectedOnlyCheck;
    public Boolean verboseCheck;
    public Boolean moveCheck;
    public String moveDirField;
    public Boolean copyCheck;
    public String copyDirField;
    public Boolean removeCheck;
    public Boolean scanMailCheck;
    public Boolean scanArchiveCheck;
    public Boolean scanPdfCheck;
    public Boolean scanOle2Check;
    public String logFileField;

    public ClamAV(String directoryPath, Boolean recursiveCheck, Boolean infectedOnlyCheck, Boolean verboseCheck, Boolean moveCheck, String moveDirField, Boolean copyCheck, String copyDirField, Boolean removeCheck, Boolean scanMailCheck, Boolean scanArchiveCheck, Boolean scanPdfCheck, Boolean scanOle2Check, String logFileField) {
        this.directoryPath = directoryPath;
        this.recursiveCheck = recursiveCheck;
        this.infectedOnlyCheck = infectedOnlyCheck;
        this.verboseCheck = verboseCheck;
        this.moveCheck = moveCheck;
        this.moveDirField = moveDirField;
        this.copyCheck = copyCheck;
        this.copyDirField = copyDirField;
        this.removeCheck = removeCheck;
        this.scanMailCheck = scanMailCheck;
        this.scanArchiveCheck = scanArchiveCheck;
        this.scanPdfCheck = scanPdfCheck;
        this.scanOle2Check = scanOle2Check;
        this.logFileField = logFileField;
    }

    public ClamAV(String directoryPath) {
        this.directoryPath = directoryPath;
        this.recursiveCheck = false;
        this.infectedOnlyCheck = false;
        this.verboseCheck = false;
        this.moveCheck = false;
        this.moveDirField = "";
        this.copyCheck = false;
        this.copyDirField = "";
        this.removeCheck = false;
        this.scanMailCheck = false;
        this.scanArchiveCheck = false;
        this.scanPdfCheck = false;
        this.scanOle2Check = false;
        this.logFileField = "";
    }
}
