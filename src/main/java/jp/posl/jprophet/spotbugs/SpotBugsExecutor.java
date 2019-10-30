package jp.posl.jprophet.spotbugs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.FindBugs;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.TextUICommandLine;
import edu.umd.cs.findbugs.filter.FilterException;
import jp.posl.jprophet.ProjectBuilder;
import jp.posl.jprophet.RepairConfiguration;


public class SpotBugsExecutor {

    private String resultPath;
    private final String resultFileName = "result.xml";


    /**
     * 
     * @param resultPath 結果を格納するディレクトリ
     */
    public SpotBugsExecutor(String resultPath) {
        this.resultPath = resultPath;
    }


    /**
     * プロジェクトをビルドし、SpotBugsを適用する
     * @param RepairConfiguration 対象のプロジェクトのconfig
     */
    public void exec(RepairConfiguration config) {
        ProjectBuilder projectBuilder = new ProjectBuilder();
        projectBuilder.build(config);
        File resultDir = new File(resultPath);
        if (!resultDir.exists()) {
            resultDir.mkdir();
        }
        try {
            runSpotBugs(config);
        }
        catch(FilterException|IOException|InterruptedException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
    

    /**
     * クラスファイルを受け取り、それらを対象にSpotBugsを適用する
     * @param RepairConfiguration 対象のプロジェクトのconfig
     * @throws FilterException
     * @throws IOException
     * @throws InterruptedException
     */
    private void runSpotBugs(RepairConfiguration config) throws FilterException, IOException, InterruptedException {       
        FindBugs2 findBugs = new FindBugs2();
        TextUICommandLine commandLine = new TextUICommandLine();
        final String outputPath = resultPath + "/" + resultFileName;   
        Project SB_project = new Project();
        List<String> dirList = new ArrayList<String>();
        dirList.add(config.getBuildPath());
        SB_project.addSourceDirs(dirList);
        findBugs.setProject(SB_project);
        String[] argv = new String[]{"-xml", "-output", outputPath, config.getBuildPath()};
        FindBugs.processCommandLine(commandLine, argv, findBugs);
        FindBugs.runMain(findBugs, commandLine);
        findBugs.execute();
        findBugs.close();
    }


    /**
     * 実行結果ファイルのパスを返す
     * @return 実行結果ファイルのパス
     */
    public String getResultFilePath() {
        return this.resultPath + "/" + resultFileName;
    }

}