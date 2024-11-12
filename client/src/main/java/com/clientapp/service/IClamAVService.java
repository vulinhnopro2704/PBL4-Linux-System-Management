package com.clientapp.service;

import java.util.List;

public interface IClamAVService {
    void startScan(String directoryPath, boolean recursiveCheck, boolean infectedOnlyCheck, boolean verboseCheck, boolean moveCheck, String moveDirField, boolean copyCheck, String copyDirField, boolean removeCheck, boolean scanMailCheck, boolean scanArchiveCheck, boolean scanPdfCheck, boolean scanOle2Check, String logFileField);
    List<String> buildCommand(String directoryPath, boolean recursiveCheck, boolean infectedOnlyCheck, boolean verboseCheck, boolean moveCheck, String moveDirField, boolean copyCheck, String copyDirField, boolean removeCheck, boolean scanMailCheck, boolean scanArchiveCheck, boolean scanPdfCheck, boolean scanOle2Check, String logFileField);
    void runClamAVScan(List<String> command);
}
