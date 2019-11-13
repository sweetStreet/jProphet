package jp.posl.jprophet.test.writer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import jp.posl.jprophet.patch.PatchCandidate;
import jp.posl.jprophet.test.result.TestResult;

public class CSVTestResultWriter implements TestResultWriter {

    private final HashMap<TestResult, PatchCandidate> patchResults;
    private final String resultFilePath = "./result.csv";

    public CSVTestResultWriter() {
        this.patchResults = new HashMap<TestResult, PatchCandidate>();
    }

    @Override
    public void addTestResult(List<TestResult> testResults, PatchCandidate patch) {
        for(TestResult testResult : testResults) {
            patchResults.put(testResult, patch);
        } 
    }

    @Override
    public void write() {

        final List<String> recodes = new ArrayList<String>();
        for (Map.Entry<TestResult, PatchCandidate> entry : patchResults.entrySet()) {
            final TestResult result = entry.getKey();
            final PatchCandidate patch = entry.getValue();

            final String patchLine = patch.getFilePath() + "," + patch.getLineNumber().get();
            final String resultLine = String.join(",", result.toStringMap().values());
            final String recode = patchLine + "," + resultLine;
            recodes.add(recode);
        }

        try {
            FileUtils.write(new File(resultFilePath), String.join("\n", recodes), "utf-8");
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

}