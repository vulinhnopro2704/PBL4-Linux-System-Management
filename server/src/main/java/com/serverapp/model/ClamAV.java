package com.serverapp.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
}
